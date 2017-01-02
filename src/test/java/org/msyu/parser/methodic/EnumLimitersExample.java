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
import org.msyu.parser.glr.UnexpectedTokenException;
import org.msyu.parser.treestack.TreeStack;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.msyu.javautil.cf.Iterators.singletonIterator;
import static org.testng.Assert.assertEquals;

public class EnumLimitersExample {

	final GrammarBuilder gb = new GrammarBuilder();
	final MethodicGrammar mg = new MethodicGrammar(gb);

	@Retention(RUNTIME)
	@Target({ElementType.PARAMETER, ElementType.METHOD})
	private @interface Limit {
		RetentionPolicy[] value();
	}

	@EnumLimiters(Limit.class)
	private interface MG {
		@Production default String prod(@Limit({SOURCE, CLASS}) RetentionPolicy t) {
			return t.name();
		}
	}

	{
		for (RetentionPolicy rp : RetentionPolicy.values()) {
			mg.addSymbol(rp, gb.addTerminal(rp.name()));
		}
		mg.addDefinition(MG.class);
		mg.seal();
	}

	final Grammar g = gb.build();
	final Sapling sapling = g.newSapling((NonTerminal) mg.getSymbolOf(String.class));

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
				return (Terminal) g.getSymbolByName(token.toString());
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
				iConsumer.accept(reduced);
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

	@Test
	public void accept() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance(SOURCE, callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer, times(2)).accept(captor.capture());
		assertEquals(captor.getAllValues(), asList(singletonList(SOURCE), singletonList(SOURCE.name())));
	}

	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void reject() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance(RUNTIME, callback);
	}

}
