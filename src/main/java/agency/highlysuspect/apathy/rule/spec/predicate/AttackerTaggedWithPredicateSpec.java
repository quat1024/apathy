package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;

import java.util.Set;

public class AttackerTaggedWithPredicateSpec implements PredicateSpec {
	public AttackerTaggedWithPredicateSpec(Set<Tag<EntityType<?>>> tags) {
		this.tags = tags;
	}
	
	private final Set<Tag<EntityType<?>>> tags;
	
	public static final Codec<AttackerTaggedWithPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(
			Tag.codec(() -> ServerTagManagerHolder.getTagManager().getEntityTypes())
		).fieldOf("tags").forGetter(x -> x.tags)
	).apply(i, AttackerTaggedWithPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(tags.isEmpty()) return ALWAYS_FALSE;
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
