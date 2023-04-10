package agency.highlysuspect.apathy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

/**
 * this is just some stuff that i know is different across 1.18 and 1.19
 */
public class Portage {
	public static String stringifyComponent(Component c) {
		return c.getContents();
	}
	
	public static Component literal(String s) {
		return new TextComponent(s);
	}
	
	public static DamageSource comicalAnvilSound(Entity rarrr) {
		return DamageSource.ANVIL;
	}
	
	public static void explodeNoBlockInteraction(Level level, Entity who, double x, double y, double z, float strength) {
		level.explode(who, x, y, z, strength, Explosion.BlockInteraction.NONE);
	}
	
	public static BlockPos blockPosContaining(double x, double y, double z) {
		return new BlockPos(x, y, z);
	}
}
