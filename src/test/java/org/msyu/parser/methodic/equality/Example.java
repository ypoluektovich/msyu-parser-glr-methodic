package org.msyu.parser.methodic.equality;

import org.msyu.parser.glr.GrammarBuilder;
import org.msyu.parser.glr.NonTerminal;
import org.msyu.parser.glr.Terminal;
import org.msyu.parser.methodic.MethodicGrammar;
import org.testng.annotations.Test;

public class Example {

	@Test(enabled = false)
	public void run() {
		GrammarBuilder gb = new GrammarBuilder();
		MethodicGrammar mg = new MethodicGrammar(gb);
		mg.addDefinition(ECGrammar.class);
		Terminal eoi = gb.addTerminal("eoi");
		NonTerminal goal = gb.addNonTerminal("Goal");
		gb.addProduction(goal, mg.getSymbolOf(Condition.class), eoi);

		// todo: a callback
		// todo: parse an example input
	}

}
