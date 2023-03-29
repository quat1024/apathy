package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.newconfig.ConfigProperty;
import agency.highlysuspect.apathy.core.newconfig.ConfigSchema;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class PlatformOptions {
	private static <B extends ConfigProperty.Builder<Set<EntityType<?>>, B>> B entityTypeSetOpt(String name, Set<EntityType<?>> defaultValue, String... comment) {
		return new ConfigProperty.Builder<Set<EntityType<?>>, B>(name, Set.class, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.map(Registry.ENTITY_TYPE::getKey)
				.map(ResourceLocation::toString)
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.split(","))
				.map(String::trim)
				.map(ResourceLocation::new)
				.map(Registry.ENTITY_TYPE::get)
				.collect(Collectors.toSet()));
	}
	
	private static <B extends ConfigProperty.Builder<Set<TagKey<EntityType<?>>>, B>> B entityTypeTagKeySetOpt(String name, Set<TagKey<EntityType<?>>> defaultValue, String... comment) {
		return new ConfigProperty.Builder<Set<TagKey<EntityType<?>>>, B>(name, Set.class, defaultValue)
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
	
	public static class Mobs {
		public static final ConfigProperty<Set<EntityType<?>>> mobSet = entityTypeSetOpt("mobSet", Collections.emptySet(), "A comma-separated set of mob IDs.")
			.example("minecraft:creeper, minecraft:spider")
			.build();
		
		public static final ConfigProperty<Set<TagKey<EntityType<?>>>> tagSet = entityTypeTagKeySetOpt("tagSet", Collections.emptySet(), "A comma-separated set of entity type tags.")
			.example("minecraft:raiders, some_datapack:some_tag")
			.build();
		
		public static void visit(ConfigSchema schema) {
			schema.getSection("Mob Set Rule").add(0, mobSet);
			schema.getSection("Tag Set Rule").add(0, tagSet);
		}
	}
}
