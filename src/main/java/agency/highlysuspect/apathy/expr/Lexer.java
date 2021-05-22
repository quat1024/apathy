package agency.highlysuspect.apathy.expr;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

class Lexer {
	public Lexer(String input) {
		StringCursor cursor = new StringCursor(input, 0);
		
		while(cursor.withinBounds()) {
			cursor.munchWhitespace();
			if(cursor.charAt() == '(') {
				add(Tokens.leftParen(cursor.pos));
				cursor.skip();
			} else if(cursor.charAt() == ')') {
				add(Tokens.rightParen(cursor.pos));
				cursor.skip();
			} else if(cursor.charAt() == '\'') {
				add(Tokens.tick(cursor.pos));
				cursor.skip();
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
	
	Result<Node, NodeParseError> parseNode() {
		return Tokens.caseOf(pop())
			.leftParen(this::list)
			.rightParen(idx -> Results.err(NodeParseErrors.unexpectedRightParen(idx)))
			.tick(idx -> Results.caseOf(parseNode())
				//I seem to have *thoroughly* stumped the type checker, doesn't compile without this
				.ok(node -> (Result<Node, NodeParseError>) (Object) Results.ok(buildQuoteFromTick(idx, node)))
				.err(err -> Results.err(NodeParseErrors.whileParsingTick(idx, err))))
			.value((start, end, val) -> Results.ok(Nodes.atom(start, end, val)))
			.eof(() -> Results.ok(Nodes.eof()));
	}
	
	private Result<Node, NodeParseError> list(int start) {
		int end;
		List<Node> innards = new ArrayList<>();
		
		while(true) {
			Result<Node, NodeParseError> result = parseNode();
			if(result.isOk()) {
				innards.add(result.get());
				if(peek().isRightParen()) {
					end = pop().getEnd().get(); //Only partial for the EOF token
					break;
				}
				
				if(peek().isEof()) {
					return Results.err(NodeParseErrors.unterminatedExpression(start));
				}
			} else {
				return Results.err(NodeParseErrors.whileParsingExpression(start, result.getErr()));
			}
		}
		
		return Results.ok(Nodes.list(start, end, innards));
	}
	
	private Node buildQuoteFromTick(int tickStart, Node quoted) {
		ArrayList<Node> contents = new ArrayList<>();
		contents.add(Nodes.atom(tickStart, tickStart + 1, "quote"));
		contents.add(quoted);
		return Nodes.list(tickStart, Nodes.getEnd(quoted).get(), contents);
	}
}
