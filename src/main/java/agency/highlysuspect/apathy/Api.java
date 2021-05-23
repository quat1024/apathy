package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.Init;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Symbol;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;

// Most of the relevant things in here have more natural-looking Clojure bindings defined in apathy-startup.clj. There's no need to import Api yourself.
public class Api {
	public static IFn rule;
	public static IFn toPreventTargetChangeBool;
	
	//Internal
	public static boolean preventAttackTargetChange(MobEntity attacker, PlayerEntity target) {
		attacker.world.getProfiler().push("apathy-clojure");
		
		Object ruleOutput = rule.invoke(attacker, target);
		boolean result = (boolean) toPreventTargetChangeBool.invoke(ruleOutput);
		
		attacker.world.getProfiler().pop();
		return result;
	}
	
	public static void log(String s) {
		Init.LOG.info(s);
	}
	
	public static void inspect(Object o) {
		//I have a debugger breakpoint on this in my IDE
		Init.LOG.info(o);
	}
	
	//Coerce various objects into Minecraft identifiers, including identifiers themselves, Clojure symbols and keywords, and strings.
	//Note that Clojure symbol coercion technically uses an internal Clojure API.
	//It's theoretically possible to implement this in Clojure itself, but Clojure doesn't know about remapping,
	//and I don't want users to have to worry about that either.
	//It's unfortunate that I have to touch minecraft classes with a ten-foot pole because of that.
	public static Identifier toIdentifier(Object a) {
		if(a instanceof Identifier) return (Identifier) a;
		else if(a instanceof Symbol) { // 'namespace/path
			Symbol s = (Symbol) a;
			return new Identifier(s.getNamespace(), s.getName());
		} else if(a instanceof Keyword) { // :namespace/path
			Keyword k = (Keyword) a;
			return new Identifier(k.getNamespace(), k.getName());
		} else if(a instanceof String) return new Identifier((String) a);
		else throw new RuntimeException("Don't know how to coerce " + a.getClass().toGenericString() + " to an Identifier.");
	}
	
	public static Tag<EntityType<?>> toEntityTypeTag(Object a) {
		Identifier id = toIdentifier(a);
		return TagRegistry.entityType(id);
	}
	
	public static EntityType<?> toEntityType(Object a) {
		Identifier id = toIdentifier(a);
		return Registry.ENTITY_TYPE.get(id);
	}
	
	public static Difficulty toDifficulty(Object a) {
		return Difficulty.byName(a.toString());
	}
	
	public static boolean attackerHasTag(MobEntity attacker, Object tagInto) {
		Tag<EntityType<?>> tag = toEntityTypeTag(tagInto);
		return tag.contains(attacker.getType());
	}
	
	public static boolean attackerIs(MobEntity attacker, EntityType<?> type) {
		return type.equals(attacker.getType());
	}
	
	public static boolean difficultyIs(MobEntity attacker, Difficulty difficulty) {
		return attacker.world.getDifficulty().equals(difficulty);
	}
}
