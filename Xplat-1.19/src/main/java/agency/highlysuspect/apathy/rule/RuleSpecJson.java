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
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements RuleSerializer<RuleSpecJson> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecJson ruleSpecJson, JsonObject json) {
			//Nothing to write
		}
		
		@Override
		public RuleSpecJson read(JsonObject json) {
			return RuleSpecJson.INSTANCE;
		}
	}
}
