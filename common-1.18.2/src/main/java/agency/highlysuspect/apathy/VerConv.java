package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.wrapper.LogFacade;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.core.wrapper.MobExt;
import agency.highlysuspect.apathy.core.wrapper.VecThree;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;

/**
 * the bulk of the mod is implemented in the "core" module, which is version-independent and can't refer
 * to anything from minecraft, so it contains wrappers for minecraft-specific concepts like "the game difficulty".
 * this file contains conversions to and from those wrapper types, to glue minecraft together with the core
 */
public class VerConv {
	public static LogFacade toLogFacade(Logger logger) {
		return new LogFacade() {
			@Override
			public void info(String message, Object... args) {
				logger.info(message, args);
			}
			
			@Override
			public void warn(String message, Object... args) {
				logger.warn(message, args);
			}
			
			@Override
			public void error(String message, Object... args) {
				logger.error(message, args);
			}
		};
	}
	
	public static ApathyDifficulty toApathyDifficulty(Difficulty diff) {
		return switch(diff) {
			case PEACEFUL -> ApathyDifficulty.PEACEFUL;
			case EASY -> ApathyDifficulty.EASY;
			case NORMAL -> ApathyDifficulty.NORMAL;
			case HARD -> ApathyDifficulty.HARD;
		};
	}
	
	public static Difficulty fromApathyDifficulty(ApathyDifficulty diff) {
		return switch(diff) {
			case PEACEFUL -> Difficulty.PEACEFUL;
			case EASY -> Difficulty.EASY;
			case NORMAL -> Difficulty.NORMAL;
			case HARD -> Difficulty.HARD;
		};
	}
	
	public static VecThree toVecThree(Vec3 vec3) {
		return new VecThree() {
			@Override
			public double x() {
				return vec3.x;
			}
			
			@Override
			public double y() {
				return vec3.y;
			}
			
			@Override
			public double z() {
				return vec3.z;
			}
		};
	}
	
	public static Vec3 fromVecThree(VecThree three) {
		return new Vec3(three.x(), three.y(), three.z());
	}
	
	public static Mob mob(Attacker attacker) {
		return (Mob) attacker.apathy$getMob();
	}
	
	public static MobExt mobExt(Attacker attacker) {
		return (MobExt) mob(attacker);
	}
	
	public static EntityType<?> type(Attacker attacker) {
		return (EntityType<?>) attacker.apathy$getEntityType();
	}
	
	public static ServerPlayer player(Defender defender) {
		return (ServerPlayer) defender.apathy$getServerPlayer();
	}
	
	public static ServerLevel level(Attacker attacker) {
		return (ServerLevel) ((Entity) attacker.apathy$getMob()).level;
	}
	
	public static ServerLevel level(Defender defender) {
		return (ServerLevel) player(defender).level;
	}
}
