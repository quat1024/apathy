package agency.highlysuspect.apathy.mixin.impl;

import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TagKey.class)
public class TagKeyMixin_ImplAttackerTag implements AttackerTag {
	@Override
	public Object apathy$underlyingObject() {
		return this;
	}
	
	@Override
	public boolean apathy$is(Attacker attacker) {
		EntityType<?> under = (EntityType<?>) attacker.apathy$getType().apathy$underlyingObject();
		return under.is(((TagKey<EntityType<?>>) (Object) this));
	}
	
	@Override
	public String apathy$id() {
		return ((TagKey<?>) (Object) this).location().toString();
	}
}
