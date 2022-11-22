package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.ApathyHell;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record PartialSpecAll(Set<PartialSpec<?>> others) implements PartialSpec<PartialSpecAll> {
	@Override
	public PartialSpec<?> optimize() {
		Set<PartialSpec<?>> loweredSpecs = others.stream().map(PartialSpec::optimize).collect(Collectors.toSet());
		
		//If an always-false partial is here, surely this partial will never match.
		if(loweredSpecs.stream().anyMatch(pred -> pred == PartialSpecAlways.FALSE)) return PartialSpecAlways.FALSE;
		
		//If an always-true partial is here, it can be ignored
		loweredSpecs.removeIf(pred -> pred == PartialSpecAlways.TRUE);
		
		//If there are no partials left, ?? always fail I guess??
		if(loweredSpecs.size() == 0) return PartialSpecAlways.FALSE;
		
		//If there is one partial left, we don't need the wrapping
		if(loweredSpecs.size() == 1) return loweredSpecs.iterator().next();
		
		return new PartialSpecAll(loweredSpecs);
	}
	
	@Override
	public Partial build() {
		Partial[] arrayParts = others.stream().map(PartialSpec::build).toArray(Partial[]::new);
		return (attacker, defender) -> {
			for(Partial p : arrayParts) {
				if(!p.test(attacker, defender)) return false;
			}
			return true;
		};
	}
	
	@Override
	public PartialSerializer<PartialSpecAll> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAll> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAll pred, JsonObject json) {
			json.add("predicates", pred.others.stream().map(ApathyHell.instance::writePartial).collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecAll read(JsonObject json) {
			Set<PartialSpec<?>> partials = new HashSet<>();
			JsonArray partialsArray = json.getAsJsonArray("predicates");
			for(JsonElement e : partialsArray) partials.add(ApathyHell.instance.readPartial(e));
			return new PartialSpecAll(partials);
		}
	}
}
