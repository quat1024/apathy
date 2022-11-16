package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PartialSpecAlways(boolean always) implements PartialSpec<PartialSpecAlways> {
	public static final PartialSpecAlways TRUE = new PartialSpecAlways(true);
	public static final PartialSpecAlways FALSE = new PartialSpecAlways(false);
	
	public static PartialSpecAlways get(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> always;
	}
	
	@Override
	public PartialSerializer<PartialSpecAlways> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecAlways> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public JsonObject write(PartialSpecAlways part, JsonObject json) {
			json.addProperty("value", part.always);
			return json;
		}
		
		@Override
		public PartialSpecAlways read(JsonObject json) {
			return get(json.getAsJsonPrimitive("value").getAsBoolean());
		}
	}
	
	//CODEC HELL//
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends PartialSpec<?>> codec() {
		return CODEC;
	}
	@Deprecated(forRemoval = true)
	public static final Codec<PartialSpecAlways> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.BOOL.fieldOf("value").forGetter(x -> x.always)
	).apply(i, PartialSpecAlways::get));
}
