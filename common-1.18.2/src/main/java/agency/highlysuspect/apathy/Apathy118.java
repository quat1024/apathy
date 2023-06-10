package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.PlayerSetManagerGuts;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public abstract class Apathy118 extends ApathyPlusMinecraft {
	public static Apathy118 instance118;
	
	public Apathy118() {
		if(instance118 == null) {
			instance118 = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy 1.18 instantiated twice!");
			log.error("Apathy 1.18 instantiated twice!", e);
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
		return new TextComponent(lit);
	}
	
	@Override
	public String stringifyComponent(Component comp) {
		return comp.getContents();
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
	public void sendSuccess(CommandContext<CommandSourceStack> cmd, Component message, boolean impersonal) {
		cmd.getSource().sendSuccess(message, impersonal);
	}
	
	@Override
	public ServerLevel serverLevel(Entity ent) {
		return (ServerLevel) ent.level;
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
	
	public @Nullable AttackerTag parseAttackerTag(String s) {
		s = s.trim();
		if(s.startsWith("#")) s = s.substring(1); //vanilla syntax for "tag-as-opposed-to-resourcelocation" that i don't care about rn
		
		if(s.isEmpty()) return null; //can sometimes happen due to shitty parsing code in my config library
		
		ResourceLocation rl = ResourceLocation.tryParse(s);
		if(rl == null) {
			log.error("Can't parse '{}' as a resourcelocation", s);
			return null;
		}
		
		return (AttackerTag) (Object) TagKey.create(Registry.ENTITY_TYPE_REGISTRY, rl);
	}
}
