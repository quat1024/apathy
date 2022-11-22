package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.wrapper.Attacker;
import agency.highlysuspect.apathy.hell.wrapper.Defender;

import java.util.function.BiPredicate;

/**
 * I don't know why I called these "partials", I guess not to conflict with the java class "Predicate".
 * It's like a Rule but doesn't ever return TriState.DEFAULT.
 */
public interface Partial extends BiPredicate<Attacker, Defender> {}
