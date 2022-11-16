package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Specs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record PartialSpecAll(Set<PartialSpec<?>> others) implements PartialSpec<PartialSpecAll> {
	@Override
	public PartialSpec<?> optimize() {
		Set<PartialSpec<?>> loweredSpecs = others.stream().map(PartialSpec::optimize).collect(Collectors.toSet());
		
		//If an always-false predicate is here, surely this predicate will never match.
		if(loweredSpecs.stream().anyMatch(pred -> pred == PartialSpecAlways.FALSE)) return PartialSpecAlways.FALSE;
		
		//If an always-true predicate is here, it can be ignored
		loweredSpecs.removeIf(pred -> pred == PartialSpecAlways.TRUE);
		
		//If there are no specs left, ?? always fail I guess??
		if(loweredSpecs.size() == 0) return PartialSpecAlways.FALSE;
		
		//If there is one spec left, we don't need the wrapping
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
		public JsonObject write(PartialSpecAll pred, JsonObject json) {
			json.add("predicates", pred.others.stream().map(Apathy119.instance119::writePartial).collect(CoolGsonHelper.toJsonArray()));
			return json;
		}
		
		@Override
		public PartialSpecAll read(JsonObject json) {
			Set<PartialSpec<?>> partials = new HashSet<>();
			JsonArray partialsArray = json.getAsJsonArray("predicates");
			for(JsonElement e : partialsArray) partials.add(Apathy119.instance119.readPartial(e));
			return new PartialSpecAll(partials);
		}
	}
	
	///CODEC HELL///
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends PartialSpec<?>> codec() {
		return CODEC;
	}
	
	@Deprecated(forRemoval = true)
	public static final Codec<PartialSpecAll> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Specs.PREDICATE_SPEC_CODEC).fieldOf("predicates").forGetter(x -> x.others)
	).apply(i, PartialSpecAll::new));
}
