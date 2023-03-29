package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonObject;

public class PartialSpecAlways implements PartialSpec<PartialSpecAlways> {
	public PartialSpecAlways(boolean always) {
		this.always = always;
	}
	
	public final boolean always;
	
	public static final PartialSpecAlways TRUE = new PartialSpecAlways(true);
	public static final PartialSpecAlways FALSE = new PartialSpecAlways(false);
	
	public static PartialSpecAlways get(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> always;
	}
	
	@Override
	public PartialSerializer<PartialSpecAlways> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAlways> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAlways part, JsonObject json) {
			json.addProperty("value", part.always);
		}
		
		@Override
		public PartialSpecAlways read(JsonObject json) {
			return get(json.getAsJsonPrimitive("value").getAsBoolean());
		}
	}
}
