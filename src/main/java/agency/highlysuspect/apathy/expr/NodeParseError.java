package agency.highlysuspect.apathy.expr;

import org.apache.commons.lang3.StringUtils;
import org.derive4j.Data;

@Data
abstract class NodeParseError {
	interface Cases<R> {
		R unexpectedEof();
		R unterminatedExpression(Span span);
		R unexpectedRightParen(Span span);
		R whileParsingExpression(Span span, NodeParseError next);
		R whileParsingQuote(Span span, NodeParseError next);
		R expectedExpressionFound(Span span, Node what);
	}
	
	public abstract <R> R match(Cases<R> cases);
	
	public final String show(String original, int indent) {
		String prefix = StringUtils.repeat('\t', indent);
		
		return prefix + NodeParseErrors.caseOf(this)
			.unexpectedEof(() -> "Unexpected end of file.")
			.unterminatedExpression((span) -> "Left paren at " + span.toLineAndCol(original) + " does not have a matching right paren.")
			.unexpectedRightParen(span -> "Unexpected right paren at " + span.toLineAndCol(original) + ".")
			.whileParsingExpression((span, next) -> "Error while parsing expression at " + span.toLineAndCol(original) + ": \n" + next.show(original, indent + 1))
			.whileParsingQuote((span, next) -> "Error while parsing quote at " + span.toLineAndCol(original) + ": \n" + next.show(original, indent + 1))
			.expectedExpressionFound((span, expr) -> "Expected an expression on " + span.toLineAndCol(original) + ", but found a " + expr.describe() + ".");
	}
}
