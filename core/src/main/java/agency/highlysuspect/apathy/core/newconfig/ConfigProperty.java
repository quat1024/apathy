package agency.highlysuspect.apathy.core.newconfig;

import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ConfigProperty<T> {
	String name();
	List<String> comment();
	
	Type type();
	T defaultValue();
	
	String write(T thing);
	T parse(String s);
	
	default void validate(T thing) {
		validate(this, thing);
	}
	
	default void validate(ConfigProperty<T> self, T thing) {
		//seems good
	}
	
	class Builder<T, BUILDER extends Builder<T, BUILDER>> {
		public Builder(String name, Type type, T defaultValue) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
		}
		
		private final String name;
		private final List<String> comment = new ArrayList<>();
		private final Type type;
		private final T defaultValue;
		private @Nullable Function<T, String> writer;
		private @Nullable Function<String, T> parser;
		private @Nullable BiConsumer<ConfigProperty<T>, T> validator = null;
		
		public BUILDER comment(String... comment) {
			this.comment.addAll(Arrays.asList(comment));
			return self();
		}
		
		public BUILDER writer(Function<T, String> writer) {
			this.writer = writer;
			return self();
		}
		
		public BUILDER parser(Function<String, T> parser) {
			this.parser = parser;
			return self();
		}
		
		public BUILDER addValidator(BiConsumer<ConfigProperty<T>, T> validator) {
			if(this.validator == null) this.validator = validator;
			else this.validator = this.validator.andThen(validator);
			return self();
		}
		
		public ConfigProperty<T> build() {
			if(writer == null || parser == null) throw new IllegalStateException("missing writer/parser");
			
			return new ConfigProperty<T>() {
				@Override
				public String name() {
					return name;
				}
				
				@Override
				public List<String> comment() {
					return comment;
				}
				
				@Override
				public Type type() {
					return type;
				}
				
				@Override
				public T defaultValue() {
					return defaultValue;
				}
				
				@Override
				public String write(T thing) {
					return writer.apply(thing);
				}
				
				@Override
				public T parse(String s) {
					return parser.apply(s);
				}
				
				@Override
				public void validate(ConfigProperty<T> self, T thing) {
					if(validator != null)	validator.accept(self, thing);
				}
			};
		}
		
		@SuppressWarnings("unchecked")
		protected BUILDER self() {
			return (BUILDER) this;
		}
		
		@SuppressWarnings("unchecked")
		protected <W extends Builder<T, W>> W typePun() {
			return (W) this;
		}
	}
	
	class IntBuilder extends Builder<Integer, IntBuilder> {
		public IntBuilder(String name, Integer defaultValue) {
			super(name, Integer.class, defaultValue);
			writer(x -> Integer.toString(x));
			parser(Integer::parseInt);
		}
		
		public IntBuilder atLeast(int min) {
			return addValidator((self, x) -> {
				if(x < min) throw new IllegalArgumentException("Expected value of " + self.name() + " to be at least " + min + ".");
			});
		}
		
		public IntBuilder atMost(int max) {
			return addValidator((self, x) -> {
				if(x > max) throw new IllegalArgumentException("Expected value of " + self.name() + " to be at most " + max + ".");
			});
		}
	}
	
	static <B extends Builder<Boolean, B>> B boolOpt(String name, boolean defaultValue, String... comment) {
		return new Builder<Boolean, B>(name, Boolean.class, defaultValue)
			.comment(comment)
			.writer(x -> Boolean.toString(x))
			.parser(Boolean::parseBoolean);
	}
	
	static IntBuilder intOpt(String name, int defaultValue, String... comment) {
		return new IntBuilder(name, defaultValue).comment(comment);
	}
	
	static <B extends Builder<Set<ApathyDifficulty>, B>> B difficultySetOpt(String name, Set<ApathyDifficulty> defaultValue, String... comment) {
		return new Builder<Set<ApathyDifficulty>, B>(name, Set.class, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.sorted()
				.map(d -> d.name().toLowerCase(Locale.ROOT))
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.split(","))
				.map(String::trim)
				.map(ApathyDifficulty::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
	}
}
