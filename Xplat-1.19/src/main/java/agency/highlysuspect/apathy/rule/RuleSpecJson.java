package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import com.google.gson.JsonObject;

public final record RuleSpecJson() implements RuleSpec<RuleSpecJson> {
	public static final RuleSpecJson INSTANCE = new RuleSpecJson();
	
	@Override
	public Rule build() {
		return (attacker, defender) -> Apathy119.instance119.jsonRule == null ? TriState.DEFAULT : Apathy119.instance119.jsonRule.apply(attacker, defender);
	}
	
	@Override
	public RuleSerializer<RuleSpecJson> getSerializer() {
		return JsonRuleSerializer.INSTANCE;
	}
	
	public static class JsonRuleSerializer implements RuleSerializer<RuleSpecJson> {
		public static final JsonRuleSerializer INSTANCE = new JsonRuleSerializer();
		
		@Override
		public JsonObject write(RuleSpecJson ruleSpecJson, JsonObject json) {
			return json;
		}
		
		@Override
		public RuleSpecJson read(JsonObject json) {
			return RuleSpecJson.INSTANCE;
		}
	}
}
