package agency.highlysuspect.apathy.rule;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.function.BiPredicate;

public interface Partial extends BiPredicate<Mob, ServerPlayer> {}
