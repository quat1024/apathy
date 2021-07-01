package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.util.TriState;

public interface RuleSpec {
	default RuleSpec optimize() {
		return this;
	}
	
	Rule build();
	Codec<? extends RuleSpec> codec();
	
}
