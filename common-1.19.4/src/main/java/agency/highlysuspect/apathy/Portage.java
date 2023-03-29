package agency.highlysuspect.apathy;

import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

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
	
	public static <T> DataResult<T> dataResultError(String error) {
		return DataResult.error(() -> error);
	}
}
