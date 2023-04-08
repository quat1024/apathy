package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.wrapper.Attacker;
import com.google.gson.JsonObject;

public final class PartialSpecRevengeTimer implements Spec<Partial, PartialSpecRevengeTimer> {
	public PartialSpecRevengeTimer(long timeout) {
		this.timeout = timeout;
	}
	
	private final long timeout;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(timeout <= 0) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			long provocationTime = attacker.apathy$getProvocationTime();
			return provocationTime != Attacker.NOT_PROVOKED && ((attacker.apathy$now() - provocationTime) <= timeout);
		};
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
			json.addProperty("timeout", thing.timeout);
		}
		
		@Override
		public PartialSpecRevengeTimer read(JsonObject json) {
			return new PartialSpecRevengeTimer(json.getAsJsonPrimitive("timeout").getAsLong());
		}
	}
}
