package agency.highlysuspect.apathy.expr;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//Based a little on the Pratt parsing example in Rust by matklad
//https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html
//...but much messier b/c Java and derive4j jank.
class Lexer {
	public Lexer(String input) {
		StringCursor cursor = new StringCursor(input, 0);
		
		while(cursor.withinBounds()) {
			cursor.munchWhitespace();
			if(cursor.charAt() == '(') {
				add(Tokens.leftParen(Span.fromChar(cursor.pos)));
				cursor.skip();
			} else if(cursor.charAt() == ')') {
				add(Tokens.rightParen(Span.fromChar(cursor.pos)));
				cursor.skip();
			} else if(cursor.charAt() == '\'') {
				add(Tokens.tick(Span.fromChar(cursor.pos)));
				cursor.skip();
			} else add(cursor.findStringToken());
		}
	}
	
	ArrayDeque<Token> tokens = new ArrayDeque<>();
	
	void add(Token token) {
		tokens.addLast(token);
	}
	
	Optional<Token> peek() {
		@Nullable Token token = tokens.peekFirst();
		return token == null ? Optional.empty() : Optional.of(token);
	}
	
	Optional<Token> pop() {
		if(tokens.isEmpty()) return Optional.empty();
		else return Optional.of(tokens.pop());
	}
	
	//Ok(Some(node)) -> Here's a node.
	//Ok(None)       -> I found the end of the file, but it was expected, and not an error.
	//Err(err)       -> I ran into a problem parsing a node.
	Result<Optional<Node>, NodeParseError> parseNode() {
		Optional<Token> opToken = pop();
		if(!opToken.isPresent()) return Results.ok(Optional.empty());
		else return Tokens.caseOf(opToken.get())
			.leftParen(span -> list(span).mapOk(Optional::of))
			.rightParen(span -> Results.err(NodeParseErrors.unexpectedRightParen(span)))
			.tick(span -> Results.caseOf(parseNode())
				.ok(node -> node
					//I seem to have *thoroughly* stumped the type checker, doesn't compile without this ridiculous cast.
					.map(value -> (Result<Optional<Node>, NodeParseError>) (Object) Results.ok(Optional.of(buildQuoteFromTick(span, value))))
					.orElseGet(() -> Results.err(NodeParseErrors.whileParsingQuote(span, NodeParseErrors.unexpectedEof()))))
				.err(err -> Results.err(NodeParseErrors.whileParsingQuote(span, err))))
			.value((span, val) -> Results.ok(Optional.of(Nodes.atom(Optional.of(span), val))));
	}
	
	private Result<Node, NodeParseError> list(Span start) {
		List<Node> innards = new ArrayList<>();
		
		while(true) {
			//This is wacky.
			//Either.left(Optional.of(span)) - Found the ), returned span is the whole span for this list expression.
			//Either.left(Optional.empty())  - Haven't found the ) yet, keep parsing.
			//Either.right(error)            - Found an error parsing the expression, stop parsing.
			//
			//Basically this is all because i can't return from the whole function with an error, inside a caseof...
			//Can I do that in Java 16? please tell me I can do that in java 16.
			//Oh also I need this cast, because I blew up the typechecker again.
			Result<Optional<Span>, NodeParseError> wack = (Result<Optional<Span>, NodeParseError>) Results.caseOf(parseNode())
				.ok(opNode -> {
					if(opNode.isPresent()) {
						innards.add(opNode.get());
						
						Optional<Token> maybeNextToken = peek();
						if(maybeNextToken.isPresent()) {
							Token nextToken = maybeNextToken.get();
							if(nextToken.isRightParen()) {
								pop(); //consume the right paren
								return Results.ok(Optional.of(Span.containingBoth(start, Tokens.getSpan(nextToken))));
							} else {
								return Results.ok(Optional.empty());
							}
						} else {
							return Results.err(NodeParseErrors.unterminatedExpression(start));
						}
					} else {
						return Results.err(NodeParseErrors.whileParsingExpression(start, NodeParseErrors.unexpectedEof()));
					}
				}).err(Results::err);
			
			//rust question mark operator:
			if(wack.isErr()) {
				return Results.err(wack.getErr());
			}
			
			//and the span logic.
			Optional<Span> maybeSpan = wack.getOk();
			if(maybeSpan.isPresent()) {
				return Results.ok(Nodes.list(maybeSpan, innards));
			}
		}
	}
	
	private Node buildQuoteFromTick(Span tickSpan, Node quoted) {
		ArrayList<Node> contents = new ArrayList<>();
		contents.add(Nodes.atom(Optional.of(tickSpan), "quote"));
		contents.add(quoted);
		return Nodes.list(Optional.of(Span.containingBoth(tickSpan, Nodes.getSource(quoted).get())), contents);
	}
}
