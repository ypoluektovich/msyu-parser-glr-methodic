package org.msyu.parser.methodic;

import org.msyu.parser.glr.*;
import org.msyu.parser.glr.Production;
import org.msyu.parser.treestack.TreeStack;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.msyu.javautil.cf.Iterators.singletonIterator;

public abstract class StringIntGrammarTestBase {

	final GrammarBuilder gb = new GrammarBuilder();
	final Terminal s = gb.addTerminal("s");
	final NonTerminal i = gb.addNonTerminal("I");

	final MethodicGrammar mg = new MethodicGrammar(gb);

	final Grammar g;
	final Sapling sapling;

	Consumer<Object> iConsumer;
	GlrCallback<Object> callback;

	public StringIntGrammarTestBase(Class<?> grammarDef) {
		mg.addSymbol(String.class, s);
		mg.addSymbol(Integer.class, i);
		mg.addDefinition(grammarDef);
		mg.seal();

		g = gb.build();
		sapling = g.newSapling(i);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod() {
		iConsumer = (Consumer<Object>) mock(Consumer.class);
		callback = spy(new GlrCallback<Object>() {
			private final TreeStack<Object> stack = new TreeStack<>();

			@Override
			public Terminal getSymbolOfToken(Object token) {
				return s;
			}

			@Override
			public Object shift(Object oldBranch, List<ASymbol> prependedEmptySymbols, Object token) {
				return stack.push(oldBranch, singletonIterator(token));
			}

			@Override
			public Object skip(Object oldBranch, List<ASymbol> emptySymbols) {
				throw new UnsupportedOperationException("GLR skip");
			}

			@Override
			public Object reduce(Object oldBranch, Production production, Lifeline lifeline) {
				List<Object> rhs = new ArrayList<>(production.rhs.size());
				Object poppedId = stack.pop(oldBranch, production.rhs.size(), t -> rhs.add(0, t));
				List<Object> reduced = mg.reduce(production, rhs);
				iConsumer.accept(reduced.get(0));
				return stack.push(poppedId, reduced.iterator());
			}

			@Override
			public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
				if (emptySymbols.isEmpty()) {
					return oldBranch;
				}
				throw new UnsupportedOperationException("GLR insert");
			}

			@Override
			public void cutLifelines(Predicate<Lifeline> lifelineIsCut) {
			}
		});
	}

}
