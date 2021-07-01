package agency.highlysuspect.apathy.mixin.wither;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
	//Used to produce authentic Wither drops.
	@Invoker("dropEquipment") void apathy$dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops);
}
