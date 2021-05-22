package agency.highlysuspect.apathy.expr;

import net.minecraft.util.Unit;
import org.apache.commons.lang3.StringUtils;
import org.derive4j.Data;

import java.util.List;

@Data
abstract class Node {
	interface Cases<R> {
		// Anything not special.
		R atom(int start, int end, String val);
		
		// (a b c d) -> list of a, b, c, d
		R list(int start, int end, List<Node> entries);
		
		// the end of the file
		R eof();
	}
	
	public abstract <R> R match(Cases<R> cases);
	
	public final String show() {
		return Nodes.caseOf(this)
			.atom((start, end, val) -> val)
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
	
	public final void debugSpans(String original, int indent) {
		String asd = StringUtils.repeat('\t', indent);
		
		Nodes.caseOf(this)
			.atom((start, end, val) -> {
				System.out.println(asd + "atom  " + val);
				System.out.println(asd + "   => " + original.substring(start, end));
				return Unit.INSTANCE;
			})
			.list((start, end, entries) -> {
				System.out.println(asd + "list  " + show());
				System.out.println(asd + "   => " + original.substring(start, end));
				entries.forEach(e -> e.debugSpans(original, indent + 1));
				return Unit.INSTANCE;
			})
			.otherwiseEmpty();
	}
	
	public final String describe() {
		return Nodes.caseOf(this)
			.atom_("atom")
			.list_("list")
			.eof_("end of file");
	}
}
