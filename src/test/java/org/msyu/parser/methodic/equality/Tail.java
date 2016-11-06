package org.msyu.parser.methodic.equality;

import org.msyu.parser.methodic.SequenceStruct;

import java.util.List;

interface Tail<T extends List<Condition>> extends SequenceStruct {
	T terms();

	Lock mk(T terms);
	interface Lock extends Tail<List<Condition>> { }
}
