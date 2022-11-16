package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.Rule;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DebugRuleSpec(RuleSpec<?> rule, String message) implements RuleSpec<DebugRuleSpec> {
	@Override
	public RuleSpec<?> optimize() {
		return new DebugRuleSpec(rule.optimize(), message);
	}
	
	@Override
	public Rule build() {
		Rule built = rule.build();
		
		return (attacker, defender) -> {
			ApathyHell.instance.log.warn("rule: " + message);
			TriState result = built.apply(attacker, defender);
			ApathyHell.instance.log.warn("returned: " + result.toAllowDenyPassString());
			return result;
		};
	}
	
	@Override
	public RuleSerializer<DebugRuleSpec> getSerializer() {
		return DebugRuleSerializer.INSTANCE;
	}
	
	public static class DebugRuleSerializer implements RuleSerializer<DebugRuleSpec> {
		public static final DebugRuleSerializer INSTANCE = new DebugRuleSerializer();
		
		@Override
		public JsonObject write(DebugRuleSpec rule, JsonObject json) {
			json.add("rule", Apathy119.instance119.writeRule(rule.rule));
			json.addProperty("message", rule.message);
			return json;
		}
		
		@Override
		public DebugRuleSpec read(JsonObject json) {
			return new DebugRuleSpec(
				Apathy119.instance119.readRule(json.getAsJsonObject("rule")),
				json.getAsJsonPrimitive("message").getAsString()
			);
		}
	}
	
	///CODEC HELLZONE///
	
	@Deprecated(forRemoval = true)
	public static final Codec<DebugRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.RULE_SPEC_CODEC.fieldOf("rule").forGetter(x -> x.rule),
		Codec.STRING.fieldOf("message").forGetter(x -> x.message)
	).apply(i, DebugRuleSpec::new));
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends RuleSpec<?>> codec() {
		return CODEC;
	}
}
