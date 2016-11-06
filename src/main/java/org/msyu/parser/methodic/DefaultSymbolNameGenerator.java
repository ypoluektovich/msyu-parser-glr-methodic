package org.msyu.parser.methodic;

import java.util.EnumSet;
import java.util.StringJoiner;

enum DefaultSymbolNameGenerator implements SymbolNameGenerator {

	INSTANCE;

	static String describeClass(Class<?> klass) {
		return klass.getName();
	}

	static <E extends Enum<E>> String describeEnumSet(Class<E> klass, EnumSet<E> elements) {
		StringJoiner sj = new StringJoiner(",", klass.getName() + "-limited-to-", "");
		for (E element : elements) {
			sj.add(element.name());
		}
		return sj.toString();
	}

	static String describeRepeat(int min, int max, Object element) {
		return "Repeat-" + min + "-" + max + "-" + SymbolNameGenerator.describe(INSTANCE, element);
	}


	@Override
	public final String byClass(Class<?> klass) {
		return describeClass(klass);
	}

	@Override
	public final <E extends Enum<E>> String byEnumSet(Class<E> klass, EnumSet<E> elements) {
		return describeEnumSet(klass, elements);
	}

	@Override
	public final String repeat(int min, int max, Object elementRep) {
		return describeRepeat(min, max, elementRep);
	}

	@Override
	public final String repeatInner(int count, Object elementRep) {
		return describeRepeat(count, count, elementRep);
	}

}
