package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.TriState;

import java.util.function.BiFunction;

/**
 * Given an attacker and a defender, returns:
 * - TriState.TRUE when the attacker is allowed to attack
 * - TriState.FALSE when the attacker is *not* allowed to attack
 * - TriState.DEFAULT if the rule doesn't apply and the next rule should be checked
 */
public interface Rule extends BiFunction<Attacker, Defender, TriState> {}