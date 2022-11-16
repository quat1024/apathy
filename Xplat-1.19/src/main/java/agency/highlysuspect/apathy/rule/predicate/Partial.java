package agency.highlysuspect.apathy.rule.predicate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.function.BiPredicate;

/**
 * I don't know why I called these "partials", I guess not to conflict with the java class "Predicate".
 * It's like a Rule but doesn't ever return TriState.DEFAULT.
 */
public interface Partial extends BiPredicate<Mob, ServerPlayer> {}
