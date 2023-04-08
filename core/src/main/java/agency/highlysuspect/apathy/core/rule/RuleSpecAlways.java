package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public class RuleSpecAlways implements Spec<Rule, RuleSpecAlways> {
	private RuleSpecAlways(TriState value) { //use get() instead of this constructor
		this.value = value;
	}
	
	public final TriState value;
	
	public static final RuleSpecAlways ALWAYS_ALLOW = new RuleSpecAlways(TriState.TRUE);
	public static final RuleSpecAlways ALWAYS_DENY = new RuleSpecAlways(TriState.FALSE);
	public static final RuleSpecAlways ALWAYS_PASS = new RuleSpecAlways(TriState.DEFAULT);
	
	public static RuleSpecAlways get(TriState which) {
		switch(which) {
			case FALSE: return ALWAYS_DENY;
			case DEFAULT: return ALWAYS_PASS;
			case TRUE: return ALWAYS_ALLOW;
			default: throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Rule build() {
		return (attacker, defender) -> value;
	}
	
	@Override
	public JsonSerializer<RuleSpecAlways> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<RuleSpecAlways> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecAlways thing, JsonObject json) {
			json.addProperty("value", thing.value.toAllowDenyPassString());
		}
		
		@Override
		public RuleSpecAlways read(JsonObject json) {
			return RuleSpecAlways.get(CoolGsonHelper.getAllowDenyPassTriState(json, "value"));
		}
	}
}
