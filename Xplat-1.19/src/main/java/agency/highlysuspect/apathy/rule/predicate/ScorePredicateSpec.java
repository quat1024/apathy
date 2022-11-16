package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.ThresholdMode;
import agency.highlysuspect.apathy.rule.Who;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public record ScorePredicateSpec(String scoreboardObjective, Who who, ThresholdMode thresholdMode, int threshold) implements PredicateSpec {
	public static final Codec<ScorePredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("objective").forGetter(ScorePredicateSpec::scoreboardObjective),
		Who.CODEC.optionalFieldOf("who", Who.DEFENDER).forGetter(ScorePredicateSpec::who),
		ThresholdMode.CODEC.fieldOf("thresholdMode").forGetter(ScorePredicateSpec::thresholdMode),
		Codec.INT.fieldOf("threshold").forGetter(ScorePredicateSpec::threshold)
	).apply(i, ScorePredicateSpec::new));
	
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
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
