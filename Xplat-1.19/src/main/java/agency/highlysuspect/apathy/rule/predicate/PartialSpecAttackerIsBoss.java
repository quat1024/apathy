package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record PartialSpecAttackerIsBoss() implements PartialSpec<PartialSpecAttackerIsBoss> {
	public static final PartialSpecAttackerIsBoss INSTANCE = new PartialSpecAttackerIsBoss();
	
	public static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, Apathy119.id("bosses"));
	
	@Override
	public Partial build() {
		return (attacker, defender) -> attacker.getType().is(BOSS_TAG);
	}
	
	@Override
	public PartialSerializer<PartialSpecAttackerIsBoss> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAttackerIsBoss> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerIsBoss asdkashd, JsonObject json) {
			//Nothing to write
		}
		
		@Override
		public PartialSpecAttackerIsBoss read(JsonObject json) {
			return PartialSpecAttackerIsBoss.INSTANCE;
		}
	}
}
