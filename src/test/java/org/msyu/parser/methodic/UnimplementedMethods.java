package org.msyu.parser.methodic;

import org.msyu.parser.glr.State;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UnimplementedMethods extends StringIntGrammarTestBase {

	public UnimplementedMethods() {
		super(MG.class);
	}

	private interface MG {
		int unimplemented();

		@Production default Integer prod(String s) {
			unimplemented();
			return Integer.parseInt(s);
		}
	}

	@Test(expectedExceptions = ReductionException.class)
	public void test() throws Exception {
		State state = State.initializeFrom(sapling);

		try {
			state = state.advance("1", callback);
		} catch (ReductionException e) {
			assertEquals(e.getCause().getClass(), MethodicException.class);
			throw e;
		}
	}

}
