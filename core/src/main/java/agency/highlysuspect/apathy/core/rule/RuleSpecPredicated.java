package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public final class RuleSpecPredicated implements Spec<Rule, RuleSpecPredicated> {
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, Spec<Partial, ?> predSpec) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	private final Spec<Partial, ?> predSpec;
	
	@Override
	public Spec<Rule, ?> optimize() {
		if(ifTrue == ifFalse) return RuleSpecAlways.get(ifTrue);
		
		Spec<Partial, ?> predSpecOpt = predSpec.optimize();
		
		if(predSpecOpt == PartialSpecAlways.TRUE) return RuleSpecAlways.get(ifTrue);
		if(predSpecOpt == PartialSpecAlways.FALSE) return RuleSpecAlways.get(ifFalse);
		
		return new RuleSpecPredicated(ifTrue, ifFalse, predSpecOpt);
	}
	
	@Override
	public Rule build() {
		Partial builtPred = predSpec.build();
		return (attacker, defender) -> builtPred.test(attacker, defender) ? ifTrue : ifFalse;
	}
	
	@Override
	public JsonSerializer<RuleSpecPredicated> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<RuleSpecPredicated> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecPredicated thing, JsonObject json) {
			json.addProperty("if_true", thing.ifTrue.toAllowDenyPassString());
			json.addProperty("if_false", thing.ifFalse.toAllowDenyPassString());
			json.add("predicate", Apathy.instance.writePartial(thing.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			TriState ifTrue = CoolGsonHelper.getAllowDenyPassTriState(json, "if_true", TriState.DEFAULT);
			TriState ifFalse = CoolGsonHelper.getAllowDenyPassTriState(json, "if_false", TriState.DEFAULT);
			Spec<Partial, ?> part = Apathy.instance.readPartial(json.get("predicate"));
			return new RuleSpecPredicated(ifTrue, ifFalse, part);
		}
	}
	
	@Deprecated //allow_if is a confusing, secondary way to do the same thing. It is kept for backwards compatibility only.
	public static class LegacyAllowIfSerializer implements JsonSerializer<RuleSpecPredicated> {
		private LegacyAllowIfSerializer() {}
		public static final LegacyAllowIfSerializer INSTANCE = new LegacyAllowIfSerializer();
		
		@Override
		public void write(RuleSpecPredicated thing, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(thing.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return new RuleSpecPredicated(TriState.TRUE, TriState.DEFAULT, Apathy.instance.readPartial(json.get("predicate")));
		}
	}
	
	@Deprecated //deny_if is a confusing, secondary way to do the same thing. It is kept for backwards compatibility only.
	public static class LegacyDenyIfSerializer implements JsonSerializer<RuleSpecPredicated> {
		private LegacyDenyIfSerializer() {}
		public static final LegacyDenyIfSerializer INSTANCE = new LegacyDenyIfSerializer();
		
		@Override
		public void write(RuleSpecPredicated thing, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(thing.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return new RuleSpecPredicated(TriState.FALSE, TriState.DEFAULT, Apathy.instance.readPartial(json.get("predicate")));
		}
	}
}
