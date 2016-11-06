package org.msyu.parser.methodic;

import java.util.EnumSet;

public interface SymbolNameGenerator {

	String byClass(Class<?> klass);

	<E extends Enum<E>> String byEnumSet(Class<E> klass, EnumSet<E> elements);

	String repeat(int min, int max, Object elementRep);

	String repeatInner(int count, Object elementRep);


	static String describe(SymbolNameGenerator generator, Object rep) {
		if (rep instanceof Class) {
			return generator.byClass((Class<?>) rep);
		} else if (rep instanceof EnumSet) {
			return EnumLimiterInfo.withEnumClassAndSet((EnumSet<?>) rep, generator::byEnumSet);
		} else if (rep instanceof RepeatRep) {
			RepeatRep repeatRep = (RepeatRep) rep;
			int minCount = repeatRep.annotation.value();
			int maxCount;
			if (minCount > 0) {
				maxCount = minCount;
			} else {
				minCount = repeatRep.annotation.min();
				maxCount = repeatRep.annotation.max();
			}
			return generator.repeat(minCount, maxCount, describe(generator, repeatRep.elementRep));
		} else {
			return "UnsupportedRep[" + rep + "]";
		}
	}

}
