package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record AttackerIsBossPredicateSpec() implements PredicateSpec {
	public static final AttackerIsBossPredicateSpec INSTANCE = new AttackerIsBossPredicateSpec();
	public static final Codec<AttackerIsBossPredicateSpec> CODEC = Codec.unit(INSTANCE);
	
	public static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, Apathy.id("bosses"));
	
	@Override
	public Partial build() {
		return (attacker, defender) -> attacker.getType().is(BOSS_TAG);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
