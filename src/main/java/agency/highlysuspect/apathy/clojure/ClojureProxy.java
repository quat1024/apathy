package agency.highlysuspect.apathy.clojure;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ClojureProxy {
	TriState allowedToTargetPlayer(MobEntity attacker, ServerPlayerEntity player);
	
	ClojureProxy NO_CLOJURE = (attacker, player) -> {
		throw new IllegalStateException("allowedToTargetPlayer on NO_CLOJURE proxy; call through config");
	};
}
