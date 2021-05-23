package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.Init;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Symbol;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;

import java.util.Set;
import java.util.function.BiFunction;

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
	
	//Keywords: :a/b -> "b"
	//Symbols:  'a/b -> "b"
	//Strings passed as-is.
	//Not appropriate to use when you care about the namespace.
	private static String nameToString(Object a) {
		if(a instanceof String) return (String) a;
		else if(a instanceof Keyword) return ((Keyword) a).getName();
		else if(a instanceof Symbol) return ((Symbol) a).getName();
		else return a.toString(); //last-ditch effort
	}
	
	//Keywords: :a/b -> a:b, :b -> minecraft:b, I think keywords with colons are illegal syntax but they'll work lol
	//Symbols:  'a/b -> a:b, 'b -> minecraft:b, 'a:b -> a:b
	//Strings:  (the usual identifier conversion)
	//Appropriate to use when you care about the namespace.
	private static Identifier namespaceAndNameToIdentifier(Object a) {
		if(a instanceof String) return new Identifier((String) a);
		else if(a instanceof Keyword) {
			Keyword k = (Keyword) a;
			if(k.getNamespace() == null) return new Identifier(k.getName());
			else return new Identifier(k.getNamespace(), k.getName());
		}
		else if(a instanceof Symbol) {
			Symbol s = (Symbol) a;
			if(s.getNamespace() == null) return new Identifier(s.getName());
			else return new Identifier(s.getNamespace(), s.getName());
		}
		else return new Identifier(a.toString());
	}
	
	//Attacker tag
	public static Tag<EntityType<?>> parseEntityTypeTag(Object a) {
		Identifier id = namespaceAndNameToIdentifier(a);
		return TagRegistry.entityType(id);
	}
	
	//I couldn't figure out how to implement this nicely in Clojure
	public static BiFunction<MobEntity, PlayerEntity, Boolean> copOut(Set<Tag<EntityType<?>>> tags) {
		return (mob, player) -> {
			for(Tag<EntityType<?>> t : tags) if (t.contains(mob.getType())) return true;
			return false;
		};
	}
	
	public static boolean entityHasTag(Entity e, Tag<EntityType<?>> tag) {
		return tag.contains(e.getType());
	}
	
	//Entity type
	public static EntityType<?> parseEntityType(Object a) {
		Identifier id = namespaceAndNameToIdentifier(a);
		return Registry.ENTITY_TYPE.get(id);
	}
	
	public static EntityType<?> entityTypeOf(Entity e) {
		return e.getType();
	}
	
	//Difficulty
	public static Difficulty parseDifficulty(Object a) {
		return Difficulty.byName(nameToString(a));
	}
	
	public static Difficulty difficultyOf(MobEntity attacker) {
		return attacker.world.getDifficulty();
	}
}
