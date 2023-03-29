package agency.highlysuspect.apathy.core.rule;

public interface SerializablePartialSpec<PRED extends SerializablePartialSpec<PRED>> {
	PartialSerializer<PRED> getSerializer();
}
