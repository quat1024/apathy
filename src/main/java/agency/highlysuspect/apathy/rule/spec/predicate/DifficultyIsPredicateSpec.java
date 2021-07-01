package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.Set;

public class DifficultyIsPredicateSpec extends PredicateSpec {
	public DifficultyIsPredicateSpec(Set<Difficulty> difficulties) {
		this.difficulties = difficulties;
	}
	
	private final Set<Difficulty> difficulties;
	
	public static final Codec<DifficultyIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(CodecUtil.DIFFICULTY).fieldOf("difficulties").forGetter(x -> x.difficulties)
	).apply(i, DifficultyIsPredicateSpec::new));
	
	@Override
	public Partial buildPartial() {
		return Partial.difficultyIsAny(difficulties);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
