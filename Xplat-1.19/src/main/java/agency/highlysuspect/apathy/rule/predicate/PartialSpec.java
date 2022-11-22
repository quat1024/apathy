package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.SerializablePartialSpec;

public interface PartialSpec<PART extends SerializablePartialSpec<PART>> extends SerializablePartialSpec<PART> {
	default PartialSpec<?> optimize() {
		return this;
	}
	
	Partial build();
}
