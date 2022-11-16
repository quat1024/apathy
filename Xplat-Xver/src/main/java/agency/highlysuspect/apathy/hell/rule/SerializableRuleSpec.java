package agency.highlysuspect.apathy.hell.rule;

public interface SerializableRuleSpec<RULE extends SerializableRuleSpec<RULE>> {
	RuleSerializer<RULE> getSerializer();
}
