package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;

public interface RuleSpec {
	Rule build();
	Codec<? extends RuleSpec> codec();
}
