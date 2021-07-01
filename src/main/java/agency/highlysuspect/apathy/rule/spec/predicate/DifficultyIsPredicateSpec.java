package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.RuleUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.Set;

public class DifficultyIsPredicateSpec implements PredicateSpec {
	public DifficultyIsPredicateSpec(Set<Difficulty> difficulties) {
		this.difficulties = difficulties;
	}
	
	private final Set<Difficulty> difficulties;
	
	public static final Codec<DifficultyIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(CodecUtil.DIFFICULTY).fieldOf("difficulties").forGetter(x -> x.difficulties)
	).apply(i, DifficultyIsPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(difficulties.isEmpty()) return ALWAYS_FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return RuleUtil.sizeSpecializeNotEmpty(difficulties,
			difficulty -> (attacker, defender) -> attacker.world.getDifficulty() == difficulty,
			set -> (attacker, defender) -> set.contains(attacker.world.getDifficulty())
		);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
