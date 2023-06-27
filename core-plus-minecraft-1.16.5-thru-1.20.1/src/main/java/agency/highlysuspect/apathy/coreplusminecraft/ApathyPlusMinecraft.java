package agency.highlysuspect.apathy.coreplusminecraft;

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
import agency.highlysuspect.apathy.core.rule.PartialSpecSpawnType;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.core.rule.RuleSpecChain;
import agency.highlysuspect.apathy.core.rule.RuleSpecJson;
import agency.highlysuspect.apathy.core.rule.RuleSpecPredicated;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.coreplusminecraft.rule.PartialSpecDefenderHasAdvancement;
import agency.highlysuspect.apathy.coreplusminecraft.rule.PartialSpecDefenderInPlayerSet;
import agency.highlysuspect.apathy.coreplusminecraft.rule.PartialSpecEffect;
import agency.highlysuspect.apathy.coreplusminecraft.rule.PartialSpecLocation;
import agency.highlysuspect.apathy.coreplusminecraft.rule.PartialSpecScore;
import agency.highlysuspect.apathy.coreplusminecraft.rule.PartialSpecScoreboardTeam;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ApathyPlusMinecraft extends Apathy {
	public static ApathyPlusMinecraft instanceMinecraft;
	
	public ApathyPlusMinecraft() {
		super(MinecraftConv.toLogFacade(LogManager.getLogger(MODID)));
		
		if(instanceMinecraft == null) {
			instanceMinecraft = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy instanceMinecraft instantiated twice!");
			log.error("Apathy instanceMinecraft instantiated twice!", e);
			throw e;
		}
	}
	
	public abstract Registry<MobEffect> mobEffectRegistry();
	public abstract Registry<EntityType<?>> entityTypeRegistry();
	public abstract BlockPos blockPosContaining(double x, double y, double z);
	public abstract Component literal(String lit);
	public abstract String stringifyComponent(Component comp);
	public abstract <T> Component formatList(Collection<T> things, Function<T, Component> toComponent);
	public abstract PlayerSetManagerGuts getFor(MinecraftServer server);
	public PlayerSetManagerGuts getFor(CommandContext<CommandSourceStack> ctx) {
		return getFor(ctx.getSource().getServer());
	}
	public abstract DamageSource comicalAnvilSound(Entity rarrr);
	public abstract void explodeNoBlockInteraction(Level level, Entity who, double x, double y, double z, float strength);
	//1.20
	public abstract void sendSuccess(CommandContext<CommandSourceStack> cmd, Supplier<Component> messageMaker, boolean impersonal);
	public abstract ServerLevel serverLevel(Entity ent);
	
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
							mobCfg.get(MinecraftMobOptions.mobEffectSetIncluded),
							mobCfg.get(MinecraftMobOptions.mobEffectSetExcluded),
							new PartialSpecEffect(mobCfg.get(MinecraftMobOptions.mobEffectSet), mobCfg.get(MinecraftMobOptions.mobEffectWho))
						));
						break;
					case "spawntype":
						ruleSpecList.add(new RuleSpecPredicated(
							mobCfg.get(CoreMobOptions.spawnTypeIncluded),
							mobCfg.get(CoreMobOptions.spawnTypeExcluded),
							new PartialSpecSpawnType(mobCfg.get(CoreMobOptions.spawnTypeSet))
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
		MinecraftMobOptions.visit(schema);
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
		
		EntityType<?> type = entityTypeRegistry().get(rl); //defaultedregistry, defaults to pig instead of null
		return (AttackerType) type; //duck interface
	}
}
