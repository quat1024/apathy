package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.PlayerSetManagerGuts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
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

public abstract class Apathy116 extends ApathyPlusMinecraft {
	public static Apathy116 instance116;
	
	public Apathy116() {
		if(instance116 == null) {
			instance116 = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy 1.16 instantiated twice!");
			log.error("Apathy 1.16 instantiated twice!", e);
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
		//Tricky:
		//in Minecraft 1.16 this method takes a Collection<T>, but in 1.18 it takes a Collection<? extends T>.
		//Even though this call-site textually looks the same, it's not really the same method; it doesn't exist in the hybrid jar.
		return ComponentUtils.formatList(things, toComponent);
	}
	
	@Override
	public PlayerSetManagerGuts getFor(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(
			PlayerSetManager::new,
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
