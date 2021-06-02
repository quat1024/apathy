package agency.highlysuspect.apathy.clojure;

import agency.highlysuspect.apathy.rule.Rule;

public interface ClojureProxy extends Rule {
	ClojureProxy NO_CLOJURE = (attacker, player) -> {
		throw new IllegalStateException("calling clojure proxy but clojure is disabled?");
	};
}
