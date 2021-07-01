package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChainRuleSpec implements RuleSpec {
	public ChainRuleSpec(List<RuleSpec> rules) {
		this.rules = rules;
	}
	
	private final List<RuleSpec> rules;
	
	public static final Codec<ChainRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.RULE_SPEC_CODEC.listOf().fieldOf("rules").forGetter(x -> x.rules)
	).apply(i, ChainRuleSpec::new));
	
	@Override
	public Rule build() {
		//TODO: flatten multiple layers of ChainRuleSpecs.
		
		List<Rule> built = rules.stream().map(RuleSpec::build).collect(Collectors.toList());
		
		if(built.size() == 0) return Rule.ALWAYS_PASS;
		else if(built.size() == 1) return built.get(0);
		else if(built.get(0) == Rule.ALWAYS_ALLOW) return Rule.ALWAYS_ALLOW;
		else if(built.get(0) == Rule.ALWAYS_DENY) return Rule.ALWAYS_DENY;
		
		List<Rule> filteredBuiltRules = new ArrayList<>();
		for(Rule rule : built) {
			if(rule == Rule.ALWAYS_PASS) continue;
			filteredBuiltRules.add(rule);
			if(rule == Rule.ALWAYS_ALLOW) break;
			if(rule == Rule.ALWAYS_DENY) break;
		}
		
		Rule[] rulesArray = filteredBuiltRules.toArray(new Rule[0]);
		return (attacker, defender) -> {
			for(Rule rule : rulesArray) {
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
