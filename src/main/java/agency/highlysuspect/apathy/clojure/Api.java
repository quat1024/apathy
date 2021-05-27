package agency.highlysuspect.apathy.clojure;

import agency.highlysuspect.apathy.Init;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Symbol;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;

// Most of the relevant things in here have more natural-feeling Clojure bindings defined in apathy-startup.clj. There's no need to import Api yourself. Hopefully.
public class Api {
	//These are initialized in the startup clojure script
	//I'd love to give them sensible defaults Java-side but hoo, IFn has 20 methods to implement for different arities lmao
	public static IFn clojureRule = null;
	
	///////////////// Inward-facing API
	
	public static TriState allowedToTargetPlayer(MobEntity attacker, PlayerEntity target) {
		if(attacker.world.isClient) throw new IllegalStateException("No script execution on the client world... bad");
		
		Object ruleOutput = clojureRule.invoke(attacker, target);
		if(ruleOutput == null) {
			return TriState.DEFAULT;
		} else if(ruleOutput instanceof Keyword) {
			return blah(((Keyword) ruleOutput).getName());
		} else if(ruleOutput instanceof Symbol) {
			return blah(((Symbol) ruleOutput).getName());
		} else {
			return blah(ruleOutput.toString());
		}
	}
	
	private static TriState blah(String yea) {
		switch(yea) {
			case "deny": return TriState.FALSE;
			case "allow": return TriState.TRUE;
			default: return TriState.DEFAULT;
		}
	}
	
	///////////////// Outward-facing API
	
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
