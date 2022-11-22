package agency.highlysuspect.apathy.hell.rule;

import com.google.gson.JsonObject;

public interface RuleSerializer<RULE> {
	void write(RULE rule, JsonObject json);
	RULE read(JsonObject json);
	
	@SuppressWarnings("unchecked")
	default void writeErased(Object shit, JsonObject json) {
		write((RULE) shit, json);
	}
}
