package agency.highlysuspect.apathy;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class Config {
	public boolean preventAttackTargetChange(MobEntity attacker, PlayerEntity target) {
		for(Rule rule : rules) {
			switch(rule.check(attacker, target)) {
				case ALLOW_ATTACK: return false;
				case DENY_ATTACK: return true;
				default: //continue
			}
		}
		
		//None of the rules applied.
		return defaultBehavior == Rule.Result.DENY_ATTACK;
	}
	
	//An ordered list of rules to check.
	private List<Rule> rules = new ArrayList<>();
	//Whether the attacker is prevented from attacking the player if *none* of the rules apply.
	//TODO precondition that it's not dont_care
	private Rule.Result defaultBehavior;
}
