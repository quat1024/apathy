package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonObject;

import java.util.Random;

public class PartialSpecRandom implements Spec<Partial, PartialSpecRandom> {
	public PartialSpecRandom(double chance) {
		this.chance = chance;
	}
	
	private final double chance;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(chance == 0) return PartialSpecAlways.FALSE;
		else if(chance == 1) return PartialSpecAlways.TRUE;
		else return this;
	}
	
	@Override
	public Partial build() {
		//forgive me, but everything more clever was simply not working
		return (attacker, defender) -> new Random(attacker.apathy$uuidBits()).nextFloat() <= chance;
	}
	
	@Override
	public JsonSerializer<PartialSpecRandom> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecRandom> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecRandom thing, JsonObject json) {
			json.addProperty("chance", thing.chance);
		}
		
		@Override
		public PartialSpecRandom read(JsonObject json) {
			return new PartialSpecRandom(json.get("chance").getAsDouble());
		}
	}
}
