package agency.highlysuspect.apathy;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.world.Difficulty;

public interface Rule {
	Result check(MobEntity attacker, PlayerEntity target);
	
	enum Result {
		//The rule applies, and says "stop checking other rules, the attacker is *always* allowed to attack."
		ALLOW_ATTACK,
		//The rule does not apply in this situation.
		DONT_CARE,
		//The rule applies, and says "stop checking other rules, the attacker *is not* allowed to attack."
		DENY_ATTACK,
		;
		
		public static Result allowIf(boolean b) {
			return b ? ALLOW_ATTACK : DONT_CARE;
		}
		
		public static Result denyIf(boolean b) {
			return b ? DENY_ATTACK : DONT_CARE;
		}
		
		public Result appliesIf(boolean b) {
			return b ? this : Result.DONT_CARE;
		}
		
		public void youMustCare() {
			if(this == DONT_CARE) throw new IllegalArgumentException("Result.DONT_CARE would create a no-op rule");
		}
	}
	
	static Rule attackerHasTag(Tag<EntityType<?>> tag, Result result) {
		result.youMustCare();
		return (attacker, target) -> result.appliesIf(tag.contains(attacker.getType()));
	}
	
	static Rule attackerIs(EntityType<?> type, Result result) {
		result.youMustCare();
		return (attacker, target) -> result.appliesIf(type.equals(attacker.getType()));
	}
	
	static Rule attackerRevenge(int timeout) {
		throw new RuntimeException("NYI");
	}
	
	static Rule difficulty(Difficulty diff) {
		return (attacker, target) -> Result.denyIf(attacker.world.getDifficulty().equals(diff));
	}
	
	
	
	class PlayerList implements Rule {
		@Override
		public Result check(MobEntity attacker, PlayerEntity target) {
			//TODO: Implement player entity data checking.
			return Result.DONT_CARE;
		}
	}
}
