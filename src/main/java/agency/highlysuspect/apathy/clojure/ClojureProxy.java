package agency.highlysuspect.apathy.clojure;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface ClojureProxy {
	TriState allowedToTargetPlayer(MobEntity attacker, PlayerEntity player);
	
	ClojureProxy NO_CLOJURE = (attacker, player) -> {
		throw new IllegalStateException("allowedToTargetPlayer on NO_CLOJURE proxy; call through config");
	};
}
