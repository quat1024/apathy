package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonObject;

public class PartialSpecAlways implements Spec<Partial, PartialSpecAlways> {
	private PartialSpecAlways(boolean always) { //use get() instead of this constructor
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
	public JsonSerializer<PartialSpecAlways> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAlways> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAlways thing, JsonObject json) {
			json.addProperty("value", thing.always);
		}
		
		@Override
		public PartialSpecAlways read(JsonObject json) {
			return get(json.getAsJsonPrimitive("value").getAsBoolean());
		}
	}
}
