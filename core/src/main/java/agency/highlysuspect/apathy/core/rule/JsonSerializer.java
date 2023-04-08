package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonObject;

public interface JsonSerializer<THING> {
	void write(THING thing, JsonObject json);
	THING read(JsonObject json);
	
	@SuppressWarnings("unchecked")
	default void writeErased(Object thing, JsonObject json) {
		write((THING) thing, json);
	}
}
