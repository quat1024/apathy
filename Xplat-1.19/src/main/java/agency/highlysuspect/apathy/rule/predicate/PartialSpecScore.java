package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.rule.ThresholdMode;
import agency.highlysuspect.apathy.rule.Who;
import com.google.gson.JsonObject;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public record PartialSpecScore(String scoreboardObjective, Who who, ThresholdMode thresholdMode, int threshold) implements PartialSpec<PartialSpecScore> {
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			Scoreboard scoreboard = attacker.level.getScoreboard();
			Objective objective = scoreboard.getObjective(scoreboardObjective);
			if(objective == null) return false;
			
			String scoreboardName = who.choose(attacker, defender).getScoreboardName();
			int score = scoreboard.hasPlayerScore(scoreboardName, objective) ? scoreboard.getOrCreatePlayerScore(scoreboardName, objective).getScore() : 0;
			
			return thresholdMode.test(score, threshold);
		};
	}
	
	@Override
	public PartialSerializer<PartialSpecScore> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecScore> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public JsonObject write(PartialSpecScore part, JsonObject json) {
			json.addProperty("objective", part.scoreboardObjective);
			json.addProperty("who", part.who.toString()); //optional but serialized unconditionally
			json.addProperty("thresholdMode", part.thresholdMode.toString());
			json.addProperty("threshold", part.threshold);
			return json;
		}
		
		@Override
		public PartialSpecScore read(JsonObject json) {
			String scoreboardObjective = json.get("objective").getAsString();
			Who who = json.has("who") ? Who.fromString(json.get("who").getAsString()) : Who.DEFENDER;
			ThresholdMode thresholdMode = ThresholdMode.fromString(json.get("thresholdMode").getAsString());
			int threshold = json.get("threshold").getAsInt();
			
			return new PartialSpecScore(scoreboardObjective, who, thresholdMode, threshold);
		}
	}
}
