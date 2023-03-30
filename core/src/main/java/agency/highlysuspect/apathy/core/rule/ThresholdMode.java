package agency.highlysuspect.apathy.core.rule;

import java.util.Locale;

public enum ThresholdMode {
	AT_LEAST,
	AT_MOST,
	EQUAL;
	
	public boolean test(int score, int threshold) {
		switch(this) {
			case AT_LEAST: return score >= threshold;
			case AT_MOST: return score <= threshold;
			case EQUAL: return score == threshold;
			default: throw new IllegalArgumentException();
		}
	}
	
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static ThresholdMode fromString(String name) {
		switch(name) {
			case "at_least": return AT_LEAST;
			case "at_most": return AT_MOST;
			case "equal": return EQUAL;
			default: throw new IllegalArgumentException("expected 'at_least', 'at_most', or 'equal'");
		}
	}
}
