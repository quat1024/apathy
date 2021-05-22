package agency.highlysuspect.apathy.expr;

import org.apache.commons.lang3.StringUtils;
import org.derive4j.Data;

@Data
abstract class NodeParseError {
	interface Cases<R> {
		R unexpectedEof();
		R unterminatedExpression(int pos);
		R unexpectedRightParen(int pos);
		R whileParsingExpression(int pos, NodeParseError next);
		R whileParsingQuote(int pos, NodeParseError next);
		R expectedExpressionFound(int pos, Node what);
	}
	
	public abstract <R> R match(Cases<R> cases);
	
	public final String show(String original, int indent) {
		String prefix = StringUtils.repeat('\t', indent);
		
		return prefix + NodeParseErrors.caseOf(this)
			.unexpectedEof(() -> "Unexpected end of file.")
			.unterminatedExpression((pos) -> "Left paren at " + nice(original, pos) + " does not have a matching right paren.")
			.unexpectedRightParen(pos -> "Unexpected right paren at " + nice(original, pos) + ".")
			.whileParsingExpression((pos, next) -> "Error while parsing expression at " + nice(original, pos) + ": \n" + next.show(original, indent + 1))
			.whileParsingTick((pos, next) -> "Error while parsing quote at " + nice(original, pos) + ": \n" + next.show(original, indent + 1))
			.expectedExpressionFound((pos, expr) -> "Expected an expression on " + nice(original, pos) + ", but found a " + expr.describe() + ".");
	}
	
	public final String nice(String input, int idx) {
		//in real life i'd probably return a pair<int, int> or something
		int line = 1, col = 1;
		for(int i = 0; i < idx; i++) {
			if(input.charAt(i) == '\n') {
				line++;
				col = 1;
			} else {
				col++;
			}
		}
		
		return "line " + line + " col " + col;
	}
}
