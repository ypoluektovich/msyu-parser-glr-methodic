package org.msyu.parser.methodic;

import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Sapling;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.fail;

public class RepeatInListParameters {

	GrammarBuilder gb;
	MethodicGrammar mg;
	Terminal terminal;
	MethodicCallbackBase callback;

	@BeforeMethod
	public void init() {
		gb = new GrammarBuilder();
		mg = new MethodicGrammar(gb);
		terminal = gb.addTerminal("t");
		mg.addSymbol(Integer.class, terminal);
		callback = spy(new MethodicCallbackBase(mg));
	}

	@Test
	public void exact() throws Exception {
		mg.addDefinition(Exactly2.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state.advance(1, callback);
		state.advance(2, callback);

		verify(callback).reduced(
				(NonTerminal) mg.getSymbolOf(String.class),
				eq(Arrays.<Object>asList(1, 2)),
				eq(Collections.singletonList("2"))
		);
	}

	@Test
	public void exactUnderflow() throws Exception {
		mg.addDefinition(Exactly2.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state.advance(1, callback);

		verify(callback, never()).reduced(any(), any(), any());
	}

	@Test
	public void exactOverflow() throws Exception {
		mg.addDefinition(Exactly2.class);
		mg.seal();

		Sapling sapling = gb.build().newSapling((NonTerminal) mg.getSymbolOf(String.class));

		State state = State.initializeFrom(sapling);
		state.advance(1, callback);
		state.advance(2, callback);
		state.advance(3, callback);

		fail();
	}

	private interface Exactly2 {
		@Production default String prod(@Repeat(2) List<Integer> terms) {
			return "" + terms.size();
		}
	}

}
