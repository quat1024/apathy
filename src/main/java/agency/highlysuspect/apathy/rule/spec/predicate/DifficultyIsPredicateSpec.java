package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.Set;

public record DifficultyIsPredicateSpec(Set<Difficulty> difficulties) implements PredicateSpec {
	
	public static final Codec<DifficultyIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(CodecUtil.DIFFICULTY).fieldOf("difficulties").forGetter(x -> x.difficulties)
	).apply(i, DifficultyIsPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(difficulties.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return PredicateSpec.sizeSpecializeNotEmpty(difficulties,
			difficulty -> (attacker, defender) -> attacker.level.getDifficulty() == difficulty,
			set -> (attacker, defender) -> set.contains(attacker.level.getDifficulty())
		);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
