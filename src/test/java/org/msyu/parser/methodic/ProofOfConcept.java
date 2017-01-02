package org.msyu.parser.methodic;

import org.mockito.ArgumentCaptor;
import org.msyu.parser.glr.ScannerlessState;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class ProofOfConcept extends StringIntGrammarTestBase {

	public ProofOfConcept() {
		super(MG.class);
	}

	private interface MG {
		@Production
		default Integer str2int(String s) {
			return Integer.parseInt(s);
		}
	}

	@Test
	public void test() throws Exception {
		ScannerlessState state = ScannerlessState.initializeFrom(sapling);

		state = state.advance("1", callback);

		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(iConsumer).accept(captor.capture());
		assertEquals(captor.getValue(), 1);
	}

}
