package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Init;
import net.fabricmc.fabric.api.util.TriState;

import java.lang.reflect.Field;

public class TriStateField {
	public static class AllowDenyPass implements FieldSerde<TriState> {
		@Override
		public TriState parse(Field sourceField, String value) {
			switch(value) {
				case "allow": return TriState.TRUE;
				case "deny": return TriState.FALSE;
				case "pass": return TriState.DEFAULT;
				default:
					Init.LOG.warn("Value " + value + " on field " + sourceField.getName() + " is not one of allow/deny/pass. Defaulting to 'pass'.");
					return TriState.DEFAULT;
			}
		}
		
		@Override
		public String write(Field targetField, TriState value) {
			switch(value) {
				case TRUE: return "allow";
				case FALSE: return "deny";
				case DEFAULT: return "pass";
				default: throw new IllegalStateException("how'd you fit four states in a TriState?");
			}
		}
	}
}