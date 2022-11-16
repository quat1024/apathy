package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

public record AttackerTaggedWithPredicateSpec(Set<TagKey<EntityType<?>>> tags) implements PartialSpec {
	public static final Codec<AttackerTaggedWithPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(TagKey.codec(Registry.ENTITY_TYPE_REGISTRY)).fieldOf("tags").forGetter(x -> x.tags)
	).apply(i, AttackerTaggedWithPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		if(tags.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			for(TagKey<EntityType<?>> tag : tags) {
				if(attacker.getType().is(tag)) return true;
			}
			return false;
		};
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
