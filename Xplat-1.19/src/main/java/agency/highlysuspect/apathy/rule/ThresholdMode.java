package agency.highlysuspect.apathy.rule;

import java.util.Locale;

public enum ThresholdMode {
	AT_LEAST,
	AT_MOST,
	EQUAL;
	
	public boolean test(int score, int threshold) {
		return switch(this) {
			case AT_LEAST -> score >= threshold;
			case AT_MOST -> score <= threshold;
			case EQUAL -> score == threshold;
		};
	}
	
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static ThresholdMode fromString(String name) {
		return switch(name) {
			case "at_least" -> AT_LEAST;
			case "at_most" -> AT_MOST;
			case "equal" -> EQUAL;
			default -> throw new IllegalArgumentException("expected 'at_least', 'at_most', or 'equal'");
		};
	}
}
