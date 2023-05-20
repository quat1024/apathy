package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import com.google.gson.JsonObject;

public class RuleSpecIf implements Spec<Rule, RuleSpecIf> {
	public RuleSpecIf(Spec<Rule, ?> ifTrue, Spec<Rule, ?> ifFalse, Spec<Partial, ?> predicate) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
		this.predicate = predicate;
	}
	
	private final Spec<Rule, ?> ifTrue;
	private final Spec<Rule, ?> ifFalse;
	private final Spec<Partial, ?> predicate;
	
	@Override
	public Spec<Rule, ?> optimize() {
		Spec<Rule, ?> ifTrue = this.ifTrue.optimize();
		Spec<Rule, ?> ifFalse = this.ifFalse.optimize();
		Spec<Partial, ?> predicate = this.predicate.optimize();
		
		if(predicate == PartialSpecAlways.TRUE) return ifTrue;
		else if(predicate == PartialSpecAlways.FALSE) return ifFalse;
		else if(ifTrue == ifFalse) return ifTrue;
		else return new RuleSpecIf(ifTrue, ifFalse, predicate);
	}
	
	@Override
	public Rule build() {
		Partial condition = this.predicate.build();
		Rule ifTrue = this.ifTrue.build();
		Rule ifFalse = this.ifFalse.build();
		return (attacker, defender) -> condition.test(attacker, defender) ? ifTrue.apply(attacker, defender) : ifFalse.apply(attacker, defender); 
	}
	
	@Override
	public JsonSerializer<RuleSpecIf> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<RuleSpecIf> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecIf thing, JsonObject json) {
			json.add("if_true", Apathy.instance.writeRule(thing.ifTrue));
			json.add("if_false", Apathy.instance.writeRule(thing.ifFalse));
			json.add("predicate", Apathy.instance.writePartial(thing.predicate));
		}
		
		@Override
		public RuleSpecIf read(JsonObject json) {
			Spec<Rule, ?> ifTrue = json.has("if_true") ? Apathy.instance.readRule(json.get("if_true")) : RuleSpecAlways.ALWAYS_PASS;
			Spec<Rule, ?> ifFalse = json.has("if_false") ? Apathy.instance.readRule(json.get("if_false")) : RuleSpecAlways.ALWAYS_PASS;
			Spec<Partial, ?> predicate = Apathy.instance.readPartial(json.get("predicate"));
			return new RuleSpecIf(ifTrue, ifFalse, predicate);
		}
	}
}
