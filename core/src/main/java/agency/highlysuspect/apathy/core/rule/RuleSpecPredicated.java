package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public final class RuleSpecPredicated implements RuleSpec<RuleSpecPredicated> {
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PartialSpec<?> predSpec) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	private final PartialSpec<?> predSpec;
	
	@Override
	public RuleSpec<?> optimize() {
		if(ifTrue == ifFalse) return RuleSpecAlways.always(ifTrue);
		
		PartialSpec<?> predSpecOpt = predSpec.optimize();
		
		if(predSpec == PartialSpecAlways.TRUE) return RuleSpecAlways.always(ifTrue);
		if(predSpec == PartialSpecAlways.FALSE) return RuleSpecAlways.always(ifFalse);
		
		return new RuleSpecPredicated(ifTrue, ifFalse, predSpecOpt);
	}
	
	@Override
	public Rule build() {
		Partial builtPred = predSpec.build();
		return (attacker, defender) -> builtPred.test(attacker, defender) ? ifTrue : ifFalse;
	}
	
	@Override
	public RuleSerializer<RuleSpecPredicated> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements RuleSerializer<RuleSpecPredicated> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecPredicated rule, JsonObject json) {
			json.addProperty("if_true", rule.ifTrue.toAllowDenyPassString());
			json.addProperty("if_false", rule.ifFalse.toAllowDenyPassString());
			json.add("predicate", Apathy.instance.writePartial(rule.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			TriState ifTrue = CoolGsonHelper.getAllowDenyPassTriState(json, "if_true", TriState.DEFAULT);
			TriState ifFalse = CoolGsonHelper.getAllowDenyPassTriState(json, "if_false", TriState.DEFAULT);
			PartialSpec<?> part = Apathy.instance.readPartial(json.get("predicate"));
			return new RuleSpecPredicated(ifTrue, ifFalse, part);
		}
	}
	
	@Deprecated //allow_if is a confusing, secondary way to do the same thing. It is kept for backwards compatibility only.
	public static class LegacyAllowIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		private LegacyAllowIfSerializer() {}
		public static final LegacyAllowIfSerializer INSTANCE = new LegacyAllowIfSerializer();
		
		@Override
		public void write(RuleSpecPredicated rule, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(rule.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return new RuleSpecPredicated(TriState.TRUE, TriState.DEFAULT, Apathy.instance.readPartial(json.get("predicate")));
		}
	}
	
	@Deprecated //deny_if is a confusing, secondary way to do the same thing. It is kept for backwards compatibility only.
	public static class LegacyDenyIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		private LegacyDenyIfSerializer() {}
		public static final LegacyDenyIfSerializer INSTANCE = new LegacyDenyIfSerializer();
		
		@Override
		public void write(RuleSpecPredicated rule, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(rule.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return new RuleSpecPredicated(TriState.FALSE, TriState.DEFAULT, Apathy.instance.readPartial(json.get("predicate")));
		}
	}
}
