package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.hell.NotRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

/**
 * @deprecated It's time to move away from Codec-based bullshit in this mod
 */
@Deprecated(forRemoval = true)
public class CodeccyNotRegistry<T> extends NotRegistry<T> {
	public CodeccyNotRegistry(String name) {
		this.name = name;
	}
	
	private final String name;
	
	///
	
	public DataResult<T> getOrFail(String name) {
		T result = get(name);
		if(result == null) {
			String validItems = String.join(", ", super.byName.keySet());
			return DataResult.error("Unknown item " + name + " in NotRegistry " + this.name + ". Valid items: " + validItems);
		}
		else return DataResult.success(result);
	}
	
	public Codec<T> byNameCodec() {
		//no DataResult on the right half, i would simply never write a bug and make an unregistered item :clueless:
		return Codec.STRING.comapFlatMap(this::getOrFail, this::getName);
	}
}
