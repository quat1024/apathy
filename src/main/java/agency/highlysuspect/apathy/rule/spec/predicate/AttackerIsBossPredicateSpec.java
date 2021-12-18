package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

public record AttackerIsBossPredicateSpec() implements PredicateSpec {
	public static final AttackerIsBossPredicateSpec INSTANCE = new AttackerIsBossPredicateSpec();
	public static final Codec<AttackerIsBossPredicateSpec> CODEC = Codec.unit(INSTANCE);
	
	public static final Tag<EntityType<?>> BOSS_TAG = TagFactory.ENTITY_TYPE.create(Apathy.id("bosses"));
	
	@Override
	public Partial build() {
		return (attacker, defender) -> BOSS_TAG.contains(attacker.getType());
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
