package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy;
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
	
	@SuppressWarnings("SimplifiableConditionalExpression") //intellij suggests something truly awful
	@Override
	@Intrinsic(displace = true) //mixin wizards feel free to correct me on this
	public boolean canAttack(LivingEntity other) {
		return super.canAttack(other) &&
			(other instanceof Villager ? Apathy.INSTANCE.generalConfig.zombieAttackVillagerDifficulties.contains(level.getDifficulty()) : true);
	}
}
