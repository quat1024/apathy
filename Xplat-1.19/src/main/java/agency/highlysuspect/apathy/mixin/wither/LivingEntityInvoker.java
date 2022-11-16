package agency.highlysuspect.apathy.mixin.wither;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
	//Used to produce authentic Wither drops.
	@Invoker("dropAllDeathLoot") void apathy$dropAllDeathLoot(DamageSource source);
}
