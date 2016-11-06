package org.msyu.parser.methodic;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.treestack.TreeStack;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.msyu.javautil.cf.Iterators.singletonIterator;
import static org.testng.Assert.assertEquals;
import static org.testng.internal.collections.Ints.asList;

public class AutomaticRhsNonTerminalCreation {

	final GrammarBuilder gb = new GrammarBuilder();
	final Terminal s = gb.addTerminal("s");
	final MethodicGrammar mg = new MethodicGrammar(gb);

	private interface MG {
		@Production
		default Integer str2int(String s) {
			return Integer.parseInt(s);
		}

		@Production
		default Integer addition(Integer i1, Integer i2) {
			return i1 + i2;
		}
	}

	{
		mg.addDefinition(MG.class);
		mg.seal();
	}

	org.msyu.parser.glr.Production stringT2N = gb.addProduction((NonTerminal) mg.getSymbolOf(String.class), s);

	final Grammar g = gb.build();
	final Sapling sapling = g.newSapling((NonTerminal) mg.getSymbolOf(Integer.class));

	Consumer<Object> iConsumer;
	GlrCallback<Object> callback;

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
			public Object reduce(Object oldBranch, org.msyu.parser.glr.Production production) {
				List<Object> rhs = new ArrayList<>(production.rhs.size());
				Object poppedId = stack.pop(oldBranch, production.rhs.size(), t -> rhs.add(0, t));
				List<Object> reduced;
				if (production == stringT2N) {
					reduced = rhs;
				} else {
					reduced = mg.reduce(production, rhs);
					iConsumer.accept(reduced.get(0));
				}
				return stack.push(poppedId, reduced.iterator());
			}

			@Override
			public Object insert(Object oldBranch, List<ASymbol> emptySymbols) {
				if (emptySymbols.isEmpty()) {
					return oldBranch;
				}
				throw new UnsupportedOperationException("GLR insert");
			}
		});
	}

	@Test
	public void test() throws Exception {
		State state = State.initializeFrom(sapling);

		state = state.advance("1", callback);
		state = state.advance("2", callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer, times(3)).accept(captor.capture());
		assertEquals(captor.getAllValues(), asList(1, 2, 3));
	}

}
