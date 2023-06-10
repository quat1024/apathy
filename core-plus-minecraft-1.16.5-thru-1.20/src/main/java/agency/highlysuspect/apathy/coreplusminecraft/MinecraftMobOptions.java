package agency.highlysuspect.apathy.coreplusminecraft;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.config.ConfigProperty;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.rule.Who;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinecraftMobOptions {
	public static final ConfigProperty<Set<MobEffect>> mobEffectSet = mobEffectSetOpt("potionEffectSet", Collections.singleton(MobEffects.INVISIBILITY),
		"A set of potion effects.",
		"Example: minecraft:invisibility, minecraft:jump_boost"
	).build();
	
	public static final ConfigProperty<Who> mobEffectWho = ConfigProperty.whoOpt("potionEffectWho", Who.DEFENDER,
		"Whose potion effects will be checked?",
		"May be one of:",
		"attacker - The attacking mob's potion effects will be checked.",
		"defender - The player's potion effects will be checked."
	).build();
	
	public static final ConfigProperty<TriState> mobEffectSetIncluded = ConfigProperty.allowDenyPassOpt("potionEffectSetIncluded", TriState.DEFAULT,
		"What happens when the entity has a potion effect included in potionEffectSet?",
		"May be one of:",
		"allow - The mob will be allowed to attack the player.",
		"deny  - The mob will not be allowed to attack the player.",
		"pass  - Defer to the next rule."
	).build();
	
	public static final ConfigProperty<TriState> mobEffectSetExcluded = ConfigProperty.allowDenyPassOpt("potionEffectSetExcluded", TriState.DEFAULT,
		"What happens when the entity does not have any potion effects included in potionEffectSet?",
		"May be one of:",
		"allow - The mob will be allowed to attack the player.",
		"deny  - The mob will not be allowed to attack the player.",
		"pass  - Defer to the next rule."
	).build();
	
	public static void visit(ConfigSchema schema) {
		schema.afterSection("Tag Set Rule", "Potion Effect Set Rule", mobEffectSet, mobEffectWho, mobEffectSetIncluded, mobEffectSetExcluded);
	}
	
	private static <B extends ConfigProperty.Builder<Set<MobEffect>, B>> B mobEffectSetOpt(String name, Set<MobEffect> defaultValue, String... comment) {
		return new ConfigProperty.Builder<Set<MobEffect>, B>(name, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.map(ApathyPlusMinecraft.instanceMinecraft.mobEffectRegistry()::getKey)
				.filter(Objects::nonNull)
				.map(ResourceLocation::toString)
				.sorted()
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.trim().split(","))
				.map(String::trim)
				.flatMap(st -> {
					ResourceLocation rl = ResourceLocation.tryParse(st);
					if(rl == null) {
						Apathy.instance.log.warn("invalid resource location: " + st);
						return Stream.of();
					} else return Stream.of(rl);
				})
				.flatMap(rl -> {
					MobEffect effect = ApathyPlusMinecraft.instanceMinecraft.mobEffectRegistry().get(rl);
					if(effect == null) {
						Apathy.instance.log.warn("invalid mob effect: " + rl);
						return Stream.of();
					} else return Stream.of(effect);
				})
				.collect(Collectors.toSet()));
	}
}
