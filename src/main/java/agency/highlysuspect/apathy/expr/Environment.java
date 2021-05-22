package agency.highlysuspect.apathy.expr;

import java.util.HashMap;
import java.util.Map;

//Overly simple, no "chaining" environments yet.
public class Environment {
	private final Map<String, Node> values = new HashMap<>();
	
	public void addSymbol(String key, Node value) {
		values.put(key, value);
	}
	
	public Node lookupSymbol(String key) {
		return values.get(key);
	}
}
