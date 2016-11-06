package org.msyu.parser.methodic;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.msyu.parser.methodic.EnumLimiterInfo.withEnumClassAndSet;
import static org.msyu.parser.methodic.MethodicException.badDefinition;
import static org.msyu.parser.methodic.MethodicException.badEnumLimiter;
import static org.msyu.parser.methodic.MethodicException.badMethod;
import static org.msyu.parser.methodic.MethodicException.badMethodParam;

public final class MethodicGrammar {

	private static final Constructor<MethodHandles.Lookup> LOOKUP_CONSTRUCTOR;

	static {
		try {
			LOOKUP_CONSTRUCTOR = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
			if (!LOOKUP_CONSTRUCTOR.isAccessible()) {
				LOOKUP_CONSTRUCTOR.setAccessible(true);
			}
		} catch (Exception e) {
			throw mhLookupAccessFailure(e);
		}
	}

	private static RuntimeException mhLookupAccessFailure(Throwable cause) {
		return new RuntimeException("MethodHandle.Lookup constructor access failure", cause);
	}

	private final GrammarBuilder gb;
	private final Map<Object, ASymbol> symbolByRep = new HashMap<>();
	private final Map<org.msyu.parser.glr.Production, Method> methodByProduction = new HashMap<>();
	private final Set<org.msyu.parser.glr.Production> noopProductions = new HashSet<>();
	private final Set<Class<?>> definitionIfaces = new HashSet<>();
	private final Map<Method, MethodHandle> handleByMethod = new HashMap<>();

	public SymbolNameGenerator symbolNameGenerator = SymbolNameGenerator.DEFAULT;

	public MethodicGrammar(GrammarBuilder gb) {
		this.gb = gb;
	}

	public final void addSymbol(Object rep, ASymbol newSymbol) {
		ASymbol oldSymbol = symbolByRep.putIfAbsent(rep, newSymbol);
		if (oldSymbol != null) {
			throw new IllegalArgumentException(String.format(
					"attempt to remap %s from %sterminal %s to %sterminal %s",
					rep,
					oldSymbol instanceof NonTerminal ? "non-" : "",
					oldSymbol.getName(),
					newSymbol instanceof NonTerminal ? "non-" : "",
					newSymbol.getName()
			));
		}
	}

	/**
	 * @throws MethodicException
	 */
	public final void addDefinition(Class<?> definition) {
		if (!definition.isInterface()) {
			throw badDefinition("methodic definition must be an interface", definition);
		}

		MethodHandles.Lookup lookup;
		try {
			lookup = LOOKUP_CONSTRUCTOR.newInstance(definition);
		} catch (Exception e) {
			throw mhLookupAccessFailure(e);
		}

		Map<Class<?>, EnumLimiterInfo> limiterInfoByEnumClass = new HashMap<>();
		{
			EnumLimiters enumLimitersAnnotation = definition.getDeclaredAnnotation(EnumLimiters.class);
			if (enumLimitersAnnotation != null) {
				for (Class<? extends Annotation> limiter : enumLimitersAnnotation.value()) {
					Method valueMethod;
					try {
						valueMethod = limiter.getDeclaredMethod("value");
					} catch (NoSuchMethodException e) {
						throw badEnumLimiter("no value() method", limiter, definition);
					}
					Class<?> enumClass = valueMethod.getReturnType().getComponentType();
					if (enumClass == null) {
						throw badEnumLimiter("value() does not return an array", limiter, definition);
					}
					if (!enumClass.isEnum()) {
						throw badEnumLimiter("value() returns an array of non-enum values", limiter, definition);
					}
					MethodHandle getter;
					try {
						getter = lookup.unreflect(valueMethod);
					} catch (IllegalAccessException e) {
						throw badEnumLimiter("can't access value()", limiter, definition).initCause(e);
					}
					limiterInfoByEnumClass.put(enumClass, new EnumLimiterInfo(limiter, getter));
				}
			}
		}

		Map<ProductionSignature, Method> methodBySignature = new HashMap<>();
		Set<Object> unregisteredReps = new LinkedHashSet<>();
		for (Method method : definition.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			boolean isDefaultMethod = method.isDefault();
			if (isDefaultMethod) {
				MethodHandle handle;
				try {
					handle = lookup.unreflectSpecial(method, definition);
				} catch (IllegalAccessException e) {
					throw badDefinition("can't access definition methods", definition).initCause(e);
				}
				handleByMethod.put(method, handle);
			}
			if (!method.isAnnotationPresent(Production.class)) {
				continue;
			}
			if (!isDefaultMethod) {
				throw badMethod("a @Production method without a default implementation", method);
			}

			Object lhs = checkAndRepresent(
					method,
					method.getGenericReturnType(),
					limiterInfoByEnumClass,
					true,
					desc -> badMethod("@Production LHS " + desc, method),
					unregisteredReps
			);

			Parameter[] parameters = method.getParameters();
			Object[] rhs = new Object[parameters.length];
			for (int i = 0; i < parameters.length; ++i) {
				int paramIndex = i;
				rhs[i] = checkAndRepresent(
						parameters[i],
						parameters[i].getParameterizedType(),
						limiterInfoByEnumClass,
						false,
						desc -> badMethodParam("@Production RHS element " + desc, method, paramIndex),
						unregisteredReps
				);
			}

			ProductionSignature psig = new ProductionSignature(lhs, rhs);
			Method anotherMethod = methodBySignature.putIfAbsent(psig, method);
			if (anotherMethod != null) {
				throw badDefinition(
						String.format(
								"duplicate production methods with signature %s: %s and %s",
								psig.toString(),
								anotherMethod.getName(),
								method.getName()
						),
						definition
				);
			}
			methodBySignature.put(psig, method);
		}

		for (Object unregisteredRep : unregisteredReps) {
			if (unregisteredRep instanceof Class) {
				Class<?> unregisteredClass = (Class<?>) unregisteredRep;
				symbolByRep.put(unregisteredClass, gb.addNonTerminal(symbolNameGenerator.byClass(unregisteredClass)));
			} else if (unregisteredRep instanceof EnumSet) {
				EnumSet<?> set = (EnumSet<?>) unregisteredRep;
				NonTerminal nonTerminal = gb.addNonTerminal(withEnumClassAndSet(set, symbolNameGenerator::byEnumSet));
				for (Enum<?> element : set) {
					noopProductions.add(gb.addProduction(nonTerminal, symbolByRep.get(element)));
				}
				symbolByRep.put(set, nonTerminal);
			} else {
				throw new UnsupportedOperationException("tell the developers: can't generate symbol for rep " + unregisteredRep);
			}
		}

		List<ASymbol> rhs = new ArrayList<>();
		for (Map.Entry<ProductionSignature, Method> sigAndMethod : methodBySignature.entrySet()) {
			ProductionSignature sig = sigAndMethod.getKey();
			rhs.clear();
			for (Object rep : sig.rhs) {
				rhs.add(symbolByRep.get(rep));
			}
			org.msyu.parser.glr.Production prod = gb.addProduction((NonTerminal) symbolByRep.get(sig.lhs), rhs);
			methodByProduction.put(prod, sigAndMethod.getValue());
		}
		definitionIfaces.add(definition);
	}

	private Object checkAndRepresent(
			AnnotatedElement annotatedElement,
			Type type,
			Map<Class<?>, EnumLimiterInfo> limiterInfoByEnumClass,
			boolean mustBeNonTerminal,
			Function<String, MethodicException> onError,
			Set<Object> unregisteredReps
	) {
		if (!(type instanceof Class)) {
			throw onError.apply("is not a raw class");
		}
		Class<?> klass = (Class<?>) type;
		if (klass.isArray()) {
			throw onError.apply("is an array");
		}
		Object rep = represent(annotatedElement, klass, limiterInfoByEnumClass, onError);
		ASymbol symbol = symbolByRep.get(rep);
		if (mustBeNonTerminal && symbol instanceof Terminal) {
			throw onError.apply("is registered as a terminal");
		}
		maybeAddToUnregistered(rep, symbol, unregisteredReps, onError);
		return rep;
	}

	private Object represent(
			AnnotatedElement annotatedElement,
			Class<?> klass,
			Map<Class<?>, EnumLimiterInfo> limiterInfoByEnumClass,
			Function<String, MethodicException> onError
	) {
		if (klass.isEnum()) {
			EnumLimiterInfo limiterInfo = limiterInfoByEnumClass.get(klass);
			if (limiterInfo == null) {
				return klass;
			}
			EnumSet<?> limit = limiterInfo.getEnumLimiterValue(annotatedElement);
			if (limit == null) {
				return klass;
			}
			if (limit.isEmpty()) {
				throw onError.apply("is limited to zero values");
			}
			// todo: automatically generate symbols for individual enum values?
			return limit;
		}
		return klass;
	}

	private void maybeAddToUnregistered(Object rep, ASymbol symbol, Set<Object> unregisteredReps, Function<String, MethodicException> onError) {
		if (symbol == null) {
			if (symbolNameGenerator == null) {
				throw onError.apply("has no registered symbol");
			}
			unregisteredReps.add(rep);
		}
	}

//	private static boolean isOnlySeqStruct(Class<?>[] ifacesArray) {
//		return ifacesArray.length == 1 && ifacesArray[0] == SequenceStruct.class;
//	}

	public final ASymbol getSymbolOf(Class<?> klass) {
		return symbolByRep.get(klass);
	}

	public void seal() {
		Class<?>[] ifaceArray = definitionIfaces.toArray(new Class<?>[0]);
		Object combinedDefObject = Proxy.newProxyInstance(
				ifaceArray[0].getClassLoader(),
				ifaceArray,
				(proxy, method, args) -> {
					MethodHandle handle = handleByMethod.get(method);
					if (handle == null) {
						throw new MethodicException(
								"asked to invoke an unimplemented method: " + method.toGenericString(),
								null,
								method
						);
					}
					return handle.invokeWithArguments(args);
				}
		);
		for (Map.Entry<Method, MethodHandle> methodAndHandle : handleByMethod.entrySet()) {
			methodAndHandle.setValue(methodAndHandle.getValue().bindTo(combinedDefObject));
		}
	}

	public final List<Object> reduce(org.msyu.parser.glr.Production production, List<Object> tokens) {
		MethodHandle handle = handleByMethod.get(methodByProduction.get(production));
		if (handle != null) {
			try {
				return Collections.singletonList(handle.invokeWithArguments(tokens));
			} catch (Throwable t) {
				throw new ReductionException(t);
			}
		}
		if (noopProductions.contains(production)) {
			return tokens;
		}
		throw new ReductionException(new UnsupportedOperationException(production.toString()));
	}

}
