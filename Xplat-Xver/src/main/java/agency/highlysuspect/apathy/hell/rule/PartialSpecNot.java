package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.ApathyHell;
import com.google.gson.JsonObject;

public record PartialSpecNot(PartialSpec<?> other) implements PartialSpec<PartialSpecNot> {
	@Override
	public PartialSpec<?> optimize() {
		if(other == PartialSpecAlways.FALSE) return PartialSpecAlways.TRUE;
		if(other == PartialSpecAlways.TRUE) return PartialSpecAlways.FALSE;
		
		if(other instanceof PartialSpecNot doubleNegative) return doubleNegative.other.optimize();
		
		else return this;
	}
	
	@Override
	public Partial build() {
		Partial built = other.build();
		return (attacker, defender) -> !built.test(attacker, defender);
	}
	
	@Override
	public PartialSerializer<PartialSpecNot> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecNot> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecNot part, JsonObject json) {
			json.add("predicate", ApathyHell.instance.writePartial(part.other));
		}
		
		@Override
		public PartialSpecNot read(JsonObject json) {
			PartialSpec<?> other = ApathyHell.instance.readPartial(json.get("predicate"));
			return new PartialSpecNot(other);
		}
	}
}
