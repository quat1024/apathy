package agency.highlysuspect.apathy.core.wrapper;

/**
 * wrapper around TagKey<EntityType<?>> and assorted infrastructure
 */
public interface AttackerTag {
	Object apathy$underlyingObject();
	boolean apathy$is(Attacker attacker);
	String apathy$id(); //resourcelocation-ish
}
