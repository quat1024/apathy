package agency.highlysuspect.apathy.core.config;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.rule.Who;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ConfigProperty<T> {
	String name();
	List<String> comment();
	
	T defaultValue();
	
	String write(T thing);
	T parse(String s);
	
	default void validate(ConfigProperty<T> self, T thing) {
		//seems good; throw on exception
	}
	
	/**
	 * BUILDER is a "curiously recurring template pattern" generic arg, to allow for ergonomic subclassing
	 * at the cost of really terrible generic signatures LOL
	 */
	class Builder<T, BUILDER extends Builder<T, BUILDER>> {
		public Builder(String name, T defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}
		
		private final String name;
		private List<String> comment = null;
		private final T defaultValue;
		private @Nullable Function<T, String> writer;
		private @Nullable Function<String, T> parser;
		private @Nullable BiConsumer<ConfigProperty<T>, T> validator = null;
		
		public BUILDER comment(String... comment) {
			if(this.comment == null) this.comment = new ArrayList<>(4);
			this.comment.addAll(Arrays.asList(comment));
			return self();
		}
		
		public BUILDER note(String... note) {
			boolean first = true;
			for(String noteLine : note) {
				comment((first ? "Note: " : "     ") + noteLine);
				first = false;
			}
			return self();
		}
		
		public BUILDER example(String example) {
			comment("Example: " + example);
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
					return comment == null ? Collections.emptyList() : comment;
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
		
		//"curiously recurring template pattern" support method
		@SuppressWarnings("unchecked")
		protected BUILDER self() {
			return (BUILDER) this;
		}
	}
	
	class IntBuilder extends Builder<Integer, IntBuilder> {
		public IntBuilder(String name, Integer defaultValue) {
			super(name, defaultValue);
			writer(x -> Integer.toString(x));
			parser(s -> Integer.parseInt(s.trim()));
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
	
	class LongBuilder extends Builder<Long, LongBuilder> {
		public LongBuilder(String name, Long defaultValue) {
			super(name, defaultValue);
			writer(l -> Long.toString(l));
			parser(s -> Long.parseLong(s.trim()));
		}
		
		public LongBuilder atLeast(long min) {
			return addValidator((self, x) -> {
				if(x < min) throw new IllegalArgumentException("Expected value of " + self.name() + " to be at least " + min + ".");
			});
		}
		
		public LongBuilder atMost(long max) {
			return addValidator((self, x) -> {
				if(x > max) throw new IllegalArgumentException("Expected value of " + self.name() + " to be at most " + max + ".");
			});
		}
	}
	
	static IntBuilder intOpt(String name, int defaultValue, String... comment) {
		return new IntBuilder(name, defaultValue).comment(comment);
	}
	
	static LongBuilder longOpt(String name, long defaultValue, String... comment) {
		return new LongBuilder(name, defaultValue).comment(comment);
	}
	
	static <B extends Builder<Boolean, B>> B boolOpt(String name, boolean defaultValue, String... comment) {
		return new Builder<Boolean, B>(name, defaultValue)
			.comment(comment)
			.writer(x -> Boolean.toString(x))
			.parser(s -> Boolean.parseBoolean(s.trim()));
	}
	
	static <B extends Builder<String, B>> B stringOpt(String name, String defaultValue, String... comment) {
		return new Builder<String, B>(name, defaultValue)
			.comment(comment)
			.writer(String::trim)
			.parser(String::trim);
	}
	
	static <B extends Builder<Optional<String>, B>> B optionalStringOpt(String name, Optional<String> defaultValue, String... comment) {
		return new Builder<Optional<String>, B>(name, defaultValue)
			.comment(comment)
			.writer(opt -> opt.orElse(""))
			.parser(s -> s.trim().isEmpty() ? Optional.empty() : Optional.of(s));
	}
	
	static <B extends Builder<List<String>, B>> B stringListOpt(String name, List<String> defaultValue, String... comment) {
		return new Builder<List<String>, B>(name, defaultValue)
			.comment(comment)
			.writer(l -> String.join(", ", l))
			.parser(s -> Arrays.stream(s.trim().split(","))
				.map(String::trim)
				.collect(Collectors.toList()));
	}
	
	static <B extends Builder<Set<ApathyDifficulty>, B>> B difficultySetOpt(String name, Set<ApathyDifficulty> defaultValue, String... comment) {
		return new Builder<Set<ApathyDifficulty>, B>(name, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.sorted()
				.map(d -> d.name().toLowerCase(Locale.ROOT))
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.trim().split(","))
				.map(String::trim)
				.map(ApathyDifficulty::fromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
	}
	
	static <B extends Builder<TriState, B>> B allowDenyPassOpt(String name, TriState defaultValue, String... comment) {
		return new Builder<TriState, B>(name, defaultValue)
			.comment(comment)
			.writer(TriState::toAllowDenyPassString)
			.parser(s -> TriState.fromAllowDenyPassString(s.trim()));
	}
	
	static <B extends Builder<Boolean, B>> B boolAllowDenyOpt(String name, boolean defaultValue, String... comment) {
		return new Builder<Boolean, B>(name, defaultValue)
			.comment(comment)
			.writer(b -> b ? "allow" : "deny")
			.parser(s -> s.trim().equalsIgnoreCase("allow"));
	}
	
	static <E extends Enum<?>, B extends Builder<E, B>> B enumOpt(String name, E defaultValue, String... comment) {
		@SuppressWarnings("unchecked") Class<E> enumClass = (Class<E>) defaultValue.getClass();
		return new Builder<E, B>(name, defaultValue)
			.comment(comment)
			.writer(e -> e.name().toLowerCase(Locale.ROOT))
			.parser(s -> {
				s = s.trim();
				for(E e : enumClass.getEnumConstants()) {
					if(e.name().equalsIgnoreCase(s)) return e;
				}
				
				//error case
				//TODO make the other parsers this permissive as well, instead of throwing
				String possibleValues = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.joining("/"));
				Apathy.instance.log.warn("Value " + s + " on field " + name + " is not one of " + possibleValues + ". Defaulting to " + defaultValue.name().toLowerCase(Locale.ROOT));
				return defaultValue;
			});
	}
	
	static <B extends Builder<Set<AttackerType>, B>> B attackerTypeSetOpt(String name, Set<AttackerType> defaultValue, String... comment) {
		return new Builder<Set<AttackerType>, B>(name, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.map(AttackerType::apathy$id)
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.trim().split(","))
				.map(String::trim)
				.map(Apathy.instance::parseAttackerType)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
	}
	
	static <B extends Builder<Set<AttackerTag>, B>> B attackerTagSetOpt(String name, Set<AttackerTag> defaultValue, String... comment) {
		return new Builder<Set<AttackerTag>, B>(name, defaultValue)
			.comment(comment)
			.writer(set -> set.stream()
				.map(AttackerTag::apathy$id)
				.collect(Collectors.joining(", ")))
			.parser(s -> Arrays.stream(s.trim().split(","))
				.map(String::trim)
				.map(Apathy.instance::parseAttackerTag)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
	}
	
	static <B extends Builder<Who, B>> B whoOpt(String name, Who defaultValue, String... comment) {
		return new Builder<Who, B>(name, defaultValue)
			.comment(comment)
			.writer(Who::toString)
			.parser(Who::fromString);
	}
}
