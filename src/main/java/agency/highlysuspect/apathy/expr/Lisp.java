package agency.highlysuspect.apathy.expr;

//Surprise, it's a lisp!
//I haven't read enough SICP to make this a *good* lisp. There's no quoting, for example.
//
//Based a little on the Pratt parsing example in Rust by matklad
//https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html
public class Lisp {
	public static void main(String[] args) {
		String input = "(hello 'world (how '''are (you)))";
		System.out.println("Lexing " + input);
		Lexer uwu = new Lexer(input);
		uwu.tokens.stream().map(Token::showMore).forEach(System.out::println);
		System.out.println("---");
		System.out.println("Parsing " + input);
		while(true) {
			Result<Node, NodeParseError> res = uwu.parseNode();
			if(Results.caseOf(res)
				.ok(node -> {
					System.out.println("Successful parse:");
					node.debugSpans(input, 0);
					return Nodes.caseOf(node).eof_(true).otherwise_(false);
				})
				.err(err -> {
					System.out.println("Unsuccessful parse.");
					System.out.println(err.show(input, 0));
					return true;
				})) break;
		}
	}
}
