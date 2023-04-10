package agency.highlysuspect.apathy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * this is just some stuff that i know is different across 1.18 and 1.19
 */
public class Portage {
	public static String stringifyComponent(Component c) {
		return c.getString();
	}
	
	public static Component literal(String s) {
		return Component.literal(s);
	}
	
	public static DamageSource comicalAnvilSound(Entity rarrr) {
		return rarrr.damageSources().anvil(rarrr);
	}
	
	public static void explodeNoBlockInteraction(Level level, Entity who, double x, double y, double z, float strength) {
		level.explode(who, x, y, z, strength, Level.ExplosionInteraction.NONE);
	}
	
	public static BlockPos blockPosContaining(double x, double y, double z) {
		return BlockPos.containing(x, y, z);
	}
}
