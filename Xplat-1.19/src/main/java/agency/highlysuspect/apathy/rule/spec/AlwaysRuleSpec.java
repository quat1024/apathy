package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AlwaysRuleSpec(TriState value) implements RuleSpec<AlwaysRuleSpec> {
	public static final AlwaysRuleSpec ALWAYS_ALLOW = new AlwaysRuleSpec(TriState.TRUE);
	public static final AlwaysRuleSpec ALWAYS_DENY = new AlwaysRuleSpec(TriState.FALSE);
	public static final AlwaysRuleSpec ALWAYS_PASS = new AlwaysRuleSpec(TriState.DEFAULT);
	
	public static AlwaysRuleSpec always(TriState which) {
		return switch(which) {
			case FALSE -> ALWAYS_DENY;
			case DEFAULT -> ALWAYS_PASS;
			case TRUE -> ALWAYS_ALLOW;
		};
	}
	
	@Override
	public Rule build() {
		return (attacker, defender) -> value;
	}
	
	@Override
	public RuleSerializer<AlwaysRuleSpec> getSerializer() {
		return AlwaysRuleSerializer.INSTANCE;
	}
	
	public static class AlwaysRuleSerializer implements RuleSerializer<AlwaysRuleSpec> {
		public static final AlwaysRuleSerializer INSTANCE = new AlwaysRuleSerializer();
		
		@Override
		public JsonObject write(AlwaysRuleSpec rule, JsonObject json) {
			json.addProperty("value", rule.value.toAllowDenyPassString());
			return json;
		}
		
		@Override
		public AlwaysRuleSpec read(JsonObject json) {
			return new AlwaysRuleSpec(CoolGsonHelper.getAllowDenyPassTriState(json, "value"));
		}
	}
	
	///CODEC HELLZONE///
	
	@Deprecated(forRemoval = true)
	public static final Codec<AlwaysRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.fieldOf("value").forGetter(a -> a.value)
	).apply(i, AlwaysRuleSpec::always));
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends RuleSpec<?>> codec() {
		return CODEC;
	}
}
