package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public class RuleSpecDebug implements RuleSpec<RuleSpecDebug> {
	public RuleSpecDebug(RuleSpec<?> rule, String message) {
		this.rule = rule;
		this.message = message;
	}
	
	public final RuleSpec<?> rule;
	public final String message;
	
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
