package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.Rule;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

public final record JsonRuleSpec() implements RuleSpec<JsonRuleSpec> {
	public static final JsonRuleSpec INSTANCE = new JsonRuleSpec();
	
	@Override
	public Rule build() {
		return (attacker, defender) -> Apathy119.instance119.jsonRule == null ? TriState.DEFAULT : Apathy119.instance119.jsonRule.apply(attacker, defender);
	}
	
	@Override
	public RuleSerializer<JsonRuleSpec> getSerializer() {
		return JsonRuleSerializer.INSTANCE;
	}
	
	public static class JsonRuleSerializer implements RuleSerializer<JsonRuleSpec> {
		public static final JsonRuleSerializer INSTANCE = new JsonRuleSerializer();
		
		@Override
		public JsonObject write(JsonRuleSpec jsonRuleSpec, JsonObject json) {
			return json;
		}
		
		@Override
		public JsonRuleSpec read(JsonObject json) {
			return JsonRuleSpec.INSTANCE;
		}
	}
	
	///CODEC HELLZONE///
	@Deprecated(forRemoval = true)
	public static final Codec<JsonRuleSpec> CODEC = Codec.unit(INSTANCE);
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends RuleSpec<?>> codec() {
		return CODEC;
	}
}
