package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.Tag;

public class AttackerIsBossPredicateSpec implements PredicateSpec {
	public static final AttackerIsBossPredicateSpec INSTANCE = new AttackerIsBossPredicateSpec();
	public static final Codec<AttackerIsBossPredicateSpec> CODEC = Codec.unit(INSTANCE);
	
	public static final Tag<EntityType<?>> BOSS_TAG = TagRegistry.entityType(Init.id("bosses"));
	
	@Override
	public Partial build() {
		return (attacker, defender) -> BOSS_TAG.contains(attacker.getType());
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}