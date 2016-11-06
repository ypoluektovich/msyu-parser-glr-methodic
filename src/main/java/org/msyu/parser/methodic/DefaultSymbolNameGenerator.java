package org.msyu.parser.methodic;

import java.util.EnumSet;
import java.util.StringJoiner;

final class DefaultSymbolNameGenerator implements SymbolNameGenerator {

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

	@Override
	public final String byClass(Class<?> klass) {
		return describeClass(klass);
	}

	@Override
	public final <E extends Enum<E>> String byEnumSet(Class<E> klass, EnumSet<E> elements) {
		return describeEnumSet(klass, elements);
	}

}
