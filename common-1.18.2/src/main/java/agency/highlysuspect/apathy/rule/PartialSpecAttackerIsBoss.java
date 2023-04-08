package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record PartialSpecAttackerIsBoss() implements Spec<Partial, PartialSpecAttackerIsBoss> {
	public static final PartialSpecAttackerIsBoss INSTANCE = new PartialSpecAttackerIsBoss();
	
	public static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Apathy.MODID, "bosses"));
	
	@Override
	public Partial build() {
		return (attacker, defender) -> VerConv.type(attacker).is(BOSS_TAG);
	}
	
	@Override
	public JsonSerializer<PartialSpecAttackerIsBoss> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAttackerIsBoss> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerIsBoss thing, JsonObject json) {
			//Nothing to write
		}
		
		@Override
		public PartialSpecAttackerIsBoss read(JsonObject json) {
			return PartialSpecAttackerIsBoss.INSTANCE;
		}
	}
}
