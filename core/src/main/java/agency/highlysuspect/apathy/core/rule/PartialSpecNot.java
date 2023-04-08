package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import com.google.gson.JsonObject;

public class PartialSpecNot implements Spec<Partial, PartialSpecNot> {
	public PartialSpecNot(Spec<Partial, ?> other) {
		this.other = other;
	}
	
	public final Spec<Partial, ?> other;
	
	@Override
	public Spec<Partial, ?> optimize() {
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
	public JsonSerializer<PartialSpecNot> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecNot> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecNot thing, JsonObject json) {
			json.add("predicate", Apathy.instance.writePartial(thing.other));
		}
		
		@Override
		public PartialSpecNot read(JsonObject json) {
			Spec<Partial, ?> other = Apathy.instance.readPartial(json.get("predicate"));
			return new PartialSpecNot(other);
		}
	}
}
