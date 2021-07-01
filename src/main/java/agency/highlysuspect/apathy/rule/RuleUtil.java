package agency.highlysuspect.apathy.rule;

import net.fabricmc.fabric.api.util.TriState;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class RuleUtil {
	public static String showTriState(TriState state) {
		switch(state) {
			case FALSE: return "deny";
			case DEFAULT: return "pass";
			case TRUE: return "allow";
			default: throw new IllegalStateException(state.toString());
		}
	}
	
	public static <T, C extends Collection<T>> T extractSingleton(C singletonCollection) {
		return singletonCollection.iterator().next();
	}
	
	public static <X, R, C extends Collection<X>> R sizeSpecialize(C things, Supplier<R> zero, Function<X, R> single, Function<C, R> multi) {
		if(things.size() == 0) {
			return zero.get();
		} else if(things.size() == 1) {
			return single.apply(extractSingleton(things));
		} else {
			return multi.apply(things);
		}
	}
	
	public static <X, R, C extends Collection<X>> R sizeSpecializeNotEmpty(C things, Function<X, R> single, Function<C, R> multi) {
		if(things.size() == 0) {
			throw new IllegalArgumentException("Empty set");
		} else if(things.size() == 1) {
			return single.apply(extractSingleton(things));
		} else {
			return multi.apply(things);
		}
	}
}
