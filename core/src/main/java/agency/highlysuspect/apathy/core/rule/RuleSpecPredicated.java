package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonObject;

public final class RuleSpecPredicated implements RuleSpec<RuleSpecPredicated> {
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PartialSpec<?> predSpec) {
		this(ifTrue, ifFalse, predSpec, PredicatedSerializer.INSTANCE);
	}
	
	public static RuleSpecPredicated allowIf(PartialSpec<?> spec) {
		return new RuleSpecPredicated(TriState.TRUE, TriState.DEFAULT, spec, AllowIfSerializer.INSTANCE);
	}
	
	public static RuleSpecPredicated denyIf(PartialSpec<?> spec) {
		return new RuleSpecPredicated(TriState.FALSE, TriState.DEFAULT, spec, DenyIfSerializer.INSTANCE);
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	private final PartialSpec<?> predSpec;
	private final RuleSerializer<RuleSpecPredicated> theSerializer;
	
	public RuleSpecPredicated(TriState ifTrue, TriState ifFalse, PartialSpec<?> predSpec, RuleSerializer<RuleSpecPredicated> theSerializer) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
		this.theSerializer = theSerializer;
	}
	
	@Override
	public RuleSpec<?> optimize() {
		if(ifTrue == ifFalse) return RuleSpecAlways.always(ifTrue);
		
		PartialSpec<?> predSpecOpt = predSpec.optimize();
		
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
		private PredicatedSerializer() {}
		public static final PredicatedSerializer INSTANCE = new PredicatedSerializer();
		
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
			return new RuleSpecPredicated(ifTrue, ifFalse, part, this);
		}
	}
	
	public static class AllowIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		private AllowIfSerializer() {}
		public static final AllowIfSerializer INSTANCE = new AllowIfSerializer();
		
		@Override
		public void write(RuleSpecPredicated rule, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(rule.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return RuleSpecPredicated.allowIf(Apathy.instance.readPartial(json.get("predicate")));
		}
	}
	
	public static class DenyIfSerializer implements RuleSerializer<RuleSpecPredicated> {
		private DenyIfSerializer() {}
		public static final DenyIfSerializer INSTANCE = new DenyIfSerializer();
		
		@Override
		public void write(RuleSpecPredicated rule, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(rule.predSpec));
		}
		
		@Override
		public RuleSpecPredicated read(JsonObject json) {
			return RuleSpecPredicated.denyIf(Apathy.instance.readPartial(json.get("predicate")));
		}
	}
}
