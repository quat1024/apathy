package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import java.util.Set;

public record AttackerIsPredicateSpec(Set<EntityType<?>> mobSet) implements PredicateSpec {
	public static final Codec<AttackerIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Registry.ENTITY_TYPE).fieldOf("mobs").forGetter(x -> x.mobSet)
	).apply(i, AttackerIsPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(mobSet.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return PredicateSpec.sizeSpecializeNotEmpty(mobSet,
			type -> (attacker, defender) -> attacker.getType().equals(type),
			set -> (attacker, defender) -> set.contains(attacker.getType())
		);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
