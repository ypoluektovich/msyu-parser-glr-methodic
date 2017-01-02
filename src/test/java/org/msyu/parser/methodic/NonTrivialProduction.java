package org.msyu.parser.methodic;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ScannerlessState;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class NonTrivialProduction extends StringIntGrammarTestBase {

	public NonTrivialProduction() {
		super(MG.class);
	}

	private interface MG {
		@Production
		default Integer concatAndParse(String s1, String s2) {
			return Integer.parseInt(s1 + s2);
		}
	}

	@Test
	public void test() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance("1", callback);
		state = state.advance("2", callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer).accept(captor.capture());
		assertEquals(captor.getValue(), 12);
	}

}
