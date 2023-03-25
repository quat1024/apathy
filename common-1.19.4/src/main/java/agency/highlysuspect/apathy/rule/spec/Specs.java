package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.rule.NotRegistry;
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
public class Specs {
	public static final NotRegistry<Codec<? extends RuleSpec>> RULE_SPEC_CODEC_REGISTRY = new NotRegistry<>("apathy:rule_spec_codec");
	public static final NotRegistry<Codec<? extends PredicateSpec>> PREDICATE_SPEC_CODEC_REGISTRY = new NotRegistry<>("apathy:predicate_spec_codec");
	
	public static final Codec<RuleSpec> RULE_SPEC_CODEC = RULE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(RuleSpec::codec, Function.identity());
	public static final Codec<PredicateSpec> PREDICATE_SPEC_CODEC = PREDICATE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(PredicateSpec::codec, Function.identity());
	
	public static void onInitialize() {
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("always"), AlwaysRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("chain"), ChainRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("predicated"), PredicatedRuleSpec.PREDICATED_CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("allow_if"), PredicatedRuleSpec.ALLOW_IF_CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("deny_if"), PredicatedRuleSpec.DENY_IF_CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("debug"), DebugRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("difficulty_case"), DifficultyCaseRuleSpec.CODEC);
		RULE_SPEC_CODEC_REGISTRY.register(Apathy.id("evaluate_json_file"), JsonRuleSpec.CODEC);
		
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("always"), AlwaysPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("attacker_tagged_with"), AttackerTaggedWithPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("attacker_is_boss"), AttackerIsBossPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("attacker_is"), AttackerIsPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("in_player_set"), DefenderInPlayerSetPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("revenge_timer"), RevengeTimerPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("difficulty_is"), DifficultyIsPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("score"), ScorePredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("advancements"), DefenderHasAdvancementPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("location"), LocationPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("all"), AllPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("any"), AnyPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register(Apathy.id("not"), NotPredicateSpec.CODEC);
	}
}
