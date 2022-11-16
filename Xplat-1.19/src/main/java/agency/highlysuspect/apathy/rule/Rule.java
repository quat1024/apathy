package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.hell.TriState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.function.BiFunction;

/**
 * Given an attacker and a defender, returns:
 * - TriState.TRUE when the attacker is allowed to attack
 * - TriState.FALSE when the attacker is *not* allowed to attack
 * - TriState.DEFAULT if the rule doesn't apply and the next rule should be checked
 */
public interface Rule extends BiFunction<Mob, ServerPlayer, TriState> {}