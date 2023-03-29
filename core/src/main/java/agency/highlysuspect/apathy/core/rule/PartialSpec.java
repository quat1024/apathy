package agency.highlysuspect.apathy.core.rule;

public interface PartialSpec<PART extends SerializablePartialSpec<PART>> extends SerializablePartialSpec<PART> {
	default PartialSpec<?> optimize() {
		return this;
	}
	
	Partial build();
}
