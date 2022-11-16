package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.ApathyHell;
import com.google.gson.JsonObject;

public interface RuleSerializer<RULE> {
	JsonObject write(RULE rule, JsonObject json);
	RULE read(JsonObject json);
	
	@SuppressWarnings("unchecked")
	default JsonObject writeErased(Object shit, JsonObject json) {
		return write((RULE) shit, json);
	}
}
