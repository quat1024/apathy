package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.TriState;

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
					ApathyHell.instance.log.warn("Value " + value + " on field " + sourceField.getName() + " is not one of allow/deny/pass. Defaulting to 'pass'.");
					return TriState.DEFAULT;
			}
		}
		
		@Override
		public String write(Field targetField, TriState value) {
			return switch(value) {
				case TRUE -> "allow";
				case FALSE -> "deny";
				case DEFAULT -> "pass";
			};
		}
	}
}
