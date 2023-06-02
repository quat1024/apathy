package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.coreplusminecraft.MinecraftConv;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Mob {
	protected ZombieMixin(EntityType<? extends Mob> $$0, Level $$1) {
		super($$0, $$1);
		throw new IllegalStateException("dummy constructor for mixin, shouldn't ever get called");
	}
	
	@Override
	@Intrinsic(displace = true) //mixin wizards feel free to correct me on this
	public boolean canAttack(LivingEntity other) {
		boolean result = super.canAttack(other);
		
		if(result && other instanceof Villager) {
			result = Apathy.instance.generalCfg.get(CoreGenOptions.zombieAttackVillagerDifficulties)
				.contains(MinecraftConv.toApathyDifficulty(level.getDifficulty()));	
		}
		
		return result;
	}
}
