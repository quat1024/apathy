package agency.highlysuspect.apathy.mixin.impl;

import agency.highlysuspect.apathy.CoreConv;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public class MobMixin_ImplAttacker implements Attacker {
	@Override
	public Object apathy$getMob() {
		return this;
	}
	
	@Override
	public Object apathy$getEntityType() {
		return ((Mob) (Object) this).getType();
	}
	
	@Override
	public ApathyDifficulty apathy$getDifficulty() {
		return CoreConv.toApathyDifficulty(((Mob) (Object) this).level.getDifficulty());
	}
}
