package agency.highlysuspect.apathy.mixin.impl;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public class MobMixin_ImplAttacker implements Attacker {
	@Override
	public Object apathy$underlyingObject() {
		return this;
	}
	
	@Override
	public ApathyDifficulty apathy$getDifficulty() {
		return VerConv.toApathyDifficulty(((Mob) (Object) this).level.getDifficulty());
	}
	
	@Override
	public AttackerType apathy$getType() {
		return (AttackerType) ((Mob) (Object) this).getType();
	}
}
