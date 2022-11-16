package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.TriState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.util.function.Function;
import java.util.stream.Collector;

public class CoolGsonHelper {
	public static TriState getAllowDenyPassTriState(JsonObject json, String key) {
		JsonElement elem = json.get(key);
		if(!(elem instanceof JsonPrimitive prim)) throw new JsonParseException("Expected TriState at " + key);
		else return TriState.fromAllowDenyPassString(prim.getAsString());
	}
	
	public static TriState getAllowDenyPassTriState(JsonObject json, String key, TriState def) {
		JsonElement elem = json.get(key);
		if(!(elem instanceof JsonPrimitive prim)) return def;
		else return TriState.fromAllowDenyPassString(prim.getAsString());
	}
	
	public static <T extends JsonElement> Collector<T, ?, JsonArray> toJsonArray() {
		return Collector.of(JsonArray::new, JsonArray::add, (left, right) -> { left.addAll(right); return left; });
	}
}
