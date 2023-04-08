package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PartialSpecAny implements Spec<Partial, PartialSpecAny> {
	public PartialSpecAny(Set<Spec<Partial, ?>> others) {
		this.others = others;
	}
	
	public final Set<Spec<Partial, ?>> others;
	
	@Override
	public Spec<Partial, ?> optimize() {
		Set<Spec<Partial, ?>> loweredSpecs = others.stream().map(Spec::optimize).collect(Collectors.toSet());
		
		//If an always-true partial is present, surely this partial is also always true.
		if(loweredSpecs.stream().anyMatch(pred -> pred == PartialSpecAlways.TRUE)) return PartialSpecAlways.TRUE;
		
		//Always-false partial can be ignored.
		loweredSpecs.removeIf(pred -> pred == PartialSpecAlways.FALSE);
		
		//If there are no partials left, uhh
		if(loweredSpecs.size() == 0) return PartialSpecAlways.FALSE;
		
		//If there is one partial left, we don't need the wrapping
		if(loweredSpecs.size() == 1) return loweredSpecs.iterator().next();
		
		return new PartialSpecAny(loweredSpecs);
	}
	
	@Override
	public Partial build() {
		Partial[] arrayParts = others.stream().map(Spec::build).toArray(Partial[]::new);
		return (attacker, defender) -> {
			for(Partial p : arrayParts) {
				if(p.test(attacker, defender)) return true;
			}
			return false;
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecAny> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAny> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAny thing, JsonObject json) {
			json.add("predicates", thing.others.stream().map(Apathy.instance::writePartial).collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecAny read(JsonObject json) {
			Set<Spec<Partial, ?>> partials = new HashSet<>();
			JsonArray partialsArray = json.getAsJsonArray("predicates");
			for(JsonElement e : partialsArray) partials.add(Apathy.instance.readPartial(e));
			return new PartialSpecAny(partials);
		}
	}
}
