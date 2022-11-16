package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.rule.predicate.AllPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.AlwaysPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.AnyPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.AttackerIsBossPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.AttackerIsPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.AttackerTaggedWithPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.DefenderHasAdvancementPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.DefenderInPlayerSetPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.DifficultyIsPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.LocationPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.NotPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.PredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.RevengeTimerPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.ScorePredicateSpec;
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
	public static final CodeccyNotRegistry<Codec<? extends PredicateSpec>> PREDICATE_SPEC_CODEC_REGISTRY = new CodeccyNotRegistry<>("apathy:predicate_spec_codec");
	
	public static final Codec<PredicateSpec> PREDICATE_SPEC_CODEC = PREDICATE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(PredicateSpec::codec, Function.identity());
	
	@Deprecated(forRemoval = true)
	public static void onInitialize() {
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
