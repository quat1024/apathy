package agency.highlysuspect.apathy.expr;

public class Span {
	public Span(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	final int start;
	final int end;
	
	public static Span from(int start, int end) {
		return new Span(start, end);
	}
	
	public static Span fromChar(int pos) {
		return new Span(pos, pos + 1);
	}
	
	public static Span containingBoth(Span a, Span b) {
		return new Span(Math.min(a.start, b.start), Math.max(a.end, b.end));
	}
	
	public String cut(String input) {
		return input.substring(start, end);
	}
	
	public String toLineAndCol(String input) {
		//in real life i'd probably return a pair<int, int> or something
		int line = 1, col = 1;
		for(int i = 0; i < start; i++) {
			if(input.charAt(i) == '\n') {
				line++;
				col = 1;
			} else {
				col++;
			}
		}
		
		return "line " + line + " col " + col;
	}
	
	@Override
	public String toString() {
		return "[" + start + "-" + end + "]";
	}
}
