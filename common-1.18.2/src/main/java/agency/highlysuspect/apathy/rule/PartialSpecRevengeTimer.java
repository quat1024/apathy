package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSerializer;
import agency.highlysuspect.apathy.core.rule.PartialSpec;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import com.google.gson.JsonObject;

public record PartialSpecRevengeTimer(long timer) implements PartialSpec<PartialSpecRevengeTimer> {
	@Override
	public PartialSpec<?> optimize() {
		if(timer <= 0) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> VerConv.mobExt(attacker).apathy$lastAttackedWithin(timer);
	}
	
	@Override
	public PartialSerializer<PartialSpecRevengeTimer> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecRevengeTimer> {
		private Serializer() {}
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
