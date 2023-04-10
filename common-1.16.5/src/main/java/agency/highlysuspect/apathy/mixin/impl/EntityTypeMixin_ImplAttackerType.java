package agency.highlysuspect.apathy.mixin.impl;

import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityType.class)
public class EntityTypeMixin_ImplAttackerType implements AttackerType {
	@Override
	public Object apathy$underlyingObject() {
		return this;
	}
	
	@Override
	public boolean apathy$hasType(Attacker mob) {
		Entity ent = (Entity) mob.apathy$underlyingObject();
		return ent.getType() == (Object) this;
	}
	
	@Override
	public String apathy$id() {
		return Registry.ENTITY_TYPE.getKey((EntityType<?>) (Object) this).toString();
	}
}
