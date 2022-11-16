package agency.highlysuspect.apathy.rule;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Locale;

public enum Who {
	ATTACKER,
	DEFENDER;
	
	public Entity choose(Mob attacker, ServerPlayer defender) {
		return switch(this) {
			case ATTACKER -> attacker;
			case DEFENDER -> defender;
		};
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static Who fromString(String name) {
		return switch(name) {
			case "attacker" -> ATTACKER;
			case "defender" -> DEFENDER;
			default -> throw new IllegalArgumentException("expected 'attacker' or 'defender'");
		};
	}
	
	@Deprecated(forRemoval = true)
	public static final Codec<Who> CODEC = CodecUtil.enumCodec("Who", Who.class);
}
