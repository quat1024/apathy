package agency.highlysuspect.apathy.core.wrapper;

/**
 * wraps EntityType<?>
 */
public interface AttackerType {
	Object apathy$underlyingObject();
	
	boolean apathy$hasType(Attacker mob);
	
	String apathy$id();
}
