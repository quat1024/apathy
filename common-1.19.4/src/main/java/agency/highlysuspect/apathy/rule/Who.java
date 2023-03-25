package agency.highlysuspect.apathy.rule;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public enum Who {
	ATTACKER,
	DEFENDER;
	
	public Entity choose(Mob attacker, ServerPlayer defender) {
		return switch(this) {
			case ATTACKER -> attacker;
			case DEFENDER -> defender;
		};
	}
	
	public static final Codec<Who> CODEC = CodecUtil.enumCodec("Who", Who.class);
}
