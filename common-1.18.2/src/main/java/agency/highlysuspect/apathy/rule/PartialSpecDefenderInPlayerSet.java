package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSerializer;
import agency.highlysuspect.apathy.core.rule.PartialSpec;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecDefenderInPlayerSet(Set<String> playerSetNames) implements PartialSpec<PartialSpecDefenderInPlayerSet> {
	@Override
	public PartialSpec<?> optimize() {
		if(playerSetNames.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerPlayer defenderSp = (ServerPlayer) defender.apathy$getServerPlayer();
			
			MinecraftServer server = defenderSp.getServer();
			assert server != null;
			
			PlayerSetManager setManager = PlayerSetManager.getFor(server);
			for(String playerSetName : playerSetNames) {
				if(setManager.playerInSet(defenderSp, playerSetName)) return true;
			}
			return false;
		};
	}
	
	@Override
	public PartialSerializer<PartialSpecDefenderInPlayerSet> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecDefenderInPlayerSet> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecDefenderInPlayerSet part, JsonObject json) {
			json.add("player_sets", part.playerSetNames.stream()
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecDefenderInPlayerSet read(JsonObject json) {
			return new PartialSpecDefenderInPlayerSet(StreamSupport.stream(json.getAsJsonArray("player_sets").spliterator(), false)
				.map(JsonElement::getAsString)
				.collect(Collectors.toSet()));
		}
	}
}
