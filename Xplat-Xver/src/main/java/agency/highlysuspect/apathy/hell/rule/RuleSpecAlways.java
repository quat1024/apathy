package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.TriState;
import com.google.gson.JsonObject;

public record RuleSpecAlways(TriState value) implements RuleSpec<RuleSpecAlways> {
	public static final RuleSpecAlways ALWAYS_ALLOW = new RuleSpecAlways(TriState.TRUE);
	public static final RuleSpecAlways ALWAYS_DENY = new RuleSpecAlways(TriState.FALSE);
	public static final RuleSpecAlways ALWAYS_PASS = new RuleSpecAlways(TriState.DEFAULT);
	
	public static RuleSpecAlways always(TriState which) {
		return switch(which) {
			case FALSE -> ALWAYS_DENY;
			case DEFAULT -> ALWAYS_PASS;
			case TRUE -> ALWAYS_ALLOW;
		};
	}
	
	@Override
	public Rule build() {
		return (attacker, defender) -> value;
	}
	
	@Override
	public RuleSerializer<RuleSpecAlways> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements RuleSerializer<RuleSpecAlways> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecAlways rule, JsonObject json) {
			json.addProperty("value", rule.value.toAllowDenyPassString());
		}
		
		@Override
		public RuleSpecAlways read(JsonObject json) {
			return new RuleSpecAlways(CoolGsonHelper.getAllowDenyPassTriState(json, "value"));
		}
	}
}
