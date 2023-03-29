package agency.highlysuspect.apathy.core.rule;

/**
 * A "rule spec" is one step removed from a rule. Rules themselves are lightweight single-method interfaces.
 * A rule spec knows a little more about how rules work.
 */
public interface RuleSpec<RULE extends SerializableRuleSpec<RULE>> extends SerializableRuleSpec<RULE> {
	/**
	 * Potentially lower this rulespec into a form that'd be less work to compute when build() is called.
	 * For example, a ChainRuleSpec with one entry can lower into only that entry, removing the wrapping.
	 */
	default RuleSpec<?> optimize() {
		return this;
	}
	
	/**
	 * Realize the actual computable rule.
	 */
	Rule build();
}
