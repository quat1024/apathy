package agency.highlysuspect.apathy.core.newconfig;

import java.util.Arrays;

public interface ConfigProperty<T> {
	String name();
	Iterable<String> comment();
	
	T defaultValue();
	
	String write(T thing);
	T parse(String s);
	
	default void validate(T thing) {
		//seems good
	}
	
	static ConfigProperty<Boolean> bool(String name, boolean defaultValue, String... comment) {
		return new ConfigProperty<Boolean>() {
			@Override
			public String name() {
				return name;
			}
			
			@Override
			public Iterable<String> comment() {
				return Arrays.asList(comment);
			}
			
			@Override
			public Boolean defaultValue() {
				return defaultValue;
			}
			
			@Override
			public String write(Boolean thing) {
				return Boolean.toString(thing);
			}
			
			@Override
			public Boolean parse(String s) {
				return Boolean.parseBoolean(s);
			}
		};
	}
}
