package org.msyu.parser.methodic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class MethodicException extends RuntimeException {

	private final Class<?> iface;
	private final Method method;

	MethodicException(String message, Class<?> iface, Method method) {
		super(message);
		this.iface = iface;
		this.method = method;
	}

	public final Class<?> getInterface() {
		return iface;
	}

	public final Method getMethod() {
		return method;
	}

	@Override
	public final MethodicException initCause(Throwable cause) {
		super.initCause(cause);
		return this;
	}

	static MethodicException badDefinition(String message, Class<?> definition) {
		return new MethodicException(message + ": " + definition.getName(), definition, null);
	}

	static MethodicException badEnumLimiter(String message, Class<? extends Annotation> limiter, Class<?> definition) {
		return new MethodicException(message + ": enum limiter " + limiter.getName() + " on " + definition.getName(), definition, null);
	}

	static MethodicException badMethod(String message, Method method) {
		return new MethodicException(message + ": " + method.toGenericString(), method.getDeclaringClass(), method);
	}

	static MethodicException badMethodParam(String message, Method method, int index) {
		return new MethodicException(
				message + ": parameter " + index + " of " + method.toGenericString(),
				method.getDeclaringClass(),
				method
		);
	}

}
