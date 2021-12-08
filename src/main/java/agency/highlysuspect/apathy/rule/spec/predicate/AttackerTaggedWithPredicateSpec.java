package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

public record AttackerTaggedWithPredicateSpec(Set<Tag<EntityType<?>>> tags) implements PredicateSpec {
	public static final Codec<AttackerTaggedWithPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(
			Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.ENTITY_TYPE_REGISTRY))
		).fieldOf("tags").forGetter(x -> x.tags)
	).apply(i, AttackerTaggedWithPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(tags.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return PredicateSpec.sizeSpecializeNotEmpty(tags,
			tag -> (attacker, defender) -> tag.contains(attacker.getType()),
			set -> (attacker, defender) -> {
				for(Tag<EntityType<?>> tag : set) {
					if(tag.contains(attacker.getType())) return true;
				}
				return false;
			}
		);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
