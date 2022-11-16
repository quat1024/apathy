package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.predicate.AlwaysPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.PredicateSpec;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

public final class RuleSpecPredicated implements RuleSpec<RuleSpecPredicated> {
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec) {
		this(ifTrue, ifFalse, predSpec, PredicatedRuleSpecSerializer.INSTANCE);
	}
	
	public static RuleSpecPredicated allowIf(PredicateSpec spec) {
		return new RuleSpecPredicated(TriState.TRUE, TriState.DEFAULT, spec, AllowIfRuleSerializer.INSTANCE);
	}
	
	public static RuleSpecPredicated denyIf(PredicateSpec spec) {
		return new RuleSpecPredicated(TriState.FALSE, TriState.DEFAULT, spec, DenyIfRuleSerializer.INSTANCE);
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	private final PredicateSpec predSpec;
	private final RuleSerializer<RuleSpecPredicated> theSerializer;
	
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec, RuleSerializer<RuleSpecPredicated> theSerializer) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
		this.theSerializer = theSerializer;
	}
	
	@Override
	public RuleSpec<?> optimize() {
		if(ifTrue == ifFalse) return RuleSpecAlways.always(ifTrue);
		
		PredicateSpec predSpecOpt = predSpec.optimize();
		
		if(predSpec == AlwaysPredicateSpec.TRUE) return RuleSpecAlways.always(ifTrue);
		if(predSpec == AlwaysPredicateSpec.FALSE) return RuleSpecAlways.always(ifFalse);
		
		//Try to use an ifTrue/ifFalse serializer if at all possible
		RuleSerializer<RuleSpecPredicated> newSer = PredicatedRuleSpecSerializer.INSTANCE;
		if(ifFalse == TriState.DEFAULT) {
			if(ifTrue == TriState.TRUE) newSer = AllowIfRuleSerializer.INSTANCE;
			if(ifTrue == TriState.FALSE) newSer = DenyIfRuleSerializer.INSTANCE;
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
	
	public static class PredicatedRuleSpecSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final PredicatedRuleSpecSerializer INSTANCE = new PredicatedRuleSpecSerializer();
		
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
			PredicateSpec pred = Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst();
			
			return new RuleSpecPredicated(ifTrue, ifFalse, pred, this);
		}
	}
	
	public static class AllowIfRuleSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final AllowIfRuleSerializer INSTANCE = new AllowIfRuleSerializer();
		
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
	
	public static class DenyIfRuleSerializer implements RuleSerializer<RuleSpecPredicated> {
		public static final DenyIfRuleSerializer INSTANCE = new DenyIfRuleSerializer();
		
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
