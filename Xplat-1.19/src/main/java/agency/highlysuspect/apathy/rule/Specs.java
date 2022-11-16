package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.rule.predicate.PartialSpecAll;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAlways;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAny;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.rule.predicate.DefenderHasAdvancementPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.DefenderInPlayerSetPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.DifficultyIsPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.LocationPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.NotPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.PartialSpec;
import agency.highlysuspect.apathy.rule.predicate.RevengeTimerPredicateSpec;
import agency.highlysuspect.apathy.rule.predicate.ScorePredicateSpec;
import com.mojang.serialization.Codec;

/**
 * IS USING VANILLA REGISTRY TECH IN 2022 A GOOD IDEA?????? no
 * UPDATE: I have moved off of vanilla registry tech :tada:
 * 
 * TODO: move off of Codecs too
 *  (Codecs are way fucking overkill for this stuff, it's just basic load-from-json really)
 */
@Deprecated(forRemoval = true) //Codec removal
public class Specs {
	public static final CodeccyNotRegistry<Codec<? extends PartialSpec>> PREDICATE_SPEC_CODEC_REGISTRY = new CodeccyNotRegistry<>("apathy:predicate_spec_codec");
	
	public static final Codec<PartialSpec<?>> PREDICATE_SPEC_CODEC = PREDICATE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(PartialSpec::codec, what -> (Codec<? extends PartialSpec<?>>) what);
	
	@Deprecated(forRemoval = true)
	public static void onInitialize() {
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:always", PartialSpecAlways.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_tagged_with", PartialSpecAttackerTaggedWith.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_is_boss", PartialSpecAttackerIsBoss.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_is", PartialSpecAttackerIs.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:in_player_set", DefenderInPlayerSetPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:revenge_timer", RevengeTimerPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:difficulty_is", DifficultyIsPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:score", ScorePredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:advancements", DefenderHasAdvancementPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:location", LocationPredicateSpec.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:all", PartialSpecAll.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:any", PartialSpecAny.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:not", NotPredicateSpec.CODEC);
	}
}
