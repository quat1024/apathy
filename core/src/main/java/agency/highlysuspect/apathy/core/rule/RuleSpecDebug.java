package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public class RuleSpecDebug implements Spec<Rule, RuleSpecDebug> {
	public RuleSpecDebug(Spec<Rule, ?> rule, String message) {
		this.rule = rule;
		this.message = message;
	}
	
	public final Spec<Rule, ?> rule;
	public final String message;
	
	@Override
	public Spec<Rule, ?> optimize() {
		return new RuleSpecDebug(rule.optimize(), message);
	}
	
	@Override
	public Rule build() {
		Rule built = rule.build();
		
		return (attacker, defender) -> {
			Apathy.instance.log.warn("rule: " + message);
			TriState result = built.apply(attacker, defender);
			Apathy.instance.log.warn("returned: " + result.toAllowDenyPassString());
			return result;
		};
	}
	
	@Override
	public JsonSerializer<RuleSpecDebug> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<RuleSpecDebug> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecDebug thing, JsonObject json) {
			json.add("rule", Apathy.instance.writeRule(thing.rule));
			json.addProperty("message", thing.message);
		}
		
		@Override
		public RuleSpecDebug read(JsonObject json) {
			return new RuleSpecDebug(
				Apathy.instance.readRule(json.getAsJsonObject("rule")),
				json.getAsJsonPrimitive("message").getAsString()
			);
		}
	}
}
