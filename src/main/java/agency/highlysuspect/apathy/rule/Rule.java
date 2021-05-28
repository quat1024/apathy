package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Init;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public interface Rule extends BiFunction<MobEntity, ServerPlayerEntity, TriState> {
	//Please use these rules whenever you know the result is constant
	//The combinators use these to optimize stuff.
	Rule ALWAYS_ALLOW = (attacker, defender) -> TriState.TRUE;
	Rule ALWAYS_DENY = (attacker, defender) -> TriState.FALSE;
	Rule ALWAYS_PASS = (attacker, defender) -> TriState.DEFAULT;
	
	static Rule alwaysRule(TriState which) {
		switch(which) {
			case FALSE: return ALWAYS_DENY;
			case DEFAULT: return ALWAYS_PASS;
			case TRUE: return ALWAYS_ALLOW;
			default: throw new IllegalStateException(which.name());
		}
	}
	
	// Combinators
	
	default Rule andThen(Rule next) {
		if(this == ALWAYS_ALLOW) return this;
		else if(this == ALWAYS_DENY) return this;
		else if(this == ALWAYS_PASS) return next;
		else if(next == ALWAYS_PASS) return this;
		else return (attacker, defender) -> {
			TriState me = this.apply(attacker, defender);
			if(me != TriState.DEFAULT) return me;
			else return next.apply(attacker, defender);
		};
	}
	
	static Rule chain(Collection<Rule> rules) {
		return chain(rules.toArray(new Rule[0]));
	}
	
	static Rule chain(Rule... rules) {
		//Try to remove as many rules as possible from the chain
		if(rules.length == 0) return ALWAYS_PASS;
		else if(rules.length == 1) return rules[0];
		else if(rules[0] == ALWAYS_ALLOW) return ALWAYS_ALLOW;
		else if(rules[0] == ALWAYS_DENY) return ALWAYS_DENY;
		
		List<Rule> filteredRules = new ArrayList<>();
		for(Rule rule : rules) {
			if(rule == ALWAYS_PASS) continue;
			filteredRules.add(rule);
			if(rule == ALWAYS_ALLOW) break;
			if(rule == ALWAYS_DENY) break;
		}
		
		if(filteredRules.size() == 0) return ALWAYS_PASS;
		else if(filteredRules.size() == 1) return filteredRules.get(0);
		else if(filteredRules.size() == 2) return filteredRules.get(0).andThen(filteredRules.get(1));
		
		Rule[] filteredRuleArray = filteredRules.toArray(new Rule[0]);
		//This is the heart of it; everything before there should be meaning-preserving transformations
		return (attacker, defender) -> {
			for(Rule rule : filteredRuleArray) {
				TriState result = rule.apply(attacker, defender);
				if(result != TriState.DEFAULT) return result;
			}
			return TriState.DEFAULT;
		};
	}
	
	static Rule difficultyCase(Rule easy, Rule normal, Rule hard) {
		return (attacker, defender) -> {
			switch(attacker.world.getDifficulty()) {
				case EASY: return easy.apply(attacker, defender);
				case NORMAL: return normal.apply(attacker, defender);
				case HARD: return hard.apply(attacker, defender);
			}
			return TriState.DEFAULT; //peaceful?
		};
	}
	
	// Builtin rules
	
	static Rule clojure() {
		return (attacker, defender) -> Init.clojureProxy.allowedToTargetPlayer(attacker, defender);
	}
	
	static Rule predicated(Partial partial, TriState ifTrue, TriState ifFalse) {
		if(ifTrue == ifFalse) return alwaysRule(ifTrue);
		else return (attacker, defender) -> partial.test(attacker, defender) ? ifTrue : ifFalse;
	}
}