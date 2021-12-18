package agency.highlysuspect.apathy.rule;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import java.util.function.BiFunction;

public interface Rule extends BiFunction<Mob, ServerPlayer, TriState> {}