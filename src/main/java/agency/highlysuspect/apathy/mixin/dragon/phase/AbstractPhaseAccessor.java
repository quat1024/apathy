package agency.highlysuspect.apathy.mixin.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractPhase.class)
public interface AbstractPhaseAccessor {
	@Accessor("dragon") EnderDragonEntity apathy$getDragon();
}
