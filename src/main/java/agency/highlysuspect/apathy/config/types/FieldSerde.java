package agency.highlysuspect.apathy.config.types;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//we have Codec at home.

public interface FieldSerde<T> {
	String write(Field targetField, T value);
	T parse(Field sourceField, String value);
	
	//Java generic hell don't mind me
	default String writeErased(Field targetField, Object value) {
		//noinspection unchecked
		return write(targetField, (T) value);
	}
	
	default <U> FieldSerde<U> dimap(Function<T, U> into, Function<U, T> from) {
		FieldSerde<T> me = this;
		
		return new FieldSerde<>() {
			@Override
			public String write(Field targetField, U value) {
				return me.write(targetField, from.apply(value));
			}
			
			@Override
			public U parse(Field sourceField, String value) {
				return into.apply(me.parse(sourceField, value));
			}
		};
	}
	
	default FieldSerde<Set<T>> commaSeparatedSet(Comparator<T> sorter) {
		FieldSerde<T> me = this;
		
		return new FieldSerde<>() {
			@Override
			public String write(Field targetField, Set<T> value) {
				if(value == null) value = Collections.emptySet();
				
				List<T> hahaYes = new ArrayList<>(value);
				hahaYes.sort(sorter);
				
				StringBuilder bob = new StringBuilder();
				for(int i = 0; i < hahaYes.size(); i++) {
					bob.append(me.write(targetField, hahaYes.get(i)));
					if(i != hahaYes.size() - 1) bob.append(", ");
				}
				return bob.toString();
			}
			
			@Override
			public Set<T> parse(Field sourceField, String value) {
				return Arrays.stream(value.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(s -> me.parse(sourceField, s))
					.collect(Collectors.toSet());
			}
		};
	}
	
	default FieldSerde<List<T>> commaSeparatedList() {
		FieldSerde<T> me = this;
		
		return new FieldSerde<>() {
			@Override
			public String write(Field targetField, List<T> value) {
				StringBuilder bob = new StringBuilder();
				for(int i = 0; i < value.size(); i++) {
					bob.append(me.write(targetField, value.get(i)));
					if(i != value.size() - 1) bob.append(", ");
				}
				return bob.toString();
			}
			
			@Override
			public List<T> parse(Field sourceField, String value) {
				return Arrays.stream(value.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(s -> me.parse(sourceField, s))
					.collect(Collectors.toList());
			}
		};
	}
	
	default FieldSerde<Optional<T>> optional() {
		FieldSerde<T> me = this;
		
		return new FieldSerde<>() {
			@Override
			public String write(Field targetField, Optional<T> value) {
				if(value.isPresent()) return me.write(targetField, value.get());
				else return "";
			}
			
			@Override
			public Optional<T> parse(Field sourceField, String value) {
				if(value.isEmpty()) return Optional.empty();
				else return Optional.of(me.parse(sourceField, value));
			}
		};
	}
	
	//For types that can be trivially converted to a string using Java's toString.
	interface ToString<T> extends FieldSerde<T> {
		@Override
		default String write(Field targetField, T value) {
			return value.toString();
		}
	}
}
