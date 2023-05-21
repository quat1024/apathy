package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.TriState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CoolGsonHelper {
	public static TriState getAllowDenyPassTriState(JsonObject json, String key) {
		JsonElement elem = json.get(key);
		if(!(elem instanceof JsonPrimitive)) throw new JsonParseException("Expected TriState at " + key);
		else return TriState.fromAllowDenyPassString(elem.getAsString());
	}
	
	public static TriState getAllowDenyPassTriState(JsonObject json, String key, TriState def) {
		JsonElement elem = json.get(key);
		if(!(elem instanceof JsonPrimitive)) return def;
		else return TriState.fromAllowDenyPassString(elem.getAsString());
	}
	
	public static <T extends JsonElement> Collector<T, ?, JsonArray> toJsonArray() {
		return Collector.of(JsonArray::new, JsonArray::add, (left, right) -> { left.addAll(right); return left; });
	}
	
	public static Stream<JsonElement> streamArray(JsonArray a) {
		return StreamSupport.stream(a.spliterator(), false);
	}
	
	//Sloppy backport of a method from a future version of gson
	public static Set<String> keySet(JsonObject obj) {
		return obj.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
	}
}
