package org.msyu.parser.methodic;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static org.msyu.parser.methodic.SymbolNameGenerator.describe;

final class ProductionSignature {

	final Object lhs;
	final Object[] rhs;

	ProductionSignature(Object lhs, Object[] rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != ProductionSignature.class) {
			return false;
		}
		ProductionSignature that = (ProductionSignature) o;
		return lhs == that.lhs && Arrays.equals(rhs, that.rhs);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(lhs, rhs);
	}

	@Override
	public final String toString() {
		StringJoiner sj = new StringJoiner(", ", describe(DefaultSymbolNameGenerator.INSTANCE, lhs) + " \u2192", "");
		for (Object element : rhs) {
			sj.add(describe(DefaultSymbolNameGenerator.INSTANCE, element));
		}
		return sj.toString();
	}

}
