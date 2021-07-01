package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ChainSpec extends RuleSpec {
	public ChainSpec(List<RuleSpec> rules) {
		this.rules = rules;
	}
	
	private final List<RuleSpec> rules;
	
	public static final Codec<ChainSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		RuleSpec.SPEC_CODEC.listOf().fieldOf("rules").forGetter(x -> x.rules)
	).apply(i, ChainSpec::new));
	
	@Override
	public Rule buildRule() {
		return Rule.chainMany(rules.stream().map(RuleSpec::buildRule).collect(Collectors.toList()));
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
