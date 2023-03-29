package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.CoreConv;
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
		return (attacker, defender) -> mobSet.contains(CoreConv.type(attacker));
	}
	
	@Override
	public PartialSerializer<PartialSpecAttackerIs> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAttackerIs> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerIs part, JsonObject json) {
			json.add("mobs", part.mobSet.stream()
				.map(Registry.ENTITY_TYPE::getKey)
				.map(rl -> new JsonPrimitive(rl.toString()))
				.collect(CoolGsonHelper.toJsonArray()));
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
}
