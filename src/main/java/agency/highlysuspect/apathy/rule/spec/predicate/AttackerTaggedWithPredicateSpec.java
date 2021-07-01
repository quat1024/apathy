package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;

import java.util.Set;

public class AttackerTaggedWithPredicateSpec extends PredicateSpec {
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
	public Partial buildPartial() {
		return Partial.attackerTaggedWithAny(tags);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
