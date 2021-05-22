package agency.highlysuspect.apathy.expr;

//Silly utility class
class StringCursor {
	public StringCursor(String input, int pos) {
		this.input = input;
		this.pos = pos;
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
		return Tokens.value(Span.from(here, pos), input.substring(here, pos));
	}
}
