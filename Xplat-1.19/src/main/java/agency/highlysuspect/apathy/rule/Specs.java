package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.rule.predicate.PartialSpecAll;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAlways;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAny;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecDefenderHasAdvancement;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecDefenderInPlayerSet;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecDifficultyIs;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecLocation;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecNot;
import agency.highlysuspect.apathy.rule.predicate.PartialSpec;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecRevengeTimer;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecScore;
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
	@Deprecated(forRemoval = true)
	public static final CodeccyNotRegistry<Codec<? extends PartialSpec>> PREDICATE_SPEC_CODEC_REGISTRY = new CodeccyNotRegistry<>("apathy:predicate_spec_codec");
	
	@Deprecated(forRemoval = true)
	public static final Codec<PartialSpec<?>> PREDICATE_SPEC_CODEC = PREDICATE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(PartialSpec::codec, what -> (Codec<? extends PartialSpec<?>>) what);
	
	@Deprecated(forRemoval = true)
	public static void onInitialize() {
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:always", PartialSpecAlways.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_tagged_with", PartialSpecAttackerTaggedWith.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_is_boss", PartialSpecAttackerIsBoss.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:attacker_is", PartialSpecAttackerIs.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:in_player_set", PartialSpecDefenderInPlayerSet.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:revenge_timer", PartialSpecRevengeTimer.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:difficulty_is", PartialSpecDifficultyIs.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:score", PartialSpecScore.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:advancements", PartialSpecDefenderHasAdvancement.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:location", PartialSpecLocation.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:all", PartialSpecAll.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:any", PartialSpecAny.CODEC);
		PREDICATE_SPEC_CODEC_REGISTRY.register("apathy:not", PartialSpecNot.CODEC);
	}
}
