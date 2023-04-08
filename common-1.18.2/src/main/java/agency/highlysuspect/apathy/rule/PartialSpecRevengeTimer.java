package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.JsonObject;

public record PartialSpecRevengeTimer(long timer) implements Spec<Partial, PartialSpecRevengeTimer> {
	@Override
	public Spec<Partial, ?> optimize() {
		if(timer <= 0) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> VerConv.mobExt(attacker).apathy$lastAttackedWithin(timer);
	}
	
	@Override
	public JsonSerializer<PartialSpecRevengeTimer> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecRevengeTimer> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecRevengeTimer thing, JsonObject json) {
			json.addProperty("timeout", thing.timer);
		}
		
		@Override
		public PartialSpecRevengeTimer read(JsonObject json) {
			return new PartialSpecRevengeTimer(json.getAsJsonPrimitive("timeout").getAsLong());
		}
	}
}
