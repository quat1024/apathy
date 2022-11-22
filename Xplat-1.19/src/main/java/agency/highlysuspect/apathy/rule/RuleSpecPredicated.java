package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAlways;
import agency.highlysuspect.apathy.rule.predicate.Partial;
import agency.highlysuspect.apathy.rule.predicate.PartialSpec;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

public final class RuleSpecPredicated implements RuleSpec<RuleSpecPredicated> {
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PartialSpec predSpec) {
		this(ifTrue, ifFalse, predSpec, PredicatedSerializer.INSTANCE);
	}
	
	public static RuleSpecPredicated allowIf(PartialSpec spec) {
		return new RuleSpecPredicated(TriState.TRUE, TriState.DEFAULT, spec, AllowIfSerializer.INSTANCE);
	}
	
	public static RuleSpecPredicated denyIf(PartialSpec spec) {
		return new RuleSpecPredicated(TriState.FALSE, TriState.DEFAULT, spec, DenyIfSerializer.INSTANCE);
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	private final PartialSpec predSpec;
	private final RuleSerializer<RuleSpecPredicated> theSerializer;
	
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PartialSpec predSpec, RuleSerializer<RuleSpecPredicated> theSerializer) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
		this.theSerializer = theSerializer;
	}
	
	@Override
	public RuleSpec<?> optimize() {
		if(ifTrue == ifFalse) return RuleSpecAlways.always(ifTrue);
		
		PartialSpec predSpecOpt = predSpec.optimize();
		
		if(predSpec == PartialSpecAlways.TRUE) return RuleSpecAlways.always(ifTrue);
		if(predSpec == PartialSpecAlways.FALSE) return RuleSpecAlways.always(ifFalse);
		
		//Try to use an ifTrue/ifFalse serializer if at all possible
		RuleSerializer<RuleSpecPredicated> newSer = PredicatedSerializer.INSTANCE;
		if(ifFalse == TriState.DEFAULT) {
			if(ifTrue == TriState.TRUE) newSer = AllowIfSerializer.INSTANCE;
			if(ifTrue == TriState.FALSE) newSer = DenyIfSerializer.INSTANCE;
		}
		
		return new RuleSpecPredicated(ifTrue, ifFalse, predSpecOpt, newSer);
	}
	
	@Override
	public Rule build() {
		Partial builtPred = predSpec.build();
		return (attacker, defender) -> builtPred.test(attacker, defender) ? ifTrue : ifFalse;
	}
	
	@Override
	public RuleSerializer<RuleSpecPredicated> getSerializer() {
		return theSerializer;
	}
	
	public static class PredicatedSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final PredicatedSerializer INSTANCE = new PredicatedSerializer();
		
		@Override
		public JsonObject write(RuleSpecPredicated rule, JsonObject json) {
			json.addProperty("if_true", rule.ifTrue.toAllowDenyPassString());
			json.addProperty("if_false", rule.ifFalse.toAllowDenyPassString());
			json.add("predicate", Apathy119.instance119.writePartial(rule.predSpec));
			return json;
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			TriState ifTrue = CoolGsonHelper.getAllowDenyPassTriState(json, "if_true", TriState.DEFAULT);
			TriState ifFalse = CoolGsonHelper.getAllowDenyPassTriState(json, "if_false", TriState.DEFAULT);
			PartialSpec<?> part = Apathy119.instance119.readPartial(json.get("predicate"));
			return new RuleSpecPredicated(ifTrue, ifFalse, part, this);
		}
	}
	
	public static class AllowIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final AllowIfSerializer INSTANCE = new AllowIfSerializer();
		
		@Override
		public JsonObject write(RuleSpecPredicated rule, JsonObject json) {
			json.add("predicate", Apathy119.instance119.writePartial(rule.predSpec));
			return json;
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return RuleSpecPredicated.allowIf(Apathy119.instance119.readPartial(json.get("predicate")));
		}
	}
	
	public static class DenyIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final DenyIfSerializer INSTANCE = new DenyIfSerializer();
		
		@Override
		public JsonObject write(RuleSpecPredicated rule, JsonObject json) {
			json.add("predicate", Apathy119.instance119.writePartial(rule.predSpec));
			return json;
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return RuleSpecPredicated.denyIf(Apathy119.instance119.readPartial(json.get("predicate")));
		}
	}
}
