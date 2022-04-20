package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.TriState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class CodecUtil {
	public static final Codec<Difficulty> DIFFICULTY = enumCodec("difficulty", Difficulty.class);
	public static final Codec<TriState> TRISTATE_ALLOW_DENY_PASS = renamedEnumCodec("tristate", TriState.class, "deny", "pass", "allow");
	
	public static <E extends Enum<E>> Codec<E> enumCodec(String errorName, Class<E> classs) {
		E[] values = classs.getEnumConstants();
		return renamedEnumCodec(errorName, classs, Arrays.stream(values).map(e -> e.name().toLowerCase(Locale.ROOT)).toArray(String[]::new));
	}
	
	public static <E extends Enum<E>> Codec<E> renamedEnumCodec(String errorName, Class<E> classs, String... names) {
		E[] values = classs.getEnumConstants();
		if(names.length != values.length) throw new IllegalArgumentException("Enum has " + values.length + " values but only " + names.length + " names supplied to renamedEnumCodec");
		
		String errorA = "Unknown " + errorName + " \"";
		String errorB = "\", must be one of " + Arrays.stream(names).map(n -> "\"" + n + "\"").collect(Collectors.joining(", "));
		
		return Codec.of(
			Codec.STRING.comap(e -> names[e.ordinal()]),
			Codec.STRING.flatMap(s -> { 
				for(int i = 0; i < values.length; i++) if(s.equals(names[i])) return DataResult.success(values[i]);
				return DataResult.error(errorA + s + errorB); 
			}),
			"[renamedEnum " + errorName + "]" 
		);
	}
	
	public static <T> Codec<Set<T>> setOf(Codec<T> codec) {
		//*fart noise*
		return codec.listOf().xmap(HashSet::new, ArrayList::new);
	}
	
	//This object from Minecraft has toJson and fromJson methods, but doesn't use a Codec.
	//This is the best way I found to lift this into the Codec world, but I'm a bit rusty on my codecs.
	//There might be a nicer way to do it?
	public static final Codec<LocationPredicate> LOCATION_PREDICATE_CODEC = Codec.of(new Encoder<>() {
		@Override
		public <T> DataResult<T> encode(LocationPredicate input, DynamicOps<T> ops, T prefix) {
			JsonElement element = input.serializeToJson();
			
			//DFU explodes on null map-values. Awesome
			JsonElement filteredForDfu = filterNulls(element);
			
			return DataResult.success(JsonOps.INSTANCE.convertTo(ops, filteredForDfu));
		}
		
		@Override
		public String toString() {
			return "LocationPredicate encoder";
		}
	}, new Decoder<>() {
		@Override
		public <T> DataResult<Pair<LocationPredicate, T>> decode(DynamicOps<T> ops, T input) {
			JsonElement asJson = ops.convertTo(JsonOps.INSTANCE, input);
			
			LocationPredicate pred;
			try {
				pred = LocationPredicate.fromJson(asJson);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return DataResult.error(e.getMessage());
			}
			
			return DataResult.success(Pair.of(pred, input));
		}
		
		@Override
		public String toString() {
			return "LocationPredicate decoder";
		}
	});
	
	public static JsonElement filterNulls(JsonElement element) {
		if(element.isJsonObject()) return filterNullsObject(element.getAsJsonObject());
		if(element.isJsonArray()) return filterNullsArray(element.getAsJsonArray());
		if(element.isJsonNull()) return new JsonObject(); //i guess!
		return element;
	}
	
	public static JsonObject filterNullsObject(JsonObject object) {
		JsonObject result = new JsonObject();
		for(String key : object.keySet()) {
			JsonElement value = object.get(key);
			if(value.isJsonNull()) continue;
			result.add(key, filterNulls(value));
		}
		return result;
	}
	
	public static JsonArray filterNullsArray(JsonArray array) {
		JsonArray result = new JsonArray();
		for(JsonElement value : array) {
			if(value.isJsonNull()) continue;
			result.add(filterNulls(value));
		}
		return result;
	}
	
	// See https://github.com/Mojang/DataFixerUpper/issues/62
	public static void main(String[] args) {
		JsonObject j = new JsonObject();
		j.add("test", JsonNull.INSTANCE);
		
		System.out.println(JsonOps.INSTANCE.convertTo(JsonOps.INSTANCE, j));
	}
	
	//Its not actually Codecs but idk i already have this class to throw stuff in
	public static ListTag writeVec3(Vec3 yes) {
		ListTag asdf = new ListTag();
		asdf.add(DoubleTag.valueOf(yes.x));
		asdf.add(DoubleTag.valueOf(yes.y));
		asdf.add(DoubleTag.valueOf(yes.z));
		return asdf;
	}
	
	public static Vec3 readVec3(ListTag asdf) {
		return new Vec3(asdf.getDouble(0), asdf.getDouble(1), asdf.getDouble(2));
	}
	
	public static final int VEC3_LIST_ID = DoubleTag.valueOf(69420).getId();
}
