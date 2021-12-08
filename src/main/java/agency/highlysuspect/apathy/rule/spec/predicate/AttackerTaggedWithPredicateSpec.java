package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;

public record AttackerTaggedWithPredicateSpec(Set<Tag<EntityType<?>>> tags) implements PredicateSpec {
	public static final Codec<AttackerTaggedWithPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(
			Tag.codec(() -> ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.ENTITY_TYPE_KEY))
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
