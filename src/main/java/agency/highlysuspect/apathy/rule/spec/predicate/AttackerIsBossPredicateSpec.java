package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.Tag;

public final class AttackerIsBossPredicateSpec implements PredicateSpec {
	public static final AttackerIsBossPredicateSpec INSTANCE = new AttackerIsBossPredicateSpec();
	public static final Codec<AttackerIsBossPredicateSpec> CODEC = Codec.unit(INSTANCE);
	
	public static final Tag<EntityType<?>> BOSS_TAG = TagFactory.ENTITY_TYPE.create(Init.id("bosses"));
	
	public AttackerIsBossPredicateSpec() {}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> BOSS_TAG.contains(attacker.getType());
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || obj != null && obj.getClass() == this.getClass();
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public String toString() {
		return "AttackerIsBossPredicateSpec[]";
	}
	
}
