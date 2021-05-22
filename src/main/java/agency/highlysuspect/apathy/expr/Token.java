package agency.highlysuspect.apathy.expr;

import org.derive4j.Data;

import java.util.Optional;
import java.util.function.Function;

@Data
abstract class Token {
	interface Cases<R> {
		// (
		R leftParen(int pos);
		// )
		R rightParen(int pos);
		// '
		R tick(int pos);
		// a string, not delimited with anything in particular
		R value(int start, int end, String val);
		// the end of the file
		R eof();
	}
	
	public abstract <R> R match(Cases<R> cases);
	
	//augh
	public final boolean isRightParen() {
		return Tokens.caseOf(this).rightParen_(true).otherwise_(false);
	}
	
	public final boolean isEof() {
		return Tokens.caseOf(this).eof_(true).otherwise_(false);
	}
	
	//Needed because sometimes the variables are named "pos" and other times "start" or "end"
//		public final Optional<Integer> getStart() {
//			return Tokens.caseOf(this)
//				.leftParen(Function.identity())
//				.rightParen(Function.identity())
//				.tick(Function.identity())
//				.value((start, end, val) -> start)
//				.otherwiseEmpty();
//		}
	
	public final Optional<Integer> getEnd() {
		return Tokens.caseOf(this)
			.leftParen(Function.identity())
			.rightParen(Function.identity())
			.tick(Function.identity())
			.value((start, end, val) -> end)
			.otherwiseEmpty();
	}
	
	public final String show() {
		return Tokens.caseOf(this)
			.leftParen_("(")
			.rightParen_(")")
			.tick_("'")
			.value((start, end, val) -> val)
			.eof_("[eof]");
	}
	
	public final String showMore() {
		return Tokens.caseOf(this)
			.leftParen(pos -> "Left Paren  ( [pos " + pos + "]")
			.rightParen(pos -> "Right Paren ) [pos " + pos + "]")
			.tick(pos -> "Tick        ' [pos " + pos + "]")
			.value((start, end, val) -> "Value       " + val + " [start " + start + ", end " + end + "]")
			.eof_("[eof]");
	}
}
