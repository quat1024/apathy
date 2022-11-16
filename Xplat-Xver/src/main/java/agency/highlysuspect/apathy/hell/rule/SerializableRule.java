package agency.highlysuspect.apathy.hell.rule;

public interface SerializableRule<RULE extends SerializableRule<RULE>> {
	RuleSerializer<RULE> getSerializer();
}
