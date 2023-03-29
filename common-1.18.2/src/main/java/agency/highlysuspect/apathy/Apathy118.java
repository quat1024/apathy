package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.CoreOptions;
import agency.highlysuspect.apathy.core.JsonRule;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.newconfig.ConfigSchema;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.RuleSpec;
import agency.highlysuspect.apathy.core.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.core.rule.RuleSpecChain;
import agency.highlysuspect.apathy.core.rule.RuleSpecJson;
import agency.highlysuspect.apathy.core.rule.RuleSpecPredicated;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class Apathy118 extends ApathyHell {
	public static Apathy118 instance118;
	
	public Apathy118(Path configPath) {
		super(configPath, CoreConv.toLogFacade(LogManager.getLogger(MODID)));
		
		Apathy118.instance118 = this;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //But it makes more sense that way!
	public boolean allowedToTargetPlayer(Mob attacker, ServerPlayer player) {
		return allowedToTargetPlayer((Attacker) attacker, (Defender) player);
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
	
	@Override
	public Rule bakeRule() {
		RuleSpec<?> ruleSpec;
		
		if(mobsConfigCooked.get(CoreOptions.Mobs.nuclearOption)) {
			ApathyHell.instance.log.info("Nuclear option enabled - Ignoring ALL rules in the config file");
			ruleSpec = RuleSpecAlways.ALWAYS_DENY;
		} else {
			ArrayList<RuleSpec<?>> ruleSpecList = new ArrayList<>();
			for(String ruleName : mobsConfigCooked.get(CoreOptions.Mobs.ruleOrder)) {
				switch(ruleName.trim().toLowerCase(Locale.ROOT)) {
					case "json"       ->ruleSpecList.add(new RuleSpecJson());
					case "difficulty" -> ruleSpecList.add(new RuleSpecPredicated(
						mobsConfigCooked.get(CoreOptions.Mobs.difficultySetIncluded),
						mobsConfigCooked.get(CoreOptions.Mobs.difficultySetExcluded),
						new PartialSpecDifficultyIs(mobsConfigCooked.get(CoreOptions.Mobs.difficultySet))
					));
					case "boss"       -> ruleSpecList.add(new RuleSpecPredicated(
						mobsConfigCooked.get(CoreOptions.Mobs.boss),
						TriState.DEFAULT,
						new PartialSpecAttackerIsBoss()
					));
					case "mobset"     -> ruleSpecList.add(new RuleSpecPredicated(
						mobsConfigCooked.get(CoreOptions.Mobs.mobSetIncluded),
						mobsConfigCooked.get(CoreOptions.Mobs.mobSetExcluded),
						new PartialSpecAttackerIs(mobsConfigCooked.get(PlatformOptions.Mobs.mobSet))
					));
					case "tagset"     -> ruleSpecList.add(new RuleSpecPredicated(
						mobsConfigCooked.get(CoreOptions.Mobs.tagSetIncluded),
						mobsConfigCooked.get(CoreOptions.Mobs.tagSetExcluded),
						new PartialSpecAttackerTaggedWith(mobsConfigCooked.get(PlatformOptions.Mobs.tagSet))
					));
					case "playerset"  -> mobsConfigCooked.get(CoreOptions.Mobs.playerSetName).ifPresent(s ->
						ruleSpecList.add(new RuleSpecPredicated(
							mobsConfigCooked.get(CoreOptions.Mobs.playerSetIncluded),
							mobsConfigCooked.get(CoreOptions.Mobs.playerSetExcluded),
							new PartialSpecDefenderInPlayerSet(Collections.singleton(s))
					)));
					case "revenge"    -> ruleSpecList.add(RuleSpecPredicated.allowIf(
						new PartialSpecRevengeTimer(mobsConfigCooked.get(CoreOptions.Mobs.revengeTimer))
					));
					default -> ApathyHell.instance.log.warn("Unknown rule " + ruleName + " listed in the ruleOrder config option.");
				}
			}
			
			ruleSpec = new RuleSpecChain(ruleSpecList);
		}
		
		if(generalConfigCooked.get(CoreOptions.General.debugBuiltinRule)) JsonRule.dump(ruleSpec, configPath, "builtin-rule");
		if(generalConfigCooked.get(CoreOptions.General.runRuleOptimizer)) {
			ruleSpec = ruleSpec.optimize();
			if(generalConfigCooked.get(CoreOptions.General.debugBuiltinRule)) JsonRule.dump(ruleSpec, configPath, "builtin-rule-opt");
		}
		
		return ruleSpec.build();
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
	
	@Override
	public void addMobConfig(ConfigSchema schema) {
		super.addMobConfig(schema);
		PlatformOptions.Mobs.visit(schema);
	}
	
	@Deprecated(forRemoval = true)
	public static <T extends Enum<?>> Set<T> allOf(Class<T> enumClass) {
		Set<T> set = new HashSet<>();
		Collections.addAll(set, enumClass.getEnumConstants());
		return set;
	}
	
	@Deprecated(forRemoval = true)
	public static Set<Difficulty> allDifficultiesNotPeaceful() {
		Set<Difficulty> wow = allOf(Difficulty.class);
		wow.remove(Difficulty.PEACEFUL);
		return wow;
	}
}
