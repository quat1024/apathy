package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.ApathyHell;
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
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements RuleSerializer<RuleSpecDebug> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecDebug rule, JsonObject json) {
			json.add("rule", ApathyHell.instance.writeRule(rule.rule));
			json.addProperty("message", rule.message);
		}
		
		@Override
		public RuleSpecDebug read(JsonObject json) {
			return new RuleSpecDebug(
				ApathyHell.instance.readRule(json.getAsJsonObject("rule")),
				json.getAsJsonPrimitive("message").getAsString()
			);
		}
	}
}
