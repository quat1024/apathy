package agency.highlysuspect.apathy.coreplusminecraft.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.rule.Who;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.MinecraftConv;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartialSpecEffect implements Spec<Partial, PartialSpecEffect> {
	public PartialSpecEffect(Set<ResourceKey<MobEffect>> mobEffects, Who who) {
		this.mobEffects = mobEffects;
		this.who = who;
	}
	
	private final Set<ResourceKey<MobEffect>> mobEffects;
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
			ResourceKey<MobEffect> theEffect = mobEffects.iterator().next();
			return (attacker, defender) -> {
				LivingEntity which = who.choose(MinecraftConv.mob(attacker), MinecraftConv.player(defender));
				return ApathyPlusMinecraft.instanceMinecraft.hasEffect(which, theEffect);
			};
		} else return (attacker, defender) -> {
			LivingEntity which = who.choose(MinecraftConv.mob(attacker), MinecraftConv.player(defender));
			for(ResourceKey<MobEffect> effect : mobEffects) {
				if(ApathyPlusMinecraft.instanceMinecraft.hasEffect(which, effect)) return true;
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
				.map(ResourceKey::location)
				.filter(Objects::nonNull)
				.map(ResourceLocation::toString)
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
			json.addProperty("who", thing.who.toString());
		}
		
		@Override
		public PartialSpecEffect read(JsonObject json) {
			Set<ResourceKey<MobEffect>> mobEffects = CoolGsonHelper.streamArray(json.getAsJsonArray("effects"))
				.map(JsonElement::getAsString)
				.map(ApathyPlusMinecraft.instanceMinecraft::resource)
				.flatMap(rl -> {
					Optional<ResourceKey<MobEffect>> effect = ApathyPlusMinecraft.instanceMinecraft.mobEffectRegistry().getResourceKey(ApathyPlusMinecraft.instanceMinecraft.mobEffectRegistry().get(rl));
					if(!effect.isPresent()) {
						Apathy.instance.log.error("unknown mob effect: " + rl);
						return Stream.of();
					} else return Stream.of(effect.get());
				})
				.collect(Collectors.toSet());
			Who who = Who.fromString(json.get("who").getAsString());
			return new PartialSpecEffect(mobEffects, who);
		}
	}
}
