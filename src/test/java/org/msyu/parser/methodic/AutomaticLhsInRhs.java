package org.msyu.parser.methodic;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.Grammar;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.Lifeline;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.ScannerlessState;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.treestack.TreeStack;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.msyu.javautil.cf.Iterators.singletonIterator;
import static org.testng.Assert.assertEquals;
import static org.testng.internal.collections.Ints.asList;

public class AutomaticLhsInRhs {

	final GrammarBuilder gb = new GrammarBuilder();
	final Terminal s = gb.addTerminal("s");
	final MethodicGrammar mg = new MethodicGrammar(gb);
	final Grammar g;
	final Sapling sapling;
	Consumer<Object> iConsumer;
	GlrCallback<Object> callback;

	public AutomaticLhsInRhs() {
		this.mg.addSymbol(String.class, this.s);
		this.mg.addDefinition(MG.class);
		this.mg.seal();

		this.g = this.gb.build();
		this.sapling = this.g.newSapling((NonTerminal) mg.getSymbolOf(Integer.class));
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
			public Object reduce(Object oldBranch, org.msyu.parser.glr.Production production, Lifeline lifeline) {
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

	@Test
	public void test() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance("1", callback);
		state = state.advance("2", callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer, times(3)).accept(captor.capture());
		assertEquals(captor.getAllValues(), asList(1, 2, 3));
	}

}
