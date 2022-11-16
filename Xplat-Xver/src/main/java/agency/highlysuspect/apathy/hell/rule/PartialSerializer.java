package agency.highlysuspect.apathy.hell.rule;

import com.google.gson.JsonObject;

public interface PartialSerializer<PART> {
	JsonObject write(PART part, JsonObject json);
	PART read(JsonObject json);
	
	@SuppressWarnings("unchecked")
	default JsonObject writeErased(Object shit, JsonObject json) {
		return write((PART) shit, json);
	}
}
