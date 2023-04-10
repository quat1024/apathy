package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecDefenderInPlayerSet(Set<String> playerSetNames) implements Spec<Partial, PartialSpecDefenderInPlayerSet> {
	@Override
	public Spec<Partial, ?> optimize() {
		if(playerSetNames.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerPlayer defenderSp = VerConv.player(defender);
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
	public JsonSerializer<PartialSpecDefenderInPlayerSet> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecDefenderInPlayerSet> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecDefenderInPlayerSet thing, JsonObject json) {
			json.add("player_sets", thing.playerSetNames.stream()
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
