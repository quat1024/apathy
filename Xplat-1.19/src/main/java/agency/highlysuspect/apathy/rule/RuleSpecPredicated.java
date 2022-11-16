package agency.highlysuspect.apathy.rule;

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
		public JsonObject write(RuleSpecPredicated ruleSpecPredicated, JsonObject json) {
			json.addProperty("if_true", ruleSpecPredicated.ifTrue.toAllowDenyPassString());
			json.addProperty("if_false", ruleSpecPredicated.ifFalse.toAllowDenyPassString());
			
			//TODO: predicate writing
			json.add("predicate", Specs.PREDICATE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, ruleSpecPredicated.predSpec).getOrThrow(false, ApathyHell.instance.log::error));
			return json;
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			TriState ifTrue = CoolGsonHelper.getAllowDenyPassTriState(json, "if_true", TriState.DEFAULT);
			TriState ifFalse = CoolGsonHelper.getAllowDenyPassTriState(json, "if_false", TriState.DEFAULT);
			
			//TODO: predicate reading
			PartialSpec pred = Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst();
			
			return new RuleSpecPredicated(ifTrue, ifFalse, pred, this);
		}
	}
	
	public static class AllowIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final AllowIfSerializer INSTANCE = new AllowIfSerializer();
		
		@Override
		public JsonObject write(RuleSpecPredicated ruleSpecPredicated, JsonObject json) {
			//TODO: predicate writing
			json.add("predicate", Specs.PREDICATE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, ruleSpecPredicated.predSpec).getOrThrow(false, ApathyHell.instance.log::error));
			return json;
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			//TODO: predicate reading
			return RuleSpecPredicated.allowIf(Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst());
		}
	}
	
	public static class DenyIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final DenyIfSerializer INSTANCE = new DenyIfSerializer();
		
		@Override
		public JsonObject write(RuleSpecPredicated ruleSpecPredicated, JsonObject json) {
			//TODO: predicate writing
			json.add("predicate", Specs.PREDICATE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, ruleSpecPredicated.predSpec).getOrThrow(false, ApathyHell.instance.log::error));
			return json;
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			//TODO: predicate reading
			return RuleSpecPredicated.denyIf(Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst());
		}
	}
}
