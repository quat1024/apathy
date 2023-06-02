package agency.highlysuspect.apathy.coreplusminecraft;

import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.core.wrapper.LogFacade;
import agency.highlysuspect.apathy.core.wrapper.VecThree;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;

/**
 * the bulk of the mod is implemented in the "core" module, which is version-independent and can't refer
 * to anything from minecraft, so it contains wrappers for minecraft-specific concepts like "the game difficulty".
 * this file contains conversions to and from those wrapper types, to glue minecraft together with the core
 */
public class MinecraftConv {
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
		switch(diff) {
			case PEACEFUL: return ApathyDifficulty.PEACEFUL;
			case EASY: return ApathyDifficulty.EASY;
			case NORMAL: return ApathyDifficulty.NORMAL;
			case HARD: return ApathyDifficulty.HARD;
			default: throw new IllegalArgumentException(diff.toString());
		}
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
	
	///
	
	public static Mob mob(Attacker attacker) {
		return (Mob) attacker.apathy$underlyingObject();
	}
	
	public static ServerPlayer player(Defender defender) {
		return (ServerPlayer) defender.apathy$getUnderlyingObject();
	}
	
	public static ServerLevel level(Defender defender) {
		return (ServerLevel) player(defender).level;
	}
}
