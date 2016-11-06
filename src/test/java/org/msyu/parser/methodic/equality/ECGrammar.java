package org.msyu.parser.methodic.equality;

import org.msyu.parser.methodic.EnumLimiters;
import org.msyu.parser.methodic.Production;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.msyu.parser.methodic.equality.Keyword.AND;
import static org.msyu.parser.methodic.equality.Keyword.CLOSE_PAREN;
import static org.msyu.parser.methodic.equality.Keyword.EQ;
import static org.msyu.parser.methodic.equality.Keyword.NEQ;
import static org.msyu.parser.methodic.equality.Keyword.NOT;
import static org.msyu.parser.methodic.equality.Keyword.OPEN_PAREN;
import static org.msyu.parser.methodic.equality.Keyword.OR;

@EnumLimiters(Keywords.class)
interface ECGrammar {

	@Production
	default Equality idEqualsNumber(Identifier id, @Keywords({EQ, NEQ}) Keyword op, Number num) {
		return new Equality(id, op == NEQ, num);
	}

	@Production
	default Equality idEqualsString(Identifier id, @Keywords({EQ, NEQ}) Keyword op, String str) {
		return new Equality(id, op == NEQ, str);
	}


	@Production
	default Negation negation(@Keywords(NOT) Keyword not, Condition condition) {
		return new Negation(condition);
	}


	@Production
	default Conjunction conjunction(Condition condition, Tail.Lock tail) {
		return new Conjunction(prependCondition(condition, tail.terms()));
	}

	@Production
	default Tail.Lock conjTailShort(@Keywords(AND) Keyword and, Condition condition) {
		return tailCtor().mk(Collections.singletonList(condition));
	}

	@Production
	default Tail.Lock conjTailLong(@Keywords(AND) Keyword and, Condition condition, Tail.Lock tail) {
		return tail.mk(prependCondition(condition, tail.terms()));
	}

	@Production
	default Disjunction disjunction(Condition condition, Tail.Lock tail) {
		return new Disjunction(prependCondition(condition, tail.terms()));
	}

	@Production
	default Tail.Lock disjTailShort(@Keywords(OR) Keyword or, Condition condition) {
		return tailCtor().mk(Collections.singletonList(condition));
	}

	@Production
	default Tail.Lock disjTailLong(@Keywords(OR) Keyword or, Condition condition, Tail.Lock tail) {
		return tail.mk(prependCondition(condition, tail.terms()));
	}

	Tail.Lock tailCtor();

	default List<Condition> prependCondition(Condition c, List<Condition> l) {
		return Stream.concat(Stream.of(c), l.stream()).collect(toList());
	}


	@Production
	default Condition exprInParentheses(@Keywords(OPEN_PAREN) Keyword open, Condition condition, @Keywords(CLOSE_PAREN) Keyword close) {
		return condition;
	}

}
