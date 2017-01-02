package org.msyu.parser.methodic;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ScannerlessState;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class StaticMethods extends StringIntGrammarTestBase {

	public StaticMethods() {
		super(MG.class);
	}

	private interface MG {
		static int parse(String s) {
			return Integer.parseInt(s);
		}

		@Production default Integer prod(String s1, String s2) {
			return parse(s1 + s2);
		}
	}

	@Test
	public void test() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance("2", callback);
		state = state.advance("3", callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer).accept(captor.capture());
		assertEquals(captor.getValue(), 23);
	}

}
