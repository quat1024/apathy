package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.rule.Who;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
public class PartialSpecEffect implements Spec<Partial, PartialSpecEffect> {
	public PartialSpecEffect(Set<MobEffect> mobEffects, Who who) {
		this.mobEffects = mobEffects;
		this.who = who;
	}
	
	private final Set<MobEffect> mobEffects;
	private final Who who;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(mobEffects.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		//i love useless microoptimizations
		if(mobEffects.size() == 1) {
			MobEffect theEffect = mobEffects.iterator().next();
			return (attacker, defender) -> {
				LivingEntity which = who.choose(VerConv.mob(attacker), VerConv.player(defender));
				return which.hasEffect(theEffect);
			};
		} else return (attacker, defender) -> {
			LivingEntity which = who.choose(VerConv.mob(attacker), VerConv.player(defender));
			for(MobEffect effect : mobEffects) {
				if(which.hasEffect(effect)) return true;
			}
			return false;
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecEffect> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecEffect> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecEffect thing, JsonObject json) {
			json.add("effects", thing.mobEffects.stream()
				.map(BuiltInRegistries.MOB_EFFECT::getKey)
				.filter(Objects::nonNull)
				.map(ResourceLocation::toString)
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
			json.addProperty("who", thing.who.toString());
		}
		
		@Override
		public PartialSpecEffect read(JsonObject json) {
			Set<MobEffect> mobEffects = CoolGsonHelper.streamArray(json.getAsJsonArray("effects"))
				.map(JsonElement::getAsString)
				.map(ResourceLocation::new)
				.flatMap(rl -> {
					MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(rl);
					if(effect == null) {
						Apathy.instance.log.error("unknown mob effect: " + rl);
						return Stream.of();
					} else return Stream.of(effect);
				})
				.collect(Collectors.toSet());
			Who who = Who.fromString(json.get("who").getAsString());
			return new PartialSpecEffect(mobEffects, who);
		}
	}
}
