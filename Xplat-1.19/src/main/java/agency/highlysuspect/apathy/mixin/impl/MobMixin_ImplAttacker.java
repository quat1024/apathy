package agency.highlysuspect.apathy.mixin.impl;

import agency.highlysuspect.apathy.hell.rule.Attacker;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public class MobMixin_ImplAttacker implements Attacker {
	@Override
	public Object apathy$getMob() {
		return this;
	}
}
