package agency.highlysuspect.apathy.rule;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiPredicate;

public interface Partial extends BiPredicate<MobEntity, ServerPlayerEntity> {
	Partial ALWAYS_TRUE = (attacker, defender) -> true;
	Partial ALWAYS_FALSE = (attacker, defender) -> false;
	
	static Partial always(boolean always) {
		return always ? ALWAYS_TRUE : ALWAYS_FALSE;
	}
}
