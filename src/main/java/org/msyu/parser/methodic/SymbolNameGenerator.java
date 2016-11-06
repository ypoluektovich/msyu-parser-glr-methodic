package org.msyu.parser.methodic;

import java.util.EnumSet;

public interface SymbolNameGenerator {

	String byClass(Class<?> klass);

	<E extends Enum<E>> String byEnumSet(Class<E> klass, EnumSet<E> elements);

	SymbolNameGenerator DEFAULT = new DefaultSymbolNameGenerator();

}
