package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.spec.predicate.*;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class Specs {
	public static void onInitialize() {
		Registry.register(RuleSpec.CODEC_REGISTRY, Init.id("always"), AlwaysSpec.CODEC);
		Registry.register(RuleSpec.CODEC_REGISTRY, Init.id("clojure"), ClojureSpec.CODEC);
		Registry.register(RuleSpec.CODEC_REGISTRY, Init.id("predicated"), PredicatedSpec.CODEC);
		Registry.register(RuleSpec.CODEC_REGISTRY, Init.id("allow_if"), PredicatedSpec.AllowIf.CODEC);
		Registry.register(RuleSpec.CODEC_REGISTRY, Init.id("deny_if"), PredicatedSpec.DenyIf.CODEC);
		
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("always"), AlwaysPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("attacker_tagged_with"), AttackerTaggedWithPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("attacker_is_boss"), AttackerIsBossPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("attacker_is"), AttackerIsPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("in_player_set"), DefenderInPlayerSetPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("revenge_timer"), RevengeTimerPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("difficulty_is"), DifficultyIsPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("all"), AllPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("any"), AnyPredicateSpec.CODEC);
		Registry.register(PredicateSpec.CODEC_REGISTRY, Init.id("not"), NotPredicateSpec.CODEC);
		
		List<RuleSpec> funny = ImmutableList.of(new AlwaysSpec(TriState.TRUE), new AlwaysSpec(TriState.DEFAULT), new AlwaysSpec(TriState.FALSE));
		
		Init.LOG.info(RuleSpec.SPEC_CODEC.listOf().encodeStart(JsonOps.INSTANCE, funny));
		Init.LOG.info("Have a good day");
	}
}
