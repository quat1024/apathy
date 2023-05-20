package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.JsonObject;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.PlayerTeam;

@SuppressWarnings("ClassCanBeRecord")
public class PartialSpecScoreboardTeam implements Spec<Partial, PartialSpecScoreboardTeam> {
	public PartialSpecScoreboardTeam(String teamName) {
		this.teamName = teamName;
	}
	
	private final String teamName;
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerLevel level = VerConv.level(defender);
			ServerScoreboard scoreboard = level.getScoreboard();
			
			PlayerTeam team = scoreboard.getPlayersTeam(defender.apathy$scoreboardName());
			return team != null && team.getName().equals(teamName);
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
			json.addProperty("team", thing.teamName);
		}
		
		@Override
		public PartialSpecScoreboardTeam read(JsonObject json) {
			return new PartialSpecScoreboardTeam(json.get("team").getAsString());
		}
	}
}
