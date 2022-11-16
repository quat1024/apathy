package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

public record AttackerIsPredicateSpec(Set<EntityType<?>> mobSet) implements PartialSpec {
	public static final Codec<AttackerIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Registry.ENTITY_TYPE.byNameCodec()).fieldOf("mobs").forGetter(x -> x.mobSet)
	).apply(i, AttackerIsPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		if(mobSet.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> mobSet.contains(attacker.getType());
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
