package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.spec.predicate.AlwaysPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.PredicateSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

public class PredicatedRuleSpec implements RuleSpec {
	public PredicatedRuleSpec(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	protected final PredicateSpec predSpec;
	
	public static final Codec<PredicatedRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_true", TriState.DEFAULT).forGetter(x -> x.ifTrue),
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_false", TriState.DEFAULT).forGetter(x -> x.ifFalse),
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedRuleSpec::new));
	
	@Override
	public RuleSpec optimize() {
		if(ifTrue == ifFalse) return AlwaysRuleSpec.always(ifTrue);
		
		PredicateSpec predSpecOpt = predSpec.optimize();
		
		if(predSpec == AlwaysPredicateSpec.TRUE) return AlwaysRuleSpec.always(ifTrue);
		if(predSpec == AlwaysPredicateSpec.FALSE) return AlwaysRuleSpec.always(ifFalse);
		
		return new PredicatedRuleSpec(ifTrue, ifFalse, predSpecOpt);
	}
	
	@Override
	public Rule build() {
		Partial builtPred = predSpec.build();
		return (attacker, defender) -> builtPred.test(attacker, defender) ? ifTrue : ifFalse;
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
	
	public static class AllowIf extends PredicatedRuleSpec {
		public AllowIf(PredicateSpec predSpec) {
			super(TriState.TRUE, TriState.DEFAULT, predSpec);
		}
		
		public static final Codec<AllowIf> CODEC = RecordCodecBuilder.create(i -> i.group(
			Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
		).apply(i, AllowIf::new));
		
		@Override
		public Codec<? extends RuleSpec> codec() {
			return CODEC;
		}
	}
	
	public static class DenyIf extends PredicatedRuleSpec {
		public DenyIf(PredicateSpec predSpec) {
			super(TriState.FALSE, TriState.DEFAULT, predSpec);
		}
		
		public static final Codec<DenyIf> CODEC = RecordCodecBuilder.create(i -> i.group(
			Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
		).apply(i, DenyIf::new));
		
		@Override
		public Codec<? extends RuleSpec> codec() {
			return CODEC;
		}
	}
}
