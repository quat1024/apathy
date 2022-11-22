package agency.highlysuspect.apathy.hell.rule;

import com.google.gson.JsonObject;

public record PartialSpecAlways(boolean always) implements PartialSpec<PartialSpecAlways> {
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
