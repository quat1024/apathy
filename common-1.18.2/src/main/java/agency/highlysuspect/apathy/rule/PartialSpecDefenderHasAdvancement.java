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
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("ClassCanBeRecord")
public class PartialSpecDefenderHasAdvancement implements Spec<Partial, PartialSpecDefenderHasAdvancement> {
	public PartialSpecDefenderHasAdvancement(Set<ResourceLocation> advancementIds) {
		this.advancementIds = advancementIds;
	}
	
	private final Set<ResourceLocation> advancementIds;
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerPlayer defenderSp = VerConv.player(defender); 
			
			MinecraftServer server = defenderSp.server;
			ServerAdvancementManager serverAdvancementManager = server.getAdvancements();
			PlayerAdvancements playerAdvancements = defenderSp.getAdvancements();
			
			for(ResourceLocation advancementId : advancementIds) {
				Advancement adv = serverAdvancementManager.getAdvancement(advancementId);
				if(adv == null) continue;
				if(playerAdvancements.getOrStartProgress(adv).isDone()) return true;
			}
			return false;
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecDefenderHasAdvancement> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecDefenderHasAdvancement> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecDefenderHasAdvancement thing, JsonObject json) {
			json.add("advancements", thing.advancementIds.stream()
				.map(rl -> new JsonPrimitive(rl.toString()))
				.collect(CoolGsonHelper.toJsonArray()));
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
