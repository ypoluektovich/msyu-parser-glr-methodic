package org.msyu.parser.methodic;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ScannerlessState;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.internal.collections.Ints.asList;

public class NonTrivialGrammar extends StringIntGrammarTestBase {

	public NonTrivialGrammar() {
		super(MG.class);
	}

	private interface MG {
		@Production default Integer parse(String s) {
			return Integer.parseInt(s);
		}

		@Production default Integer multiply(Integer i1, Integer i2) {
			return i1 * i2;
		}
	}

	@Test
	public void test() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance("2", callback);
		state = state.advance("3", callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer, times(3)).accept(captor.capture());
		assertEquals(captor.getAllValues(), asList(2, 3, 6));
	}

}
