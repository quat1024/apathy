package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.CodeccyNotRegistry;
import agency.highlysuspect.apathy.rule.spec.predicate.AllPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.AlwaysPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.AnyPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.AttackerIsBossPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.AttackerIsPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.AttackerTaggedWithPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.DefenderHasAdvancementPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.DefenderInPlayerSetPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.DifficultyIsPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.LocationPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.NotPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.PredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.RevengeTimerPredicateSpec;
import agency.highlysuspect.apathy.rule.spec.predicate.ScorePredicateSpec;
import com.mojang.serialization.Codec;

import java.util.function.Function;

/**
 * IS USING VANILLA REGISTRY TECH IN 2022 A GOOD IDEA?????? no
 * UPDATE: I have moved off of vanilla registry tech :tada:
 * 
 * TODO: move off of Codecs too
 *  (Codecs are way fucking overkill for this stuff, it's just basic load-from-json really)
 */
@Deprecated(forRemoval = true) //Codec removal
public class Specs {
	public static final CodeccyNotRegistry<Codec<? extends RuleSpec>> RULE_SPEC_CODEC_REGISTRY = new CodeccyNotRegistry<>("apathy:rule_spec_codec");
	public static final CodeccyNotRegistry<Codec<? extends PredicateSpec>> PREDICATE_SPEC_CODEC_REGISTRY = new CodeccyNotRegistry<>("apathy:predicate_spec_codec");
	
	public static final Codec<RuleSpec<?>> RULE_SPEC_CODEC = RULE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(RuleSpec::codec, shit -> (Codec<? extends RuleSpec<?>>) shit);
	public static final Codec<PredicateSpec> PREDICATE_SPEC_CODEC = PREDICATE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(PredicateSpec::codec, Function.identity());
	
	@Deprecated(forRemoval = true)
	public static void onInitialize() {
		RULE_SPEC_CODEC_REGISTRY.register("apathy:always", AlwaysRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:chain", ChainRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:predicated", PredicatedRuleSpec.PREDICATED_CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:allow_if", PredicatedRuleSpec.ALLOW_IF_CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:deny_if", PredicatedRuleSpec.DENY_IF_CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:debug", DebugRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:difficulty_case", DifficultyCaseRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register("apathy:evaluate_json_file", JsonRuleSpec.CODEC);
		
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:always", AlwaysPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_tagged_with", AttackerTaggedWithPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_is_boss", AttackerIsBossPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_is", AttackerIsPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:in_player_set", DefenderInPlayerSetPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:revenge_timer", RevengeTimerPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:difficulty_is", DifficultyIsPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:score", ScorePredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:advancements", DefenderHasAdvancementPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:location", LocationPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:all", AllPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:any", AnyPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:not", NotPredicateSpec.CODEC);
	}
}
