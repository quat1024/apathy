package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.Defender;

import java.util.function.BiPredicate;

/**
 * I don't know why I called these "partials". I guess because they don't form a complete rule by themself.
 * These are predicates, they return "true" or "false", how that maps onto "allow"/"deny"/"pass" is up to the user.
 */
public interface Partial extends BiPredicate<Attacker, Defender> {}
