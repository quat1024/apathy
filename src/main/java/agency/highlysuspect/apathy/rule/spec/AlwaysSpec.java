package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

public class AlwaysSpec extends RuleSpec {
	public AlwaysSpec(TriState value) {
		this.value = value;
	}
	
	public static final Codec<AlwaysSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.fieldOf("value").forGetter(a -> a.value)
	).apply(i, AlwaysSpec::new));
	
	public final TriState value;
	
	@Override
	public Rule buildRule() {
		return Rule.alwaysRule(value);
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
