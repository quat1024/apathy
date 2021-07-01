package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;

public class ClojureSpec extends RuleSpec {
	public static final ClojureSpec INSTANCE = new ClojureSpec();
	public static final Codec<ClojureSpec> CODEC = Codec.unit(INSTANCE);
	
	@Override
	public Rule buildRule() {
		return Rule.clojure();
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
