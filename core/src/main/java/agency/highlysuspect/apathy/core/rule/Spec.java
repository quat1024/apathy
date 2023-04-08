package agency.highlysuspect.apathy.core.rule;

/**
 * A "specification" for the type TYPE.
 * - Can inspect the TYPE and possibly lower it into an optimized form with optimize().
 * - Can construct a new instance of TYPE with build().
 * - Is associated with a JsonSerializer, which can read and write instances of TYPE to and from json objects.
 */
public interface Spec<TYPE, SELF extends Spec<TYPE, SELF>> {
	default Spec<TYPE, ?> optimize() {
		return this;
	}
	
	TYPE build();
	
	JsonSerializer<SELF> getSerializer();
}
