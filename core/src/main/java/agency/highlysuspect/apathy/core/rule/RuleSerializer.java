package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonObject;

public interface RuleSerializer<RULE> {
	void write(RULE rule, JsonObject json);
	RULE read(JsonObject json);
	
	@SuppressWarnings("unchecked")
	default void writeErased(Object rule, JsonObject json) {
		write((RULE) rule, json);
	}
}
