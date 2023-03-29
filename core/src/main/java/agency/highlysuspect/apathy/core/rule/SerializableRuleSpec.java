package agency.highlysuspect.apathy.core.rule;

public interface SerializableRuleSpec<RULE extends SerializableRuleSpec<RULE>> {
	RuleSerializer<RULE> getSerializer();
}
