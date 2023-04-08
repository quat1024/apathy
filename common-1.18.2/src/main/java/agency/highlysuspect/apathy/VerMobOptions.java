package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.config.ConfigProperty;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * in comparison to CoreMobOptions, this file contains definitions for some options that refer to types
 * from Minecraft, and/or rely on minecraft being present to serialize and deserialize them correctly
 */
public class VerMobOptions {
	private static <B extends ConfigProperty.Builder<Set<TagKey<EntityType<?>>>, B>> B entityTypeTagKeySetOpt(String name, Set<TagKey<EntityType<?>>> defaultValue, String... comment) {
		return new ConfigProperty.Builder<Set<TagKey<EntityType<?>>>, B>(name, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.map(TagKey::location)
				.map(ResourceLocation::toString)
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.split(","))
				.map(String::trim)
				.map(ResourceLocation::new)
				.map(rl -> TagKey.create(Registry.ENTITY_TYPE_REGISTRY, rl))
				.collect(Collectors.toSet()));
	}
	
	public static final ConfigProperty<Set<TagKey<EntityType<?>>>> tagSet = entityTypeTagKeySetOpt("tagSet", Collections.emptySet(), "A comma-separated set of entity type tags.")
		.example("minecraft:raiders, some_datapack:some_tag")
		.build();
	
	public static void visit(ConfigSchema schema) {
		schema.getSection("Tag Set Rule").add(0, tagSet);
	}
}
