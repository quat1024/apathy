package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.platform.TriState;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ChainRuleSpec(List<RuleSpec> rules) implements RuleSpec {
	public static final Codec<ChainRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.RULE_SPEC_CODEC.listOf().fieldOf("rules").forGetter(x -> x.rules)
	).apply(i, ChainRuleSpec::new));
	
	@Override
	public RuleSpec optimize() {
		//TODO: flatten multiple layers of ChainRuleSpecs, maybe?
		
		List<RuleSpec> optimizedRules = rules.stream().map(RuleSpec::optimize).collect(Collectors.toList());
		
		if(optimizedRules.size() == 0) return AlwaysRuleSpec.ALWAYS_PASS;
		else if(optimizedRules.size() == 1) return optimizedRules.get(0);
		else if(optimizedRules.get(0) == AlwaysRuleSpec.ALWAYS_ALLOW) return AlwaysRuleSpec.ALWAYS_ALLOW;
		else if(optimizedRules.get(0) == AlwaysRuleSpec.ALWAYS_DENY) return AlwaysRuleSpec.ALWAYS_DENY;
		
		List<RuleSpec> filteredRules = new ArrayList<>();
		for(RuleSpec spec : optimizedRules) {
			if(spec == AlwaysRuleSpec.ALWAYS_PASS) continue;
			filteredRules.add(spec);
			if(spec == AlwaysRuleSpec.ALWAYS_ALLOW) break;
			if(spec == AlwaysRuleSpec.ALWAYS_DENY) break;
		}
		
		if(filteredRules.size() == 0) return AlwaysRuleSpec.ALWAYS_PASS;
		else if(filteredRules.size() == 1) return filteredRules.get(0);
		
		else return new ChainRuleSpec(filteredRules);
	}
	
	@Override
	public Rule build() {
		Rule[] built = rules.stream().map(RuleSpec::build).toArray(Rule[]::new);
		return (attacker, defender) -> {
			for(Rule rule : built) {
				TriState result = rule.apply(attacker, defender);
				if(result != TriState.DEFAULT) return result;
			}
			return TriState.DEFAULT;
		};
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
