package org.msyu.parser.methodic;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.StringJoiner;

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
		StringJoiner sj = new StringJoiner(", ", describe(lhs) + " \u2192", "");
		for (Object element : rhs) {
			sj.add(describe(element));
		}
		return sj.toString();
	}

	private static String describe(Object rep) {
		if (rep instanceof Class) {
			return DefaultSymbolNameGenerator.describeClass((Class<?>) rep);
		} else if (rep instanceof EnumSet) {
			return EnumLimiterInfo.withEnumClassAndSet((EnumSet<?>) rep, DefaultSymbolNameGenerator::describeEnumSet);
		} else {
			return "UnsupportedRep[" + rep + "]";
		}
	}

}
