package agency.highlysuspect.apathy.rule;

import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public interface Partial extends BiPredicate<Mob, ServerPlayer> {}
