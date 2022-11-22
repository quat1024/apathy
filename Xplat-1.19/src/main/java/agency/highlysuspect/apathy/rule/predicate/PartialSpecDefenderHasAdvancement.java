package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecDefenderHasAdvancement(Set<ResourceLocation> advancementIds) implements PartialSpec<PartialSpecDefenderHasAdvancement> {
	@Override
	public PartialSpec<?> optimize() {
		if(advancementIds.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			MinecraftServer server = defender.server;
			ServerAdvancementManager serverAdvancementManager = server.getAdvancements();
			PlayerAdvancements playerAdvancements = defender.getAdvancements();
			
			for(ResourceLocation advancementId : advancementIds) {
				Advancement adv = serverAdvancementManager.getAdvancement(advancementId);
				if(adv == null) continue;
				if(playerAdvancements.getOrStartProgress(adv).isDone()) return true;
			}
			return false;
		};
	}
	
	@Override
	public PartialSerializer<PartialSpecDefenderHasAdvancement> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecDefenderHasAdvancement> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public JsonObject write(PartialSpecDefenderHasAdvancement part, JsonObject json) {
			json.add("advancements", part.advancementIds.stream()
				.map(rl -> new JsonPrimitive(rl.toString()))
				.collect(CoolGsonHelper.toJsonArray()));
			return json;
		}
		
		@Override
		public PartialSpecDefenderHasAdvancement read(JsonObject json) {
			return new PartialSpecDefenderHasAdvancement(StreamSupport.stream(json.getAsJsonArray("advancements").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(ResourceLocation::new)
				.collect(Collectors.toSet()));
		}
	}
}
