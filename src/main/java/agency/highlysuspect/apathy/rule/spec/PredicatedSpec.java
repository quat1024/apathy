package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.spec.predicate.PredicateSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

public class PredicatedSpec extends RuleSpec {
	public PredicatedSpec(TriState ifTrue, TriState ifFalse, PredicateSpec predSpec) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predSpec = predSpec;
	}
	
	private final TriState ifTrue;
	private final TriState ifFalse;
	protected final PredicateSpec predSpec;
	
	public static final Codec<PredicatedSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_true", TriState.DEFAULT).forGetter(x -> x.ifTrue),
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.optionalFieldOf("if_false", TriState.DEFAULT).forGetter(x -> x.ifFalse),
		PredicateSpec.SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
	).apply(i, PredicatedSpec::new));
	
	@Override
	public Rule buildRule() {
		return Rule.predicated(predSpec.buildPartial(), ifTrue, ifFalse);
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
	
	public static class AllowIf extends PredicatedSpec {
		public AllowIf(PredicateSpec predSpec) {
			super(TriState.TRUE, TriState.DEFAULT, predSpec);
		}
		
		public static final Codec<AllowIf> CODEC = RecordCodecBuilder.create(i -> i.group(
			PredicateSpec.SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
		).apply(i, AllowIf::new));
		
		@Override
		public Codec<? extends RuleSpec> codec() {
			return CODEC;
		}
	}
	
	public static class DenyIf extends PredicatedSpec {
		public DenyIf(PredicateSpec predSpec) {
			super(TriState.FALSE, TriState.DEFAULT, predSpec);
		}
		
		public static final Codec<DenyIf> CODEC = RecordCodecBuilder.create(i -> i.group(
			PredicateSpec.SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.predSpec)
		).apply(i, DenyIf::new));
		
		@Override
		public Codec<? extends RuleSpec> codec() {
			return CODEC;
		}
	}
}
