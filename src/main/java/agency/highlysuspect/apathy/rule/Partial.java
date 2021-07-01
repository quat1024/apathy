package agency.highlysuspect.apathy.rule;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiPredicate;

public interface Partial extends BiPredicate<MobEntity, ServerPlayerEntity> {}
