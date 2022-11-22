package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import com.google.gson.JsonObject;

public record PartialSpecRevengeTimer(long timer) implements PartialSpec<PartialSpecRevengeTimer> {
	@Override
	public PartialSpec<?> optimize() {
		if(timer <= 0) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> ((MobExt) attacker).apathy$lastAttackedWithin(timer);
	}
	
	@Override
	public PartialSerializer<PartialSpecRevengeTimer> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecRevengeTimer> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecRevengeTimer part, JsonObject json) {
			json.addProperty("timeout", part.timer);
		}
		
		@Override
		public PartialSpecRevengeTimer read(JsonObject json) {
			return new PartialSpecRevengeTimer(json.getAsJsonPrimitive("timeout").getAsLong());
		}
	}
}
