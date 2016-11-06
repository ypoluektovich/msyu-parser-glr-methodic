package org.msyu.parser.methodic;

import org.msyu.parser.glr.ASymbol;
import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.glr.UnexpectedTokenException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class RepeatInListParameters {

	GrammarBuilder gb;
	MethodicGrammar mg;
	Terminal terminal;
	MethodicCallbackBase callback;

	class MyCallback extends MethodicCallbackBase {
		MyCallback(MethodicGrammar methodicGrammar) {
			super(methodicGrammar);
		}

		@Override
		public Object emptySymbolPlaceholder(ASymbol symbol) {
			return emptyList();
		}
	}

	@BeforeMethod
	public void init() {
		gb = new GrammarBuilder();
		mg = new MethodicGrammar(gb);
		terminal = gb.addTerminal("t");
		mg.addSymbol(Integer.class, terminal);
		callback = spy(new MyCallback(mg));
	}


	private interface Exactly2 {
		@Production default String prod(@Repeat(2) List<Integer> terms) {
			return "" + terms.size();
		}
	}

	@Test
	public void exact() throws Exception {
		mg.addDefinition(Exactly2.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(11, callback);
		state = state.advance(22, callback);

		verify(callback).reduced(
				(NonTerminal) mg.getSymbolOf(String.class),
				singletonList(Arrays.<Object>asList(11, 22)),
				singletonList("2")
		);
	}

	@Test
	public void exactUnderflow() throws Exception {
		mg.addDefinition(Exactly2.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(1, callback);

		verify(callback, never()).reduced(any(), any(), any());
	}

	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void exactOverflow() throws Exception {
		mg.addDefinition(Exactly2.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(1, callback);
		state = state.advance(2, callback);
		state = state.advance(3, callback);
	}


	private interface ZeroToTwo {
		@Production default String prod(Integer first, @Repeat(max = 2) List<Integer> terms) {
			return "" + terms.size();
		}
	}

	@Test
	public void zeroToTwo0() throws Exception {
		mg.addDefinition(ZeroToTwo.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(0, callback);

		verify(callback).reduced(
				(NonTerminal) mg.getSymbolOf(String.class),
				asList(0, emptyList()),
				singletonList("0")
		);
	}

	@Test
	public void zeroToTwo1() throws Exception {
		mg.addDefinition(ZeroToTwo.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(0, callback);
		state = state.advance(11, callback);

		verify(callback).reduced(
				(NonTerminal) mg.getSymbolOf(String.class),
				asList(0, singletonList(11)),
				singletonList("1")
		);
	}

	@Test
	public void zeroToTwo2() throws Exception {
		mg.addDefinition(ZeroToTwo.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(0, callback);
		state = state.advance(11, callback);
		state = state.advance(22, callback);

		verify(callback).reduced(
				(NonTerminal) mg.getSymbolOf(String.class),
				asList(0, Arrays.<Object>asList(11, 22)),
				singletonList("2")
		);
	}

	@Test(expectedExceptions = UnexpectedTokenException.class)
	public void zeroToTwo3() throws Exception {
		mg.addDefinition(ZeroToTwo.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(0, callback);
		state = state.advance(11, callback);
		state = state.advance(22, callback);
		state = state.advance(33, callback);
	}


	private interface Any {
		@Production default String prod(Integer first, @Repeat List<Integer> terms) {
			return "" + terms.size();
		}
	}

	@Test
	public void any5() throws Exception {
		mg.addDefinition(Any.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state = state.advance(0, callback);
		state = state.advance(1, callback);
		state = state.advance(2, callback);
		state = state.advance(3, callback);
		state = state.advance(4, callback);
		state = state.advance(5, callback);

		verify(callback).reduced(
				(NonTerminal) mg.getSymbolOf(String.class),
				asList(0, Arrays.<Object>asList(1, 2, 3, 4, 5)),
				singletonList("5")
		);
	}

}
