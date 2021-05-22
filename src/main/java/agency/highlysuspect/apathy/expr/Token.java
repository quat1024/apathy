package agency.highlysuspect.apathy.expr;

import org.derive4j.Data;

@Data
abstract class Token {
	interface Cases<R> {
		// (
		R leftParen(Span span);
		// )
		R rightParen(Span span);
		// '
		R tick(Span span);
		// a string, not delimited with anything in particular
		R value(Span span, String val);
	}
	
	public abstract String toString();
	public abstract <R> R match(Cases<R> cases);
	
	//augh
	public final boolean isRightParen() {
		return Tokens.caseOf(this).rightParen_(true).otherwise_(false);
	}
	
	public final String show() {
		return Tokens.caseOf(this)
			.leftParen_("(")
			.rightParen_(")")
			.tick_("'")
			.value((span, val) -> val);
	}
	
	public final String showMore() {
		return Tokens.caseOf(this)
			.leftParen(span -> "Left Paren  (\t" + span)
			.rightParen(span -> "Right Paren )\t" + span)
			.tick(span -> "Tick        '\t" + span)
			.value((span, val) -> "Value       " + val + "\t" + span);
	}
}
