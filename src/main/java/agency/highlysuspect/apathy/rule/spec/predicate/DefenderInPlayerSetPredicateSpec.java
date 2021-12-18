package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.playerset.PlayerSet;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;
import java.util.Set;

public final class DefenderInPlayerSetPredicateSpec implements PredicateSpec {
	public static final Codec<DefenderInPlayerSetPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Codec.STRING).fieldOf("player_sets").forGetter(x -> x.playerSetNames)
	).apply(i, DefenderInPlayerSetPredicateSpec::new));
	private final Set<String> playerSetNames;
	
	public DefenderInPlayerSetPredicateSpec(Set<String> playerSetNames) {this.playerSetNames = playerSetNames;}
	
	@Override
	public PredicateSpec optimize() {
		if(playerSetNames.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			MinecraftServer server = defender.getServer();
			assert server != null;
			
			PlayerSetManager setManager = PlayerSetManager.getFor(server);
			for(String playerSetName : playerSetNames) {
				PlayerSet playerSet = setManager.get(playerSetName);
				if(playerSet == null) continue;
				if(playerSet.contains(defender)) return true;
			}
			return false;
		};
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	public Set<String> playerSetNames() {return playerSetNames;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (DefenderInPlayerSetPredicateSpec) obj;
		return Objects.equals(this.playerSetNames, that.playerSetNames);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(playerSetNames);
	}
	
	@Override
	public String toString() {
		return "DefenderInPlayerSetPredicateSpec[" +
			"playerSetNames=" + playerSetNames + ']';
	}
	
}
