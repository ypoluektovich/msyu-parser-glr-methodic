package org.msyu.parser.methodic;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.BiFunction;

final class EnumLimiterInfo {

	private final Class<? extends Annotation> limiterClass;
	private final MethodHandle limitGetter;

	EnumLimiterInfo(Class<? extends Annotation> limiterClass, MethodHandle limitGetter) {
		this.limiterClass = limiterClass;
		this.limitGetter = limitGetter;
	}

	final EnumSet<?> getEnumLimiterValue(AnnotatedElement annotatedElement) {
		Annotation annotation = annotatedElement.getDeclaredAnnotation(limiterClass);
		if (annotation == null) {
			return null;
		}
		try {
			return EnumSet.copyOf((Collection) Arrays.asList((Enum[]) limitGetter.invoke(annotation)));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	static <E extends Enum<E>, R> R withEnumClassAndSet(EnumSet<E> set, BiFunction<Class<E>, EnumSet<E>, R> action) {
		return action.apply(set.iterator().next().getDeclaringClass(), set);
	}

}
