package agency.highlysuspect.apathy.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

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
	
	public static final Codec<ThresholdMode> CODEC = Codec.STRING.comapFlatMap((s -> switch(s) {
		case "at_least" -> DataResult.success(AT_LEAST);
		case "at_most" -> DataResult.success(AT_MOST);
		case "equal" -> DataResult.success(EQUAL);
		default -> DataResult.error("unknown predicate mode " + s);
	}), m -> m.name().toLowerCase(Locale.ROOT));
}
