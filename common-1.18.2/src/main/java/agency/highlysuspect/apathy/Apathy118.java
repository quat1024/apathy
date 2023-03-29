package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.CoreOptions;
import agency.highlysuspect.apathy.core.JsonRule;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.core.wrapper.MobExt;
import agency.highlysuspect.apathy.rule.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.rule.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.rule.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.rule.PartialSpecDefenderHasAdvancement;
import agency.highlysuspect.apathy.rule.PartialSpecDefenderInPlayerSet;
import agency.highlysuspect.apathy.rule.PartialSpecDifficultyIs;
import agency.highlysuspect.apathy.rule.PartialSpecLocation;
import agency.highlysuspect.apathy.rule.PartialSpecRevengeTimer;
import agency.highlysuspect.apathy.rule.PartialSpecScore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Apathy118 extends ApathyHell {
	public static Apathy118 instance118;
	
	public MobConfig mobConfig = new MobConfig();
	public BossConfig bossConfig = new BossConfig();
	
	public Apathy118(Path configPath) {
		super(configPath, CoreConv.toLogFacade(LogManager.getLogger(MODID)));
		
		Apathy118.instance118 = this;
	}
	
	@Override
	public boolean loadConfig() {
		boolean ok = super.loadConfig();
		
		MobConfig newMobConfig = mobConfig;
		try {
			newMobConfig = Config.read(new MobConfig(), configPath.resolve("mobs.cfg"));
		} catch (Exception e) {
			log.error("Problem reading mobs.cfg: ", e);
			ok = false;
		} finally {
			mobConfig = newMobConfig;
		}
		
		BossConfig newBossConfig = bossConfig;
		try {
			newBossConfig = Config.read(new BossConfig(), configPath.resolve("boss.cfg"));
		} catch (Exception e) {
			log.error("Problem reading boss.cfg: ", e);
			ok = false;
		} finally {
			bossConfig = newBossConfig;
		}
		
		return ok;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //But it makes more sense that way!
	public boolean allowedToTargetPlayer(Mob attacker, ServerPlayer player) {
		if(attacker.level.isClientSide) throw new IllegalStateException("Do not call on the client, please");
		
		TriState result = mobConfig.rule.apply((Attacker) attacker, (Defender) player);
		if(result != TriState.DEFAULT) return result.get();
		else return mobConfig.fallthrough;
	}
	
	public void noticePlayerAttack(Player player, Entity provoked) {
		Level level = player.level;
		if(level.isClientSide) return;
		
		if(provoked instanceof MobExt ext) {
			//Set the revengetimer on the hit entity
			ext.apathy$provokeNow();
			
			int sameTypeRevengeSpread = generalConfigCooked.get(CoreOptions.General.sameTypeRevengeSpread);
			if(sameTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntitiesOfClass(provoked.getClass(), provoked.getBoundingBox().inflate(sameTypeRevengeSpread))) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
			
			int differentTypeRevengeSpread = generalConfigCooked.get(CoreOptions.General.differentTypeRevengeSpread);
			if(differentTypeRevengeSpread > 0) {
				//kinda grody sorry
				for(Entity nearby : level.getEntities((Entity) null, provoked.getBoundingBox().inflate(differentTypeRevengeSpread), ent -> ent instanceof MobExt)) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
		}
		
		//handle the "peaceful-at-the-start dragon" option
		if(provoked instanceof DragonDuck dragn) dragn.apathy$allowAttackingPlayers();
	}
	
	//Random util crap
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public static <T extends Enum<?>> Set<T> allOf(Class<T> enumClass) {
		Set<T> set = new HashSet<>();
		Collections.addAll(set, enumClass.getEnumConstants());
		return set;
	}
	
	public static Set<Difficulty> allDifficultiesNotPeaceful() {
		Set<Difficulty> wow = allOf(Difficulty.class);
		wow.remove(Difficulty.PEACEFUL);
		return wow;
	}
	
	/// Cross platform stuff
	
	@Override
	public void addRules() {
		super.addRules();
		
		//TODO: add private constructors to all of these LOL, lost time debugging because i used to have "new xxx.serializer()" here
		partialSerializers.register("apathy:attacker_is", PartialSpecAttackerIs.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_is_boss", PartialSpecAttackerIsBoss.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_tagged_with", PartialSpecAttackerTaggedWith.Serializer.INSTANCE);
		partialSerializers.register("apathy:advancements", PartialSpecDefenderHasAdvancement.Serializer.INSTANCE);
		partialSerializers.register("apathy:in_player_set", PartialSpecDefenderInPlayerSet.Serializer.INSTANCE);
		partialSerializers.register("apathy:difficulty_is", PartialSpecDifficultyIs.Serializer.INSTANCE);
		partialSerializers.register("apathy:location", PartialSpecLocation.Serializer.INSTANCE);
		partialSerializers.register("apathy:revenge_timer", PartialSpecRevengeTimer.Serializer.INSTANCE);
		partialSerializers.register("apathy:score", PartialSpecScore.Serializer.INSTANCE);
	}
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
}
