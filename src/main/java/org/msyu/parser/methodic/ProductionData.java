package org.msyu.parser.methodic;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.NonTerminal;

import java.lang.reflect.Method;
import java.util.List;

final class ProductionData {

	final Method method;
	final NonTerminal lhs;
	final List<ASymbol> rhs;

	ProductionData(Method method, NonTerminal lhs, List<ASymbol> rhs) {
		this.method = method;
		this.lhs = lhs;
		this.rhs = rhs;
	}

}
