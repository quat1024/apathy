package agency.highlysuspect.apathy.expr;

import java.util.Optional;

//Surprise, it's a lisp!
//I haven't read enough SICP to make this a *good* lisp.
public class Lisp {
	public static void main(String[] args) {
		String input = "(hello 'world (how '''are (you)))";
		System.out.println("Lexing " + input);
		Lexer uwu = new Lexer(input);
		uwu.tokens.stream().map(Token::showMore).forEach(System.out::println);
		System.out.println("---");
		System.out.println("Parsing " + input);
		while(true) {
			Result<Optional<Node>, NodeParseError> res = uwu.parseNode();
			if(Results.caseOf(res)
				.ok(opNode -> {
					if(opNode.isPresent()) {
						System.out.println("Successful parse:");
						opNode.get().debugSpans(input, 0);
						return false;
					} else {
						System.out.println("Successful EOF.");
						return true;
					}
				})
				.err(err -> {
					System.out.println("Parse error:");
					System.out.println(err.show(input, 0));
					return true;
				})) break;
		}
	}
}
