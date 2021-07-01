package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.Function;

public abstract class PredicateSpec {
	public static final RegistryKey<Registry<Codec<? extends PredicateSpec>>> KEY = RegistryKey.ofRegistry(Init.id("rule_predicate_spec_codec"));
	public static final Registry<Codec<? extends PredicateSpec>> CODEC_REGISTRY = new SimpleRegistry<>(KEY, Lifecycle.stable());
	//Not named "CODEC" because you can accidentally name it when you meant to refer to CODEC in a subclass.
	//It's possible to accidentally leave that field private, or use an inline Codec.unit() instead of defining a field.
	public static final Codec<PredicateSpec> SPEC_CODEC = CODEC_REGISTRY.dispatch(PredicateSpec::codec, Function.identity());
	
	public abstract Partial buildPartial();
	public abstract Codec<? extends PredicateSpec> codec();
}
