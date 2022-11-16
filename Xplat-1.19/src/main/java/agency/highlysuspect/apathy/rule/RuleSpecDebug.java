package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import com.google.gson.JsonObject;

public record RuleSpecDebug(RuleSpec<?> rule, String message) implements RuleSpec<RuleSpecDebug> {
	@Override
	public RuleSpec<?> optimize() {
		return new RuleSpecDebug(rule.optimize(), message);
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
	public RuleSerializer<RuleSpecDebug> getSerializer() {
		return DebugRuleSerializer.INSTANCE;
	}
	
	public static class DebugRuleSerializer implements RuleSerializer<RuleSpecDebug> {
		public static final DebugRuleSerializer INSTANCE = new DebugRuleSerializer();
		
		@Override
		public JsonObject write(RuleSpecDebug rule, JsonObject json) {
			json.add("rule", Apathy119.instance119.writeRule(rule.rule));
			json.addProperty("message", rule.message);
			return json;
		}
		
		@Override
		public RuleSpecDebug read(JsonObject json) {
			return new RuleSpecDebug(
				Apathy119.instance119.readRule(json.getAsJsonObject("rule")),
				json.getAsJsonPrimitive("message").getAsString()
			);
		}
	}
}
