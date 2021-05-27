package agency.highlysuspect.apathy.clojure;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;

public interface ClojureProxy {
	boolean isClojure();
	TriState allowedToTargetPlayer(MobEntity attacker, PlayerEntity player);
	
	ClojureProxy NO_CLOJURE = new ClojureProxy() {
		@Override
		public boolean isClojure() {
			return false;
		}
		
		@Override
		public TriState allowedToTargetPlayer(MobEntity attacker, PlayerEntity player) {
			throw new IllegalStateException("allowedToTargetPlayer on NO_CLOJURE proxy; call through config");
		}
	};
}
