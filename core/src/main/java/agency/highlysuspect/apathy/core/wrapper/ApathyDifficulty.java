package agency.highlysuspect.apathy.core.wrapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum ApathyDifficulty {
	PEACEFUL, EASY, NORMAL, HARD;
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static ApathyDifficulty fromStringOrNull(String name) {
		switch(name.toLowerCase(Locale.ROOT)) {
			case "peaceful": return PEACEFUL;
			case "easy": return EASY;
			case "normal": return NORMAL;
			case "hard": return HARD;
			default: return null;
		}
	}
	
	public static ApathyDifficulty fromString(String name) {
		ApathyDifficulty x = fromStringOrNull(name);
		if(x == null) throw new IllegalArgumentException("expected 'peaceful', 'easy', 'normal', or 'hard'");
		return x;
	}
	
	public static Set<ApathyDifficulty> allDifficultiesNotPeaceful() {
		Set<ApathyDifficulty> funy = new HashSet<>(Arrays.asList(ApathyDifficulty.values()));
		funy.remove(PEACEFUL);
		return funy;
	}
}
