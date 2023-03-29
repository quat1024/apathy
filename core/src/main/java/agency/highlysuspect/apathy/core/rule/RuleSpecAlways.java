package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public class RuleSpecAlways implements RuleSpec<RuleSpecAlways> {
	public RuleSpecAlways(TriState value) {
		this.value = value;
	}
	
	public final TriState value;
	
	public static final RuleSpecAlways ALWAYS_ALLOW = new RuleSpecAlways(TriState.TRUE);
	public static final RuleSpecAlways ALWAYS_DENY = new RuleSpecAlways(TriState.FALSE);
	public static final RuleSpecAlways ALWAYS_PASS = new RuleSpecAlways(TriState.DEFAULT);
	
	public static RuleSpecAlways always(TriState which) {
		switch(which) {
			case FALSE:
				return ALWAYS_DENY;
			case DEFAULT:
				return ALWAYS_PASS;
			case TRUE:
				return ALWAYS_ALLOW;
			default:
				throw new IllegalArgumentException();
		}
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
		private Serializer() {}
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
