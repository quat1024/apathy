package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.MinecraftConv;
import agency.highlysuspect.apathy.coreplusminecraft.PlayerSetManagerGuts;
import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Apathy119 extends ApathyPlusMinecraft {
	public static Apathy119 instance119;
	
	public Apathy119() {
		if(instance119 == null) {
			instance119 = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy 1.19 instantiated twice!");
			log.error("Apathy 1.19 instantiated twice!", e);
			throw e;
		}
	}
	
	@Override
	public Registry<MobEffect> mobEffectRegistry() {
		return Registry.MOB_EFFECT;
	}
	
	@Override
	public Registry<EntityType<?>> entityTypeRegistry() {
		return Registry.ENTITY_TYPE;
	}
	
	@Override
	public BlockPos blockPosContaining(double x, double y, double z) {
		return new BlockPos(x, y, z);
	}
	
	@Override
	public Component literal(String lit) {
		return Component.literal(lit);
	}
	
	@Override
	public String stringifyComponent(Component comp) {
		return comp.getString();
	}
	
	@Override
	public <T> Component formatList(Collection<T> things, Function<T, Component> toComponent) {
		return ComponentUtils.formatList(things, toComponent);
	}
	
	@Override
	public PlayerSetManagerGuts getFor(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(
			PlayerSetManager::new, //Nbt constructor
			PlayerSetManager::new, //Default constructor
			"apathy-player-sets"
		).guts;
	}
	
	@Override
	public DamageSource comicalAnvilSound(Entity rarrr) {
		return DamageSource.ANVIL;
	}
	
	@Override
	public void explodeNoBlockInteraction(Level level, Entity who, double x, double y, double z, float strength) {
		level.explode(who, x, y, z, strength, Explosion.BlockInteraction.NONE);
	}
	
	@Override
	public void sendSuccess(CommandContext<CommandSourceStack> cmd, Supplier<Component> message, boolean impersonal) {
		cmd.getSource().sendSuccess(message.get(), impersonal);
	}
	
	@Override
	public ServerLevel serverLevel(Entity ent) {
		return (ServerLevel) ent.level;
	}

	@Override
	public ResourceLocation resource(String str) {
		return new ResourceLocation(str);
	}

	@Override
	public ResourceKey<MobEffect> invisibilityResourceKey() {
		return Registry.MOB_EFFECT.getResourceKey(MobEffects.INVISIBILITY).orElseThrow(RuntimeException::new);
	}

	@Override
	public boolean hasEffect(LivingEntity entity, ResourceKey<MobEffect> effect) {
		return entity.hasEffect(Registry.MOB_EFFECT.getOrThrow(effect));
	}

	@Override
	public int getPlayerScore(Scoreboard scoreboard, Entity entity, Objective objective) {
		return scoreboard.hasPlayerScore(entity.getScoreboardName(), objective) ? scoreboard.getOrCreatePlayerScore(entity.getScoreboardName(), objective).getScore() : 0;
	}

	@Override
	public boolean isLocationPredicateAny(LocationPredicate predicate) {
		return predicate == LocationPredicate.ANY;
	}

	@Override
	public boolean doesAdvancementExist(ServerAdvancementManager manager, ResourceLocation id) {
		return manager.getAdvancement(id) != null;
	}

	@Override
	public boolean isAdvancementDone(PlayerAdvancements playerAdvancements, ServerAdvancementManager manager, ResourceLocation id) {
		return playerAdvancements.getOrStartProgress(manager.getAdvancement(id)).isDone();
	}

	@Override
	public JsonElement serializeLocationPredicate(LocationPredicate predicate) {
		return predicate.serializeToJson();
	}

	@Override
	public LocationPredicate deserializeLocationPredicate(JsonElement json) {
		return LocationPredicate.fromJson(json);
	}

	public void noticePlayerAttack(Player player, Entity provoked) {
		Level level = player.level;
		if(level.isClientSide) return;
		
		if(provoked instanceof Attacker ext) {
			long now = ext.apathy$now();
			
			//revenge timer on the hit entity:
			ext.apathy$setProvocationTime(now);
			
			//revenge timer with same-type spreading:
			int sameTypeRevengeSpread = generalCfg.get(CoreGenOptions.sameTypeRevengeSpread);
			if(sameTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntitiesOfClass(provoked.getClass(), provoked.getBoundingBox().inflate(sameTypeRevengeSpread))) {
					if(nearby instanceof Attacker extt) extt.apathy$setProvocationTime(now);
				}
			}
			
			//revenge timer with different-type spreading: (or really "regardless-of-type spreading" i guess)
			int differentTypeRevengeSpread = generalCfg.get(CoreGenOptions.differentTypeRevengeSpread);
			if(differentTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntities((Entity) null, provoked.getBoundingBox().inflate(differentTypeRevengeSpread), ent -> ent instanceof Attacker)) {
					if(nearby instanceof Attacker extt) extt.apathy$setProvocationTime(now);
				}
			}
		}
		
		//handle the "peaceful-at-the-start dragon" option
		if(provoked instanceof DragonDuck dragn) dragn.apathy$allowAttackingPlayers();
	}
	
	public void filterMobEffectUtilCall(ServerLevel level, @Nullable Entity provoker, List<ServerPlayer> original) {
		if(provoker instanceof Warden warden) {
			if(!bossCfg.get(VerBossOptions.wardenDarknessDifficuties).contains(MinecraftConv.toApathyDifficulty(level.getDifficulty()))) {
				original.clear();
				return;
			}
			
			if(bossCfg.get(VerBossOptions.wardenDarknessOnlyToPlayersItCanTarget)) {
				original.removeIf(player -> !allowedToTargetPlayer(warden, player));
			}
		}
	}
	
	@Override
	public void addBossConfig(ConfigSchema schema) {
		super.addBossConfig(schema);
		VerBossOptions.visit(schema);
	}
	
	public @Nullable AttackerTag parseAttackerTag(String s) {
		s = s.trim();
		if(s.startsWith("#")) s = s.substring(1); //vanilla syntax for "tag-as-opposed-to-resourcelocation" that i don't care about rn
		
		if(s.isEmpty()) return null; //can sometimes happen due to shitty parsing code in my config library
		
		ResourceLocation rl = ResourceLocation.tryParse(s);
		if(rl == null) {
			log.error("Can't parse '{}' as a resourcelocation", s);
			return null;
		}
		
		return (AttackerTag) (Object) TagKey.create(Registry.ENTITY_TYPE.key(), rl);
	}
}
