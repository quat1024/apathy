package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecAttackerIs(Set<EntityType<?>> mobSet) implements Spec<Partial, PartialSpecAttackerIs> {
	@Override
	public Spec<Partial, ?> optimize() {
		if(mobSet.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> mobSet.contains(VerConv.type(attacker));
	}
	
	@Override
	public JsonSerializer<PartialSpecAttackerIs> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAttackerIs> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerIs thing, JsonObject json) {
			json.add("mobs", thing.mobSet.stream()
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
