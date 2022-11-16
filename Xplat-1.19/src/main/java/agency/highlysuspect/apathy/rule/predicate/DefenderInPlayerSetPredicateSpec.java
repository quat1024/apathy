package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.rule.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

public record DefenderInPlayerSetPredicateSpec(Set<String> playerSetNames) implements PartialSpec {
	public static final Codec<DefenderInPlayerSetPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Codec.STRING).fieldOf("player_sets").forGetter(x -> x.playerSetNames)
	).apply(i, DefenderInPlayerSetPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		if(playerSetNames.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			MinecraftServer server = defender.getServer();
			assert server != null;
			
			PlayerSetManager setManager = PlayerSetManager.getFor(server);
			for(String playerSetName : playerSetNames) {
				if(setManager.playerInSet(defender, playerSetName)) return true;
			}
			return false;
		};
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
