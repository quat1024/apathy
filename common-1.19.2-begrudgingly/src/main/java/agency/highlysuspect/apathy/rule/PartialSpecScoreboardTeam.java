package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class PartialSpecScoreboardTeam implements Spec<Partial, PartialSpecScoreboardTeam> {
	public PartialSpecScoreboardTeam(Set<String> teamNames) {
		this.teamNames = teamNames;
	}
	
	private final Set<String> teamNames;
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerLevel level = VerConv.level(defender);
			ServerScoreboard scoreboard = level.getScoreboard();
			
			PlayerTeam team = scoreboard.getPlayersTeam(defender.apathy$scoreboardName());
			return team != null && teamNames.contains(team.getName());
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecScoreboardTeam> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecScoreboardTeam> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecScoreboardTeam thing, JsonObject json) {
			json.add("teams", thing.teamNames.stream()
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecScoreboardTeam read(JsonObject json) {
			if(json.has("team")) return new PartialSpecScoreboardTeam(Collections.singleton(json.getAsJsonPrimitive("teams").getAsString()));
			else return new PartialSpecScoreboardTeam(CoolGsonHelper.streamArray(json.getAsJsonArray("teams"))
				.map(JsonElement::getAsString)
				.collect(Collectors.toSet()));
		}
	}
}
