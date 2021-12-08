package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.spec.predicate.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.function.Function;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class Specs {
	public static final ResourceKey<Registry<Codec<? extends RuleSpec>>> RULE_SPEC_CODEC_KEY = ResourceKey.createRegistryKey(Init.id("rule_spec_codec"));
	public static final Registry<Codec<? extends RuleSpec>> RULE_SPEC_CODEC_REGISTRY = new MappedRegistry<>(RULE_SPEC_CODEC_KEY, Lifecycle.stable());
	
	public static final ResourceKey<Registry<Codec<? extends PredicateSpec>>> PREDICATE_SPEC_REGISTRY_KEY = ResourceKey.createRegistryKey(Init.id("rule_predicate_spec_codec"));
	public static final Registry<Codec<? extends PredicateSpec>> PREDICATE_SPEC_CODEC_REGISTRY = new MappedRegistry<>(PREDICATE_SPEC_REGISTRY_KEY, Lifecycle.stable());
	
	public static final Codec<RuleSpec> RULE_SPEC_CODEC = RULE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(RuleSpec::codec, Function.identity());
	public static final Codec<PredicateSpec> PREDICATE_SPEC_CODEC = PREDICATE_SPEC_CODEC_REGISTRY.byNameCodec().dispatch(PredicateSpec::codec, Function.identity());
	
	public static void onInitialize() {
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("always"), AlwaysRuleSpec.CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("chain"), ChainRuleSpec.CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("predicated"), PredicatedRuleSpec.PREDICATED_CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("allow_if"), PredicatedRuleSpec.ALLOW_IF_CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("deny_if"), PredicatedRuleSpec.DENY_IF_CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("debug"), DebugRuleSpec.CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("difficulty_case"), DifficultyCaseRuleSpec.CODEC);
		Registry.register(RULE_SPEC_CODEC_REGISTRY, Init.id("evaluate_json_file"), JsonRuleSpec.CODEC);
		
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("always"), AlwaysPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("attacker_tagged_with"), AttackerTaggedWithPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("attacker_is_boss"), AttackerIsBossPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("attacker_is"), AttackerIsPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("in_player_set"), DefenderInPlayerSetPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("revenge_timer"), RevengeTimerPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("difficulty_is"), DifficultyIsPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("all"), AllPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("any"), AnyPredicateSpec.CODEC);
		Registry.register(PREDICATE_SPEC_CODEC_REGISTRY, Init.id("not"), NotPredicateSpec.CODEC);
	}
}
