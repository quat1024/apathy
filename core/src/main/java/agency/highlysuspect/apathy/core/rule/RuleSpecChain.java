package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RuleSpecChain implements RuleSpec<RuleSpecChain> {
	public RuleSpecChain(List<RuleSpec<?>> rules) {
		this.rules = rules;
	}
	
	public final List<RuleSpec<?>> rules;
	
	@Override
	public RuleSpec<?> optimize() {
		//TODO: flatten multiple layers of ChainRuleSpecs, maybe?
		
		List<RuleSpec<?>> optimizedRules = rules.stream().map(RuleSpec::optimize).collect(Collectors.toList());
		
		if(optimizedRules.size() == 0) return RuleSpecAlways.ALWAYS_PASS;
		else if(optimizedRules.size() == 1) return optimizedRules.get(0);
		else if(optimizedRules.get(0) == RuleSpecAlways.ALWAYS_ALLOW) return RuleSpecAlways.ALWAYS_ALLOW;
		else if(optimizedRules.get(0) == RuleSpecAlways.ALWAYS_DENY) return RuleSpecAlways.ALWAYS_DENY;
		
		List<RuleSpec<?>> filteredRules = new ArrayList<>();
		for(RuleSpec<?> spec : optimizedRules) {
			if(spec == RuleSpecAlways.ALWAYS_PASS) continue;
			filteredRules.add(spec);
			if(spec == RuleSpecAlways.ALWAYS_ALLOW) break;
			if(spec == RuleSpecAlways.ALWAYS_DENY) break;
		}
		
		if(filteredRules.size() == 0) return RuleSpecAlways.ALWAYS_PASS;
		else if(filteredRules.size() == 1) return filteredRules.get(0);
		
		else return new RuleSpecChain(filteredRules);
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
	public RuleSerializer<RuleSpecChain> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements RuleSerializer<RuleSpecChain> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecChain rule, JsonObject json) {
			JsonArray rulesArray = new JsonArray();
			for(RuleSpec<?> ruleToWrite : rule.rules) {
				rulesArray.add(Apathy.instance.writeRule(ruleToWrite));
			}
			json.add("rules", rulesArray);
		}
		
		@Override
		public RuleSpecChain read(JsonObject json) {
			JsonArray rulesArray = json.getAsJsonArray("rules");
			ArrayList<RuleSpec<?>> rules = new ArrayList<>();
			for(JsonElement e : rulesArray) rules.add(Apathy.instance.readRule(e.getAsJsonObject()));
			return new RuleSpecChain(rules);
		}
	}
}
