package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;

import java.util.Set;

public record DefenderHasAdvancementPredicateSpec(Set<ResourceLocation> advancementIds) implements PredicateSpec {
	public static final Codec<DefenderHasAdvancementPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(ResourceLocation.CODEC).fieldOf("advancements").forGetter(DefenderHasAdvancementPredicateSpec::advancementIds)
	).apply(i, DefenderHasAdvancementPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(advancementIds.isEmpty()) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			MinecraftServer server = defender.server;
			ServerAdvancementManager serverAdvancementManager = server.getAdvancements();
			PlayerAdvancements playerAdvancements = defender.getAdvancements();
			
			for(ResourceLocation advancementId : advancementIds) {
				Advancement adv = serverAdvancementManager.getAdvancement(advancementId);
				if(adv == null) continue;
				if(playerAdvancements.getOrStartProgress(adv).isDone()) return true;
			}
			return false;
		};
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
