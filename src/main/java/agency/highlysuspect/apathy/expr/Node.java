package agency.highlysuspect.apathy.expr;

import net.minecraft.util.Unit;
import org.apache.commons.lang3.StringUtils;
import org.derive4j.Data;

import java.util.List;
import java.util.Optional;

@Data
@SuppressWarnings("OptionalUsedAsFieldOrParameterType") //bad convention!
abstract class Node {
	interface Cases<R> {
		// Anything not special.
		R atom(Optional<Span> source, String val);
		
		// (a b c d) -> list of a, b, c, d
		R list(Optional<Span> source, List<Node> entries);
	}
	
	public abstract String toString();
	public abstract <R> R match(Cases<R> cases);
	
	public final String show() {
		return Nodes.caseOf(this)
			.atom((span, val) -> val)
			.list((span, entries) -> {
				StringBuilder owo = new StringBuilder("(");
				entries.forEach(node -> {
					owo.append(node.show());
					owo.append(" ");
				});
				return owo.substring(0, owo.length() - 1) + ")";
			});
	}
	
	public final void debugSpans(String original, int indent) {
		String asd = StringUtils.repeat('\t', indent);
		
		Nodes.caseOf(this)
			.atom((span, val) -> {
				System.out.println(asd + "atom  " + val);
				if(span.isPresent()) {
					System.out.println(asd + "   => " + span.get().cut(original));
				} else {
					System.out.println(asd + "   =>  (no span)");
				}
				return Unit.INSTANCE;
			})
			.list((span, entries) -> {
				System.out.println(asd + "list  " + show());
				if(span.isPresent()) {
					System.out.println(asd + "   => " + span.get().cut(original));
				} else {
					System.out.println(asd + "   =>  (no span)");
				}
				entries.forEach(e -> e.debugSpans(original, indent + 1));
				return Unit.INSTANCE;
			});
	}
	
	public final String describe() {
		return Nodes.caseOf(this)
			.atom_("atom")
			.list_("list");
	}
}
