package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.CoreMobOptions;
import agency.highlysuspect.apathy.core.JsonRule;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.rule.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.core.rule.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.core.rule.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.core.rule.PartialSpecDifficultyIs;
import agency.highlysuspect.apathy.core.rule.PartialSpecRevengeTimer;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.core.rule.RuleSpecChain;
import agency.highlysuspect.apathy.core.rule.RuleSpecJson;
import agency.highlysuspect.apathy.core.rule.RuleSpecPredicated;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.rule.PartialSpecDefenderHasAdvancement;
import agency.highlysuspect.apathy.rule.PartialSpecDefenderInPlayerSet;
import agency.highlysuspect.apathy.rule.PartialSpecEffect;
import agency.highlysuspect.apathy.rule.PartialSpecLocation;
import agency.highlysuspect.apathy.rule.PartialSpecScore;
import agency.highlysuspect.apathy.rule.PartialSpecScoreboardTeam;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public abstract class Apathy116 extends Apathy {
	public static Apathy116 instance116;
	
	public Apathy116() {
		super(VerConv.toLogFacade(LogManager.getLogger(MODID)));
		
		if(instance116 == null) {
			instance116 = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy 1.16 instantiated twice!");
			log.error("Apathy 1.16 instantiated twice!", e);
			throw e;
		}
	}
	
	@Override
	public Rule bakeMobsConfigRule() {
		Spec<Rule, ?> ruleSpec;
		
		if(mobCfg.get(CoreMobOptions.nuclearOption)) {
			Apathy.instance.log.info("Nuclear option enabled - Ignoring ALL rules in the config file");
			ruleSpec = RuleSpecAlways.ALWAYS_DENY;
		} else {
			ArrayList<Spec<Rule, ?>> ruleSpecList = new ArrayList<>();
			for(String ruleName : mobCfg.get(CoreMobOptions.ruleOrder)) {
				switch(ruleName.trim().toLowerCase(Locale.ROOT)) {
					case "json":
						ruleSpecList.add(new RuleSpecJson());
						break;
					case "difficulty":
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(CoreMobOptions.difficultySetIncluded),
							mobCfg.get(CoreMobOptions.difficultySetExcluded),
							new PartialSpecDifficultyIs(mobCfg.get(CoreMobOptions.difficultySet))
						));
						break;
					case "boss":
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(CoreMobOptions.boss),
							TriState.DEFAULT,
							PartialSpecAttackerIsBoss.INSTANCE
						));
						break;
					case "mobset":
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(CoreMobOptions.mobSetIncluded),
							mobCfg.get(CoreMobOptions.mobSetExcluded),
							new PartialSpecAttackerIs(mobCfg.get(CoreMobOptions.mobSet))
						));
						break;
					case "tagset":
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(CoreMobOptions.tagSetIncluded),
							mobCfg.get(CoreMobOptions.tagSetExcluded),
							new PartialSpecAttackerTaggedWith(mobCfg.get(CoreMobOptions.tagSet))
						));
						break;
					case "playerset":
						mobCfg.get(CoreMobOptions.playerSetName).ifPresent(s ->
							ruleSpecList.add(new RuleSpecPredicated(
								mobCfg.get(CoreMobOptions.playerSetIncluded),
								mobCfg.get(CoreMobOptions.playerSetExcluded),
								new PartialSpecDefenderInPlayerSet(Collections.singleton(s))
							)));
						break;
					case "potionset":
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(VerMobOptions.mobEffectSetIncluded),
							mobCfg.get(VerMobOptions.mobEffectSetExcluded),
							new PartialSpecEffect(mobCfg.get(VerMobOptions.mobEffectSet), mobCfg.get(VerMobOptions.mobEffectWho))
						));
						break;
					case "revenge":
						ruleSpecList.add(new RuleSpecPredicated(
							TriState.TRUE, TriState.DEFAULT,
							new PartialSpecRevengeTimer(mobCfg.get(CoreMobOptions.revengeTimer))
						));
						break;
					default:
						Apathy.instance.log.warn("Unknown rule " + ruleName + " listed in the ruleOrder config option.");
				}
			}
			
			ruleSpec = new RuleSpecChain(ruleSpecList);
		}
		
		if(generalCfg.get(CoreGenOptions.debugBuiltinRule)) JsonRule.dump(ruleSpec, "builtin-rule");
		if(generalCfg.get(CoreGenOptions.runRuleOptimizer)) {
			ruleSpec = ruleSpec.optimize();
			if(generalCfg.get(CoreGenOptions.debugBuiltinRule)) JsonRule.dump(ruleSpec, "builtin-rule-opt");
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
		
		if(provoked instanceof Attacker) {
			Attacker ext = (Attacker) provoked;
			long now = ext.apathy$now();
			
			//revenge timer on the hit entity:
			ext.apathy$setProvocationTime(now);
			
			//revenge timer with same-type spreading:
			int sameTypeRevengeSpread = generalCfg.get(CoreGenOptions.sameTypeRevengeSpread);
			if(sameTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntitiesOfClass(provoked.getClass(), provoked.getBoundingBox().inflate(sameTypeRevengeSpread))) {
					if(nearby instanceof Attacker) ((Attacker) nearby).apathy$setProvocationTime(now);
				}
			}
			
			//revenge timer with different-type spreading: (or really "regardless-of-type spreading" i guess)
			int differentTypeRevengeSpread = generalCfg.get(CoreGenOptions.differentTypeRevengeSpread);
			if(differentTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntities((Entity) null, provoked.getBoundingBox().inflate(differentTypeRevengeSpread), ent -> ent instanceof Attacker)) {
					if(nearby instanceof Attacker) ((Attacker) nearby).apathy$setProvocationTime(now);
				}
			}
		}
		
		//handle the "peaceful-at-the-start dragon" option
		if(provoked instanceof DragonDuck) ((DragonDuck) provoked).apathy$allowAttackingPlayers();
	}
	
	@Override
	public void addRules() {
		super.addRules();
		
		partialSerializers.register("advancements", PartialSpecDefenderHasAdvancement.Serializer.INSTANCE);
		partialSerializers.register("effect", PartialSpecEffect.Serializer.INSTANCE);
		partialSerializers.register("in_player_set", PartialSpecDefenderInPlayerSet.Serializer.INSTANCE);
		partialSerializers.register("location", PartialSpecLocation.Serializer.INSTANCE);
		partialSerializers.register("score", PartialSpecScore.Serializer.INSTANCE);
		partialSerializers.register("team", PartialSpecScoreboardTeam.Serializer.INSTANCE);
	}
	
	@Override
	public void addMobConfig(ConfigSchema schema) {
		super.addMobConfig(schema);
		VerMobOptions.visit(schema);
	}
	
	@Override
	public @Nullable AttackerType parseAttackerType(String s) {
		s = s.trim();
		
		if(s.isEmpty()) return null; //can sometimes happen due to shitty parsing code in my config library
		
		ResourceLocation rl = ResourceLocation.tryParse(s);
		if(rl == null) {
			log.error("Can't parse '{}' as a resourcelocation", s);
			return null;
		}
		
		EntityType<?> type = Registry.ENTITY_TYPE.get(rl); //defaultedregistry, defaults to pig instead of null
		return (AttackerType) type; //duck interface
	}
	
	public @Nullable AttackerTag parseAttackerTag(String s) {
		s = s.trim();
		
		if(s.isEmpty()) return null; //can sometimes happen due to shitty parsing code in my config library
		
		ResourceLocation rl = ResourceLocation.tryParse(s);
		if(rl == null) {
			log.error("Can't parse '{}' as a resourcelocation", s);
			return null;
		}
		
		Tag.Named<EntityType<?>> tag = constructTagUsingWeirdAncientMethods(rl);
		if(tag == null) {
			log.error("Couldn't construct tag with id '{}'", s);
			return null;
		}
		
		return new TagWrapper(tag);
	}
	
	//in lieu of a mixin this time (??just because??)
	public static class TagWrapper implements AttackerTag {
		public TagWrapper(Tag.Named<EntityType<?>> tag) {
			this.tag = tag;
		}
		
		private final Tag.Named<EntityType<?>> tag;
		
		@Override
		public Object apathy$underlyingObject() {
			return tag;
		}
		
		@Override
		public boolean apathy$is(Attacker attacker) {
			return tag.contains(((Entity) attacker.apathy$underlyingObject()).getType());
		}
		
		@Override
		public String apathy$id() {
			return tag.getName().toString();
		}
	}
	
	public abstract @Nullable Tag.Named<EntityType<?>> constructTagUsingWeirdAncientMethods(ResourceLocation rl);
}
