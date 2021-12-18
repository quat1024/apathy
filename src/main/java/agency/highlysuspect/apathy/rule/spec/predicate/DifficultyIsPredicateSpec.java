package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.Objects;
import java.util.Set;

public final class DifficultyIsPredicateSpec implements PredicateSpec {
	
	public static final Codec<DifficultyIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(CodecUtil.DIFFICULTY).fieldOf("difficulties").forGetter(x -> x.difficulties)
	).apply(i, DifficultyIsPredicateSpec::new));
	private final Set<Difficulty> difficulties;
	
	public DifficultyIsPredicateSpec(Set<Difficulty> difficulties) {this.difficulties = difficulties;}
	
	@Override
	public PredicateSpec optimize() {
		if(difficulties.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> difficulties.contains(attacker.level.getDifficulty());
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	public Set<Difficulty> difficulties() {return difficulties;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (DifficultyIsPredicateSpec) obj;
		return Objects.equals(this.difficulties, that.difficulties);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(difficulties);
	}
	
	@Override
	public String toString() {
		return "DifficultyIsPredicateSpec[" +
			"difficulties=" + difficulties + ']';
	}
	
}
