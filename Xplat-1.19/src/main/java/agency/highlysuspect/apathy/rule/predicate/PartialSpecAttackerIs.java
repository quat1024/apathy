package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.rule.CodecUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecAttackerIs(Set<EntityType<?>> mobSet) implements PartialSpec<PartialSpecAttackerIs> {
	@Override
	public PartialSpec<?> optimize() {
		if(mobSet.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> mobSet.contains(attacker.getType());
	}
	
	@Override
	public PartialSerializer<PartialSpecAttackerIs> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAttackerIs> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public JsonObject write(PartialSpecAttackerIs part, JsonObject json) {
			json.add("mobs", part.mobSet.stream()
				.map(Registry.ENTITY_TYPE::getKey)
				.map(rl -> new JsonPrimitive(rl.toString()))
				.collect(CoolGsonHelper.toJsonArray()));
			return json;
		}
		
		@Override
		public PartialSpecAttackerIs read(JsonObject json) {
			return new PartialSpecAttackerIs(StreamSupport.stream(json.getAsJsonArray("mobs").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(ResourceLocation::new)
				.map(Registry.ENTITY_TYPE::get)
				.collect(Collectors.toSet()));
		}
	}
	
	///CODEC HELL ///
	@Deprecated(forRemoval = true)
	public static final Codec<PartialSpecAttackerIs> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Registry.ENTITY_TYPE.byNameCodec()).fieldOf("mobs").forGetter(x -> x.mobSet)
	).apply(i, PartialSpecAttackerIs::new));
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends PartialSpec<?>> codec() {
		return CODEC;
	}
}
