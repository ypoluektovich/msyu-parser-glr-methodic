package org.msyu.parser.methodic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Repeat {
	int INF = Integer.MAX_VALUE;

	int value() default 0;
	int min() default 0;
	int max() default INF;
}
