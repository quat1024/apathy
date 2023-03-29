package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.CoreMobOptions;
import agency.highlysuspect.apathy.core.JsonRule;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public abstract class Apathy118 extends Apathy {
	public static Apathy118 instance118;
	
	public Apathy118(Path configPath) {
		super(configPath, CoreConv.toLogFacade(LogManager.getLogger(MODID)));
		
		if(instance118 == null) {
			instance118 = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy 1.18 instantiated twice!");
			log.error("Apathy 1.18 instantiated twice!", e);
			throw e;
		}
	}
	
	@Override
	public Rule bakeRule() {
		RuleSpec<?> ruleSpec;
		
		if(mobCfg.get(CoreMobOptions.nuclearOption)) {
			Apathy.instance.log.info("Nuclear option enabled - Ignoring ALL rules in the config file");
			ruleSpec = RuleSpecAlways.ALWAYS_DENY;
		} else {
			ArrayList<RuleSpec<?>> ruleSpecList = new ArrayList<>();
			for(String ruleName : mobCfg.get(CoreMobOptions.ruleOrder)) {
				switch(ruleName.trim().toLowerCase(Locale.ROOT)) {
					case "json"       ->ruleSpecList.add(new RuleSpecJson());
					case "difficulty" -> ruleSpecList.add(new RuleSpecPredicated(
						mobCfg.get(CoreMobOptions.difficultySetIncluded),
						mobCfg.get(CoreMobOptions.difficultySetExcluded),
						new PartialSpecDifficultyIs(mobCfg.get(CoreMobOptions.difficultySet))
					));
					case "boss"       -> ruleSpecList.add(new RuleSpecPredicated(
						mobCfg.get(CoreMobOptions.boss),
						TriState.DEFAULT,
						new PartialSpecAttackerIsBoss()
					));
					case "mobset"     -> ruleSpecList.add(new RuleSpecPredicated(
						mobCfg.get(CoreMobOptions.mobSetIncluded),
						mobCfg.get(CoreMobOptions.mobSetExcluded),
						new PartialSpecAttackerIs(mobCfg.get(VerMobOptions.mobSet))
					));
					case "tagset"     -> ruleSpecList.add(new RuleSpecPredicated(
						mobCfg.get(CoreMobOptions.tagSetIncluded),
						mobCfg.get(CoreMobOptions.tagSetExcluded),
						new PartialSpecAttackerTaggedWith(mobCfg.get(VerMobOptions.tagSet))
					));
					case "playerset"  -> mobCfg.get(CoreMobOptions.playerSetName).ifPresent(s ->
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(CoreMobOptions.playerSetIncluded),
							mobCfg.get(CoreMobOptions.playerSetExcluded),
							new PartialSpecDefenderInPlayerSet(Collections.singleton(s))
						)));
					case "revenge"    -> ruleSpecList.add(RuleSpecPredicated.allowIf(
						new PartialSpecRevengeTimer(mobCfg.get(CoreMobOptions.revengeTimer))
					));
					default -> Apathy.instance.log.warn("Unknown rule " + ruleName + " listed in the ruleOrder config option.");
				}
			}
			
			ruleSpec = new RuleSpecChain(ruleSpecList);
		}
		
		if(generalCfg.get(CoreGenOptions.debugBuiltinRule)) JsonRule.dump(ruleSpec, configPath, "builtin-rule");
		if(generalCfg.get(CoreGenOptions.runRuleOptimizer)) {
			ruleSpec = ruleSpec.optimize();
			if(generalCfg.get(CoreGenOptions.debugBuiltinRule)) JsonRule.dump(ruleSpec, configPath, "builtin-rule-opt");
		}
		
		return ruleSpec.build();
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
			
			int sameTypeRevengeSpread = generalCfg.get(CoreGenOptions.sameTypeRevengeSpread);
			if(sameTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntitiesOfClass(provoked.getClass(), provoked.getBoundingBox().inflate(sameTypeRevengeSpread))) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
			
			int differentTypeRevengeSpread = generalCfg.get(CoreGenOptions.differentTypeRevengeSpread);
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
		VerMobOptions.visit(schema);
	}
}
