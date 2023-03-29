package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonObject;

public interface PartialSerializer<PART> {
	void write(PART part, JsonObject json);
	PART read(JsonObject json);
	
	@SuppressWarnings("unchecked")
	default void writeErased(Object shit, JsonObject json) {
		write((PART) shit, json);
	}
}
