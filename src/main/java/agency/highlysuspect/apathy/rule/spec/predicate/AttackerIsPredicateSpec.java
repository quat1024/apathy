package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import java.util.Objects;
import java.util.Set;

public final class AttackerIsPredicateSpec implements PredicateSpec {
	public static final Codec<AttackerIsPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Registry.ENTITY_TYPE.getCodec()).fieldOf("mobs").forGetter(x -> x.mobSet)
	).apply(i, AttackerIsPredicateSpec::new));
	private final Set<EntityType<?>> mobSet;
	
	public AttackerIsPredicateSpec(Set<EntityType<?>> mobSet) {this.mobSet = mobSet;}
	
	@Override
	public PredicateSpec optimize() {
		if(mobSet.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> mobSet.contains(attacker.getType());
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	public Set<EntityType<?>> mobSet() {return mobSet;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (AttackerIsPredicateSpec) obj;
		return Objects.equals(this.mobSet, that.mobSet);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mobSet);
	}
	
	@Override
	public String toString() {
		return "AttackerIsPredicateSpec[" +
			"mobSet=" + mobSet + ']';
	}
	
}
