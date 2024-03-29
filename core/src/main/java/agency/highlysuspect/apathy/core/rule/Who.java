package agency.highlysuspect.apathy.core.rule;

import java.util.Locale;

public enum Who {
	ATTACKER,
	DEFENDER;
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static Who fromString(String name) {
		switch(name) {
			case "attacker": return ATTACKER;
			case "defender": return DEFENDER;
			default: throw new IllegalArgumentException("expected 'attacker' or 'defender'");
		}
	}
	
	//"spooky action at a distance" type punning; returns a common superclass of its arguments
	public <SUPER, SUB1 extends SUPER, SUB2 extends SUPER> SUPER choose(SUB1 attacker, SUB2 defender) {
		if(this == ATTACKER) return attacker;
		else return defender;
	}
}
