package agency.highlysuspect.apathy.rule;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiFunction;

public interface Rule extends BiFunction<MobEntity, ServerPlayerEntity, TriState> {
	Rule ALWAYS_ALLOW = (attacker, defender) -> TriState.TRUE;
	Rule ALWAYS_DENY = (attacker, defender) -> TriState.FALSE;
	Rule ALWAYS_PASS = (attacker, defender) -> TriState.DEFAULT;
	
	static Rule always(TriState which) {
		switch(which) {
			case FALSE: return ALWAYS_DENY;
			case DEFAULT: return ALWAYS_PASS;
			case TRUE: return ALWAYS_ALLOW;
			default: throw new IllegalStateException(which.name());
		}
	}
}