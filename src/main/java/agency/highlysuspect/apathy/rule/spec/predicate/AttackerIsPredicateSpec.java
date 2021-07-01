package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import java.util.Set;

public class AttackerIsPredicateSpec extends PredicateSpec {
	public AttackerIsPredicateSpec(Set<EntityType<?>> mobSet) {
		this.mobSet = mobSet;
	}
	
	private final Set<EntityType<?>> mobSet;
	
	public static final Codec<AttackerIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Registry.ENTITY_TYPE).fieldOf("mobs").forGetter(x -> x.mobSet)
	).apply(i, AttackerIsPredicateSpec::new));
	
	@Override
	public Partial buildPartial() {
		return Partial.attackerIsAny(mobSet);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
