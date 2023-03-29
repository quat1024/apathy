package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.Defender;

import java.util.function.BiFunction;

/**
 * Given an attacker and a defender, returns:
 * - TriState.TRUE when the attacker is allowed to attack
 * - TriState.FALSE when the attacker is *not* allowed to attack
 * - TriState.DEFAULT if the rule doesn't apply and the next rule should be checked
 */
public interface Rule extends BiFunction<Attacker, Defender, TriState> {}