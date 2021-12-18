package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

public final class AttackerTaggedWithPredicateSpec implements PredicateSpec {
	public static final Codec<AttackerTaggedWithPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(
			Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.ENTITY_TYPE_REGISTRY))
		).fieldOf("tags").forGetter(x -> x.tags)
	).apply(i, AttackerTaggedWithPredicateSpec::new));
	private final Set<Tag<EntityType<?>>> tags;
	
	public AttackerTaggedWithPredicateSpec(Set<Tag<EntityType<?>>> tags) {this.tags = tags;}
	
	@Override
	public PredicateSpec optimize() {
		if(tags.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			for(Tag<EntityType<?>> tag : tags) {
				if(tag.contains(attacker.getType())) return true;
			}
			return false;
		};
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	public Set<Tag<EntityType<?>>> tags() {return tags;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (AttackerTaggedWithPredicateSpec) obj;
		return Objects.equals(this.tags, that.tags);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tags);
	}
	
	@Override
	public String toString() {
		return "AttackerTaggedWithPredicateSpec[" +
			"tags=" + tags + ']';
	}
	
}
