package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.Function;

public abstract class RuleSpec {
	public static final RegistryKey<Registry<Codec<? extends RuleSpec>>> KEY = RegistryKey.ofRegistry(Init.id("rule_spec"));
	public static final Registry<Codec<? extends RuleSpec>> CODEC_REGISTRY = new SimpleRegistry<>(KEY, Lifecycle.stable());
	//Not named "CODEC" because you can accidentally name it when you meant to refer to CODEC in a subclass.
	//It's possible to accidentally leave that field private, or use an inline Codec.unit() instead of defining a field.
	public static final Codec<RuleSpec> SPEC_CODEC = CODEC_REGISTRY.dispatch(RuleSpec::codec, Function.identity());
	
	public abstract Rule buildRule();
	public abstract Codec<? extends RuleSpec> codec();
}
