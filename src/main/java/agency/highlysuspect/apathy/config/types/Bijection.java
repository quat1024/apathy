package agency.highlysuspect.apathy.config.types;

import java.util.function.Function;

public interface Bijection<I, O> {
	O into(I value);
	
	I from(O value);
	
	static <I, O> Bijection<I, O> create(Function<I, O> into, Function<O, I> from) {
		return new Bijection<I, O>() {
			@Override
			public O into(I value) {
				return into.apply(value);
			}
			
			@Override
			public I from(O value) {
				return from.apply(value);
			}
		};
	}
}
