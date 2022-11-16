package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.spec.predicate.AlwaysPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.PredicateSpec;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class PredicatedRuleSpec implements RuleSpec<PredicatedRuleSpec> {
	public PredicatedRuleSpec(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec) {
		this(ifTrue, ifFalse, predSpec, PREDICATED_CODEC, PredicatedRuleSpecSerializer.INSTANCE);
	}
	
	public static PredicatedRuleSpec allowIf(PredicateSpec spec) {
		return new PredicatedRuleSpec(TriState.TRUE, TriState.DEFAULT, spec, ALLOW_IF_CODEC, AllowIfRuleSerializer.INSTANCE);
	}
	
	public static PredicatedRuleSpec denyIf(PredicateSpec spec) {
		return new PredicatedRuleSpec(TriState.FALSE, TriState.DEFAULT, spec, DENY_IF_CODEC, DenyIfRuleSerializer.INSTANCE);
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	private final PredicateSpec predSpec;
	@Deprecated(forRemoval = true) private final Codec<PredicatedRuleSpec> theCodec;
	private final RuleSerializer<PredicatedRuleSpec> theSerializer;
	
	public PredicatedRuleSpec(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec, Codec<PredicatedRuleSpec> theCodec, RuleSerializer<PredicatedRuleSpec> theSerializer) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
		this.theCodec = theCodec;
		this.theSerializer = theSerializer;
	}
	
	@Override
	public RuleSpec<?> optimize() {
		if(ifTrue == ifFalse) return AlwaysRuleSpec.always(ifTrue);
		
		PredicateSpec predSpecOpt = predSpec.optimize();
		
		if(predSpec == AlwaysPredicateSpec.TRUE) return AlwaysRuleSpec.always(ifTrue);
		if(predSpec == AlwaysPredicateSpec.FALSE) return AlwaysRuleSpec.always(ifFalse);
		
		//Try to use an ifTrue/ifFalse codec if at all possible
		Codec<PredicatedRuleSpec> newCodec = PREDICATED_CODEC;
		if(ifFalse == TriState.DEFAULT) {
			if(ifTrue == TriState.TRUE) newCodec = ALLOW_IF_CODEC;
			if(ifTrue == TriState.FALSE) newCodec = DENY_IF_CODEC;
		}
		
		//Try to use an ifTrue/ifFalse serializer if at all possible
		RuleSerializer<PredicatedRuleSpec> newSer = PredicatedRuleSpecSerializer.INSTANCE;
		if(ifFalse == TriState.DEFAULT) {
			if(ifTrue == TriState.TRUE) newSer = AllowIfRuleSerializer.INSTANCE;
			if(ifTrue == TriState.FALSE) newSer = DenyIfRuleSerializer.INSTANCE;
		}
		
		return new PredicatedRuleSpec(ifTrue, ifFalse, predSpecOpt, newCodec, newSer);
	}
	
	@Override
	public Rule build() {
		Partial builtPred = predSpec.build();
		return (attacker, defender) -> builtPred.test(attacker, defender) ? ifTrue : ifFalse;
	}
	
	@Override
	public RuleSerializer<PredicatedRuleSpec> getSerializer() {
		return theSerializer;
	}
	
	public static class PredicatedRuleSpecSerializer implements RuleSerializer<PredicatedRuleSpec> {
		public static final PredicatedRuleSpecSerializer INSTANCE = new PredicatedRuleSpecSerializer();
		
		@Override
		public JsonObject write(PredicatedRuleSpec predicatedRuleSpec, JsonObject json) {
			json.addProperty("if_true", predicatedRuleSpec.ifTrue.toAllowDenyPassString());
			json.addProperty("if_false", predicatedRuleSpec.ifFalse.toAllowDenyPassString());
			
			//TODO: predicate writing
			json.add("predicate", Specs.PREDICATE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, predicatedRuleSpec.predSpec).getOrThrow(false, ApathyHell.instance.log::error));
			return json;
		}
		
		@Override
		public PredicatedRuleSpec read(JsonObject json) {
			TriState ifTrue = CoolGsonHelper.getAllowDenyPassTriState(json, "if_true", TriState.DEFAULT);
			TriState ifFalse = CoolGsonHelper.getAllowDenyPassTriState(json, "if_false", TriState.DEFAULT);
			
			//TODO: predicate reading
			PredicateSpec pred = Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst();
			
			return new PredicatedRuleSpec(ifTrue, ifFalse, pred, PREDICATED_CODEC, this);
		}
	}
	
	public static class AllowIfRuleSerializer implements RuleSerializer<PredicatedRuleSpec> {
		public static final AllowIfRuleSerializer INSTANCE = new AllowIfRuleSerializer();
		
		@Override
		public JsonObject write(PredicatedRuleSpec predicatedRuleSpec, JsonObject json) {
			//TODO: predicate writing
			json.add("predicate", Specs.PREDICATE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, predicatedRuleSpec.predSpec).getOrThrow(false, ApathyHell.instance.log::error));
			return json;
		}
		
		@Override
		public PredicatedRuleSpec read(JsonObject json) {
			//TODO: predicate reading
			return PredicatedRuleSpec.allowIf(Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst());
		}
	}
	
	public static class DenyIfRuleSerializer implements RuleSerializer<PredicatedRuleSpec> {
		public static final DenyIfRuleSerializer INSTANCE = new DenyIfRuleSerializer();
		
		@Override
		public JsonObject write(PredicatedRuleSpec predicatedRuleSpec, JsonObject json) {
			//TODO: predicate writing
			json.add("predicate", Specs.PREDICATE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, predicatedRuleSpec.predSpec).getOrThrow(false, ApathyHell.instance.log::error));
			return json;
		}
		
		@Override
		public PredicatedRuleSpec read(JsonObject json) {
			//TODO: predicate reading
			return PredicatedRuleSpec.denyIf(Specs.PREDICATE_SPEC_CODEC.decode(JsonOps.INSTANCE, json.get("predicate")).getOrThrow(false, ApathyHell.instance.log::error).getFirst());
		}
	}
	
	///CODEC HELLZONE///
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends RuleSpec<?>> codec() {
		return theCodec;
	}
	
	@Deprecated(forRemoval = true)
	public static final Codec<PredicatedRuleSpec> PREDICATED_CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_true", TriState.DEFAULT).forGetter(x -> x.ifTrue),
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_false", TriState.DEFAULT).forGetter(x -> x.ifFalse),
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::new));
	
	@Deprecated(forRemoval = true)
	public static final Codec<PredicatedRuleSpec> ALLOW_IF_CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::allowIf));
	
	@Deprecated(forRemoval = true)
	public static final Codec<PredicatedRuleSpec> DENY_IF_CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::denyIf));
}
