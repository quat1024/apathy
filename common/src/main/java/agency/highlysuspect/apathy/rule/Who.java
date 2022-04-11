package agency.highlysuspect.apathy.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
	
	public static final Codec<Who> CODEC = Codec.STRING.comapFlatMap(s -> switch(s) {
		case "attacker" -> DataResult.success(ATTACKER);
		case "defender" -> DataResult.success(DEFENDER);
		default -> DataResult.error("unknown who " + s);
	}, w -> w.name().toLowerCase(Locale.ROOT));
}
