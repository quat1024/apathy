package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.playerset.PlayerSet;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

public class DefenderInPlayerSetPredicateSpec implements PredicateSpec {
	public DefenderInPlayerSetPredicateSpec(Set<String> playerSetNames) {
		this.playerSetNames = playerSetNames;
	}
	
	private final Set<String> playerSetNames;
	
	public static final Codec<DefenderInPlayerSetPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Codec.STRING).fieldOf("player_sets").forGetter(x -> x.playerSetNames)
	).apply(i, DefenderInPlayerSetPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(playerSetNames.isEmpty()) return ALWAYS_FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return PredicateSpec.sizeSpecializeNotEmpty(playerSetNames,
			playerSetName -> (attacker, defender) -> {
				MinecraftServer server = defender.getServer();
				assert server != null; //it's a ServerPlayerEntity
				
				PlayerSetManager setManager = PlayerSetManager.getFor(server);
				PlayerSet set = setManager.get(playerSetName);
				if(set == null) return false;
				else return set.contains(defender);
			},
			set -> (attacker, defender) -> {
				MinecraftServer server = defender.getServer();
				assert server != null;
				
				PlayerSetManager setManager = PlayerSetManager.getFor(server);
				for(String playerSetName : set) {
					PlayerSet playerSet = setManager.get(playerSetName);
					if(playerSet == null) continue;
					if(playerSet.contains(defender)) return true;
				}
				return false;
			}
		);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
