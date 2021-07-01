package agency.highlysuspect.apathy.rule;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiFunction;

public interface Rule extends BiFunction<MobEntity, ServerPlayerEntity, TriState> {}