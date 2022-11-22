package agency.highlysuspect.apathy.hell.rule;

import java.util.Locale;

public enum Who {
	ATTACKER,
	DEFENDER;
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static Who fromString(String name) {
		return switch(name) {
			case "attacker" -> ATTACKER;
			case "defender" -> DEFENDER;
			default -> throw new IllegalArgumentException("expected 'attacker' or 'defender'");
		};
	}
}
