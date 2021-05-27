package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Init;
import net.fabricmc.fabric.api.util.TriState;

import java.lang.reflect.Field;

public class TriStateField {
	public static class AllowDenyDisabled implements FieldSerde<TriState> {
		@Override
		public TriState parse(Field sourceField, String value) {
			switch(value) {
				case "allow-list": return TriState.TRUE;
				case "deny-list": return TriState.FALSE;
				case "disabled": return TriState.DEFAULT;
				default:
					Init.LOG.warn("Unknown mob set mode " + value);
					return TriState.DEFAULT;
			}
		}
		
		@Override
		public String write(Field targetField, TriState value) {
			switch(value) {
				case TRUE: return "allow-list";
				case FALSE: return "deny-list";
				case DEFAULT: return "disabled";
				default: throw new IllegalStateException("how'd you fit four states in a TriState?");
			}
		}
	}
	
	public static class DifficultyListMode implements FieldSerde<TriState> {
		@Override
		public TriState parse(Field sourceField, String value) {
			switch(value) {
				case "difficulty-lock": return TriState.TRUE;
				case "difficulty-exclude": return TriState.FALSE;
				case "disabled": return TriState.DEFAULT;
				default:
					Init.LOG.warn("Unknown difficulty set mode " + value);
					return TriState.DEFAULT;
			}
		}
		
		@Override
		public String write(Field targetField, TriState value) {
			switch(value) {
				case TRUE: return "difficulty-lock";
				case FALSE: return "difficulty-exclude";
				case DEFAULT: return "disabled";
				default: throw new IllegalStateException("how'd you fit four states in a TriState?");
			}
		}
	}
}
