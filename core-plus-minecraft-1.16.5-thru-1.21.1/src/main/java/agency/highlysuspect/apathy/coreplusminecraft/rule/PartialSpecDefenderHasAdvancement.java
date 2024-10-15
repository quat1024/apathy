package agency.highlysuspect.apathy.coreplusminecraft.rule;

import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyCommands;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.MinecraftConv;
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

public class PartialSpecDefenderHasAdvancement implements Spec<Partial, PartialSpecDefenderHasAdvancement> {
	public PartialSpecDefenderHasAdvancement(Set<ResourceLocation> advancementIds) {
		this.advancementIds = advancementIds;
	}
	
	private final Set<ResourceLocation> advancementIds;
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerPlayer defenderSp = MinecraftConv.player(defender); 
			
			MinecraftServer server = defenderSp.server;
			ServerAdvancementManager serverAdvancementManager = server.getAdvancements();
			PlayerAdvancements playerAdvancements = defenderSp.getAdvancements();
			
			for(ResourceLocation advancementId : advancementIds) {
				if(!ApathyPlusMinecraft.instanceMinecraft.doesAdvancementExist(serverAdvancementManager, advancementId)) continue;
				if(ApathyPlusMinecraft.instanceMinecraft.isAdvancementDone(playerAdvancements, serverAdvancementManager, advancementId)) return true;
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
			return new PartialSpecDefenderHasAdvancement(CoolGsonHelper.streamArray(json.getAsJsonArray("advancements"))
				.map(JsonElement::getAsString)
				.map(ApathyPlusMinecraft.instanceMinecraft::resource)
				.collect(Collectors.toSet()));
		}
	}
}
