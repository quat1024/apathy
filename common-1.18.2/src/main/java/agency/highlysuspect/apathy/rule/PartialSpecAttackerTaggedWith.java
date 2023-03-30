package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSerializer;
import agency.highlysuspect.apathy.core.rule.PartialSpec;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecAttackerTaggedWith(Set<TagKey<EntityType<?>>> tags) implements PartialSpec<PartialSpecAttackerTaggedWith> {
	@Override
	public PartialSpec<?> optimize() {
		if(tags.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			EntityType<?> type = VerConv.type(attacker);
			
			for(TagKey<EntityType<?>> tag : tags) if(type.is(tag)) return true;
			return false;
		};
	}
	
	@Override
	public PartialSerializer<PartialSpecAttackerTaggedWith> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAttackerTaggedWith> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerTaggedWith part, JsonObject json) {
			json.add("tags", part.tags.stream()
				.map(TagKey::location)
				.map(rl -> new JsonPrimitive(rl.toString()))
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecAttackerTaggedWith read(JsonObject json) {
			return new PartialSpecAttackerTaggedWith(StreamSupport.stream(json.getAsJsonArray("tags").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(ResourceLocation::new)
				.map(rl -> TagKey.create(Registry.ENTITY_TYPE_REGISTRY, rl))
				.collect(Collectors.toSet()));
		}
	}
}
