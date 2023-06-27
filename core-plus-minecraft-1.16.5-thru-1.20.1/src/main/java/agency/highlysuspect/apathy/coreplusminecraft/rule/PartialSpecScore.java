package agency.highlysuspect.apathy.coreplusminecraft.rule;

import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.rule.ThresholdMode;
import agency.highlysuspect.apathy.core.rule.Who;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.MinecraftConv;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class PartialSpecScore implements Spec<Partial, PartialSpecScore> {
	public PartialSpecScore(String scoreboardObjective, Who who, ThresholdMode thresholdMode, int threshold) {
		this.scoreboardObjective = scoreboardObjective;
		this.who = who;
		this.thresholdMode = thresholdMode;
		this.threshold = threshold;
	}
	
	private final String scoreboardObjective;
	private final Who who;
	private final ThresholdMode thresholdMode;
	private final int threshold;
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			Scoreboard scoreboard = MinecraftConv.level(defender).getScoreboard();
			Objective objective = scoreboard.getObjective(scoreboardObjective);
			if(objective == null) return false;
			
			Entity which = who.choose(MinecraftConv.mob(attacker), MinecraftConv.player(defender));
			String scoreboardName = which.getScoreboardName();
			
			int score = scoreboard.hasPlayerScore(scoreboardName, objective) ? scoreboard.getOrCreatePlayerScore(scoreboardName, objective).getScore() : 0;
			
			return thresholdMode.test(score, threshold);
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecScore> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecScore> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecScore thing, JsonObject json) {
			json.addProperty("objective", thing.scoreboardObjective);
			json.addProperty("who", thing.who.toString()); //optional but serialized unconditionally
			json.addProperty("thresholdMode", thing.thresholdMode.toString());
			json.addProperty("threshold", thing.threshold);
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
