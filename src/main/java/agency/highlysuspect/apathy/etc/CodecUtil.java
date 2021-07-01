package agency.highlysuspect.apathy.etc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.world.Difficulty;

import java.util.*;
import java.util.stream.Collectors;

public class CodecUtil {
	public static final Codec<Difficulty> DIFFICULTY = enumCodec("difficulty", Difficulty.class);
	public static final Codec<TriState> TRISTATE_ALLOW_DENY_PASS = renamedEnumCodec("tristate", TriState.class, "deny", "pass", "allow");
	
	public static <E extends Enum<E>> Codec<E> enumCodec(String errorName, Class<E> classs) {
		E[] values = classs.getEnumConstants();
		
		String errorA = "Unknown " + errorName + " \"";
		String errorB = "\", must be one of " + Arrays.stream(values).map(e -> "\"" + e.name() + "\"").collect(Collectors.joining(", "));
		
		return Codec.STRING.comapFlatMap(s -> {
			for(E e : values) if(e.name().equals(s)) return DataResult.success(e);
			return DataResult.error(errorA + s + errorB);
		}, E::name);
	}
	
	public static <E extends Enum<E>> Codec<E> renamedEnumCodec(String errorName, Class<E> classs, String... names) {
		E[] values = classs.getEnumConstants();
		if(names.length != values.length) throw new IllegalArgumentException("Enum has " + values.length + " values but only " + names.length + " names supplied to renamedEnumCodec");
		
		String errorA = "Unknown " + errorName + " \"";
		String errorB = "\", must be one of " + Arrays.stream(names).map(n -> "\"" + n + "\"").collect(Collectors.joining(", "));
		
		return Codec.STRING.comapFlatMap(s -> {
			for(int i = 0; i < values.length; i++) if(s.equals(names[i])) return DataResult.success(values[i]);
			return DataResult.error(errorA + s + errorB);
		}, e -> names[e.ordinal()]);
	}
	
	public static <T> Codec<Set<T>> setOf(Codec<T> codec) {
		//*fart noise*
		return codec.listOf().xmap(HashSet::new, ArrayList::new);
	}
}