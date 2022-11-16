package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.Set;

public record DifficultyIsPredicateSpec(Set<Difficulty> difficulties) implements PartialSpec {
	public static final Codec<DifficultyIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(CodecUtil.DIFFICULTY).fieldOf("difficulties").forGetter(x -> x.difficulties)
	).apply(i, DifficultyIsPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		if(difficulties.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> difficulties.contains(attacker.level.getDifficulty());
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
