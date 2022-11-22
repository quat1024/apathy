package agency.highlysuspect.apathy.hell.wrapper;

import java.util.Locale;

public enum ApathyDifficulty {
	PEACEFUL, EASY, NORMAL, HARD;
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static ApathyDifficulty fromStringOrNull(String name) {
		return switch(name.toLowerCase(Locale.ROOT)) {
			case "peaceful" -> PEACEFUL;
			case "easy" -> EASY;
			case "normal" -> NORMAL;
			case "hard" -> HARD;
			default -> null;
		};
	}
	
	public static ApathyDifficulty fromString(String name) {
		ApathyDifficulty x = fromStringOrNull(name);
		if(x == null) throw new IllegalArgumentException("expected 'preaceful', 'easy', 'normal', or 'hard'");
		return x;
	}
}
