package org.msyu.parser.methodic;

import org.msyu.javautil.cf.Iterators;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Production;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.treestack.TreeStack;

import java.util.ArrayList;
import java.util.List;

public class MethodicCallbackBase implements GlrCallback<Object> {

	public final TreeStack<Object> stack = new TreeStack<>();

	public final MethodicGrammar methodicGrammar;

	public MethodicCallbackBase(MethodicGrammar methodicGrammar) {
		this.methodicGrammar = methodicGrammar;
	}

	@Override
	public Terminal getSymbolOfToken(Object token) {
		return (Terminal) methodicGrammar.getSymbolOf(token.getClass());
	}

	public Object emptySymbolPlaceholder(ASymbol symbol) {
		throw new UnsupportedOperationException("empty symbols");
	}

	@Override
	public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, Object token) {
		return stack.push(
				oldBranch,
				Iterators.concat(
						prependedEmptySymbols.stream()
								.map(this::emptySymbolPlaceholder)
								.iterator(),
						Iterators.singletonIterator(token)
				)
		);
	}

	@Override
	public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
		return stack.push(
				oldBranch,
				emptySymbols.stream()
						.map(this::emptySymbolPlaceholder)
						.iterator()
		);
	}

	@Override
	public Object reduce(Object oldBranch, Production production) {
		List<Object> tokens = new ArrayList<>(production.rhs.size());
		Object poppedId = stack.pop(oldBranch, production.rhs.size(), x -> tokens.add(0, x));
		List<Object> result = methodicGrammar.reduce(production, tokens);
		reduced(production.lhs, tokens, result);
		return stack.push(poppedId, result.iterator());
	}

	public void reduced(NonTerminal lhs, List<Object> rhs, List<Object> result) {
		// do nothing
	}

	@Override
	public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
		Object[] token = new Object[1];
		Object poppedId = stack.pop(oldBranch, 1, x -> token[0] = x);
		return stack.push(
				poppedId,
				Iterators.concat(
						emptySymbols.stream().map(this::emptySymbolPlaceholder).iterator(),
						Iterators.singletonIterator(token[0])
				)
		);
	}

}
