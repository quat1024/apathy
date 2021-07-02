package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.spec.predicate.AlwaysPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.PredicateSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

public record PredicatedRuleSpec(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec, Codec<PredicatedRuleSpec> theCodec) implements RuleSpec {
	public PredicatedRuleSpec(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec) {
		this(ifTrue, ifFalse, predSpec, PREDICATED_CODEC);
	}
	
	public static PredicatedRuleSpec allowIf(PredicateSpec spec) {
		return new PredicatedRuleSpec(TriState.TRUE, TriState.DEFAULT, spec, ALLOW_IF_CODEC);
	}
	
	public static PredicatedRuleSpec denyIf(PredicateSpec spec) {
		return new PredicatedRuleSpec(TriState.FALSE, TriState.DEFAULT, spec, DENY_IF_CODEC);
	}
	
	public static final Codec<PredicatedRuleSpec> PREDICATED_CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_true", TriState.DEFAULT).forGetter(x -> x.ifTrue),
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_false", TriState.DEFAULT).forGetter(x -> x.ifFalse),
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::new));
	
	public static final Codec<PredicatedRuleSpec> ALLOW_IF_CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::allowIf));
	
	public static final Codec<PredicatedRuleSpec> DENY_IF_CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::denyIf));
	
	@Override
	public RuleSpec optimize() {
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
		
		return new PredicatedRuleSpec(ifTrue, ifFalse, predSpecOpt, newCodec);
	}
	
	@Override
	public Rule build() {
		Partial builtPred = predSpec.build();
		return (attacker, defender) -> builtPred.test(attacker, defender) ? ifTrue : ifFalse;
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return theCodec;
	}
}
