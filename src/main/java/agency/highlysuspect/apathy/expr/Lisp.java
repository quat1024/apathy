package agency.highlysuspect.apathy.expr;

import net.minecraft.util.Unit;
import org.apache.commons.lang3.StringUtils;
import org.derive4j.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

//Surprise, it's a lisp!
//I haven't read enough SICP to make this a *good* lisp. There's no quoting, for example.
//
//Based a little on the Pratt parsing example in Rust by matklad
//https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html
public class Lisp {
	static class Lexer {
		public Lexer(String input) {
			StringCursor cursor = new StringCursor(input, 0);
			
			while(cursor.withinBounds()) {
				cursor.munchWhitespace();
				if(cursor.charAt() == '(') {
					cursor.skip();
					add(Tokens.leftParen(cursor.pos));
				} else if(cursor.charAt() == ')') {
					cursor.skip();
					add(Tokens.rightParen(cursor.pos));
				} else if(cursor.charAt() == '\'') {
					cursor.skip();
					add(Tokens.tick(cursor.pos));
				} else add(cursor.findStringToken());
			}
		}
		
		ArrayDeque<Token> tokens = new ArrayDeque<>();
		
		void add(Token token) {
			tokens.addLast(token);
		}
		
		Token peek() {
			@Nullable Token token = tokens.peekFirst();
			return token == null ? Tokens.eof() : token;
		}
		
		Token pop() {
			if(tokens.isEmpty()) return Tokens.eof();
			else return tokens.pop();
		}
	}
	
	static Result<Node, NodeParseError> parseNode(Lexer lexer) {
		//Cast here because I seem to have thoroughly stumped the type checker
		return (Result<Node, NodeParseError>) Tokens.caseOf(lexer.pop())
			.leftParen(idx -> {
				int start = idx;
				int end;
				List<Node> innards = new ArrayList<>();
				
				while(true) {
					Result<Node, NodeParseError> result = parseNode(lexer);
					if(result.isOk()) {
						innards.add(result.get());
						if(lexer.peek().isRightParen()) {
							end = lexer.pop().getEnd().get(); //Only partial for the EOF token
							break;
						}
						
						if(lexer.peek().isEof()) {
							return Results.err(NodeParseErrors.whileParsingExpression(start, NodeParseErrors.unexpectedEof()));
						}
					} else {
						return Results.err(NodeParseErrors.whileParsingExpression(start, result.getErr()));
					}
				}
				
				return Results.ok(Nodes.list(start, end, innards));
			})
			.rightParen(idx -> Results.err(NodeParseErrors.unexpectedRightParen(idx)))
			.tick(idx -> Results.caseOf(parseNode(lexer))
					.ok(node -> Results.ok(Nodes.tick(idx, Nodes.getEnd(node).get(), node)))
					.err(err -> Results.err(NodeParseErrors.whileParsingTick(idx, err))))
			.value((start, end, val) -> Results.ok(Nodes.symbol(start, end, val)))
			.eof(() -> Results.ok(Nodes.eof()));
	}
	
	@Data
	abstract static class Token {
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
				.leftParen(pos -> "Left Paren  ([pos " + pos + "]")
				.rightParen(pos -> "Right Paren )[pos " + pos + "]")
				.tick(pos -> "Tick        '[pos " + pos + "]")
				.value((start, end, val) -> "Value      \"" + val + "\" [start " + start + ", end " + end + "]")
				.eof_("[eof]");
		}
	}
	
	@Data
	abstract static class Node {
		interface Cases<R> {
			// Anything not special.
			R symbol(int start, int end, String val);
			// Anything starting with a '
			R tick(int start, int end, Node ticked);
			// (a b c d) -> list(a, b, c, d)
			R list(int start, int end, List<Node> entries);
			// the end of the file
			R eof();
		}
		
		public abstract <R> R match(Cases<R> cases);
		
		public final String show() {
			return Nodes.caseOf(this)
				.symbol((start, end, val) -> val)
				.tick((start, end, ticked) -> "'" + ticked.show())
				.list((start, end, entries) -> {
					StringBuilder owo = new StringBuilder("(");
					entries.forEach(node -> {
						owo.append(node.show());
						owo.append(" ");
					});
					return owo.substring(0, owo.length() - 1) + ")";
				})
				.eof_("");
		}
		
		public final String showMore(int indent) {
			String prefix = StringUtils.repeat('\t', indent);
			
			return prefix + Nodes.caseOf(this)
				.symbol((start, end, val) -> "Symbol \"" + val + "\" [start " + start + ", end " + end + "]")
				.tick((start, end, ticked) -> "Tick '\n" + ticked.showMore(indent + 1) + "[start " + start + ", end " + end + "]")
				.list((start, end, entries) -> {
					StringBuilder owo = new StringBuilder("Expr (\n");
					entries.forEach(node -> owo.append(node.showMore(indent + 1)));
					return owo.toString() + prefix + ") [start " + start + ", end " + end + "]";
				})
				.eof_("[eof]") + "\n";
		}
	}
	
	//What is this, Rust?
	@Data
	abstract static class Result<OK, ERR> {
		public abstract <R> R cata(Function<OK, R> ok, Function<ERR, R> err);
		
		public final boolean isOk() {
			return Results.getOk(this).isPresent();
		}
		
		public final OK get() {
			return Results.getOk(this).get();
		}
		
		public final boolean isErr() {
			return Results.getErr(this).isPresent();
		}
		
		public final ERR getErr() {
			return Results.getErr(this).get();
		}
	}
	
	@Data
	abstract static class NodeParseError {
		interface Cases<R> {
			R unexpectedEof();
			R unexpectedRightParen(int pos);
			R whileParsingExpression(int pos, NodeParseError next);
			R whileParsingTick(int pos, NodeParseError next);
		}
		
		public abstract <R> R match(Cases<R> cases);
		
		public final String show() {
			return NodeParseErrors.caseOf(this)
				.unexpectedEof(() -> "Unexpected end of file.")
				.unexpectedRightParen(pos -> "Unexpected right paren at character position " + pos + ".")
				.whileParsingExpression((pos, next) -> "Error while parsing expression starting at " + pos + ": " + next.show())
				.whileParsingTick((pos, next) -> "Error while parsing tick at " + pos + ": " + next.show());
		}
	}
	
	//Silly utility class
	static class StringCursor {
		public StringCursor(String input, int pos) {
			this.input = input;
			this.pos = pos;
		}
		
		public StringCursor copy() {
			return new StringCursor(input, pos);
		}
		
		final String input;
		int pos;
		
		char charAt() {
			return input.charAt(pos);
		}
		
		boolean withinBounds() {
			return pos < input.length();
		}
		
		boolean atEnd() {
			return pos == input.length();
		}
		
		boolean atWhitespace() {
			return charAt() <= ' '; //used by string#trim
		}
		
		void skip() {
			pos++;
		}
		
		void munchWhitespace() {
			while(withinBounds() && atWhitespace()) skip();
		}
		
		//Parses until it finds non-whitespace or a ) character.
		Token findStringToken() {
			int here = pos;
			while(withinBounds() && !atWhitespace() && charAt() != ')') skip();
			return Tokens.value(here, pos, input.substring(here, pos));
		}
	}
	
	public static void main(String[] args) {
		Lexer uwu = new Lexer("(1 '2 3 '(4 5)) (6 '''7 8 '(9 10))");
		uwu.tokens.stream().map(Token::show).forEach(System.out::println);
		System.out.println("---");
		uwu.tokens.stream().map(Token::showMore).forEach(System.out::println);
		System.out.println("---");
		
		while(true) {
			Result<Node, NodeParseError> res = parseNode(uwu);
			if(Results.caseOf(res)
				.ok(node -> {
					System.out.println("Successful parse");
					System.out.println(node.show());
					System.out.println(node.showMore(0));
					return Nodes.caseOf(node).eof_(true).otherwise_(false);
				})
				.err(err -> {
					System.out.println("Unsuccessful parse.");
					System.out.println(err.show());
					return true;
				})) break;
		}
	}
}
