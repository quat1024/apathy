package agency.highlysuspect.apathy.hell.rule;

public interface PartialSpec<PART extends SerializablePartialSpec<PART>> extends SerializablePartialSpec<PART> {
	default PartialSpec<?> optimize() {
		return this;
	}
	
	Partial build();
}
