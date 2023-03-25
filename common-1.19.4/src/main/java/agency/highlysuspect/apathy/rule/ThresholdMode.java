package agency.highlysuspect.apathy.rule;

import com.mojang.serialization.Codec;

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
	
	public static final Codec<ThresholdMode> CODEC = CodecUtil.enumCodec("ThresholdMode", ThresholdMode.class);
}
