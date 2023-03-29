package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import com.google.gson.JsonObject;

public class PartialSpecNot implements PartialSpec<PartialSpecNot> {
	public PartialSpecNot(PartialSpec<?> other) {
		this.other = other;
	}
	
	public final PartialSpec<?> other;
	
	@Override
	public PartialSpec<?> optimize() {
		if(other == PartialSpecAlways.FALSE) return PartialSpecAlways.TRUE;
		if(other == PartialSpecAlways.TRUE) return PartialSpecAlways.FALSE;
		
		if(other instanceof PartialSpecNot) {
			PartialSpecNot doubleNegative = (PartialSpecNot) other;
			return doubleNegative.other.optimize();
		}
		
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
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecNot part, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(part.other));
		}
		
		@Override
		public PartialSpecNot read(JsonObject json) {
			PartialSpec<?> other = Apathy.instance.readPartial(json.get("predicate"));
			return new PartialSpecNot(other);
		}
	}
}
