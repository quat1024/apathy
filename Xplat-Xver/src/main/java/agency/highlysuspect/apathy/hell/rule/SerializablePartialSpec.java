package agency.highlysuspect.apathy.hell.rule;

public interface SerializablePartialSpec<PRED extends SerializablePartialSpec<PRED>> {
	PartialSerializer<PRED> getSerializer();
}
