package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.SerializablePartialSpec;
import com.mojang.serialization.Codec;

public interface PartialSpec<PART extends SerializablePartialSpec<PART>> extends SerializablePartialSpec<PART> {
	default PartialSpec<?> optimize() {
		return this;
	}
	
	Partial build();
	
	/**
	 * @deprecated No codecs!!!!!!!!
	 */
	@Deprecated(forRemoval = true)
	Codec<? extends PartialSpec<?>> codec();
}
