package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.config.annotation.AtLeast;
import agency.highlysuspect.apathy.config.annotation.Comment;
import agency.highlysuspect.apathy.config.annotation.NoDefault;
import agency.highlysuspect.apathy.config.annotation.Section;
import agency.highlysuspect.apathy.config.annotation.Use;
import net.minecraft.world.Difficulty;

import java.util.Set;

@SuppressWarnings("CanBeFinal")
public class GeneralConfig extends Config {
	protected static final int CURRENT_CONFIG_VERSION = 0;
	@NoDefault protected int configVersion = CURRENT_CONFIG_VERSION;
	
	////////////////////////
	@Section("Optimization")
	////////////////////////
	
	@Comment({
		"By default, mobs that are currently attacking a player do not check every tick if it's still okay to do so.",
		"This is how often the mob will check. (Set this to 1 to check every tick.)"
	})
	@AtLeast(minInt = 1)
	public int recheckInterval = 20;
	
	@Comment({
		"Should Apathy attempt to optimize the rule in the config file, to do less work per tick?",
		"If your rule is doing something unexpected, or isn't working like it should, try turning this off.",
		"And report it too, since it's definitely a bug, lol."
	})
	public boolean runRuleOptimizer = true;
	
	//////////////////////////////////////
	@Section("Wow even more misc options")
	//////////////////////////////////////
	
	@Comment({
		"Comma-separated list of difficulties where zombies are allowed to attack villagers."
	})
	@Use("difficultySet")
	public Set<Difficulty> zombieAttackVillagerDifficulties = Apathy.allDifficultiesNotPeaceful();
	
	//////////////////////////
	@Section("Revenge Spread")
	//////////////////////////
	
	@Comment({
		"Comma-separated list of difficulties.",
		"If the current world difficulty appears in the set, zombified piglins will alert their friends",
		"when a player provokes one. This will also spread Apathy's revengeTimer to them."
	})
	@Use("difficultySet")
	public Set<Difficulty> angryPiggies = Apathy.allDifficultiesNotPeaceful();
	
	@Comment({
		"Let's say this option is set to 10, and you attack a zombie. Other zombies within 10 blocks will have their revengeTimer set, too.",
		"This option affects mobs of the same type only.",
		"Please recall that the revengeTimer is an Apathy concept, and Apathy only ever suppresses normal mob AI.",
		"i.e, this won't cause a mob to attack a player that already wasn't going to."
	})
	@AtLeast(minInt = 0)
	public int sameTypeRevengeSpread = 0;
	
	@Comment({
		"When attacking any mob, other mobs within this range will have their revengeTimer set, too.",
		"This option affects all mobs, whether they have the same type or not.",
		"Please recall that the revengeTimer is an Apathy concept, and Apathy only ever suppresses normal mob AI.",
		"i.e, this won't cause a mob to attack a player that already wasn't going to."
	})
	@AtLeast(minInt = 0)
	public int differentTypeRevengeSpread = 0;
	
	/////////////////
	@Section("Debug")
	/////////////////
	
	@Comment({
		"If 'true', Apathy will dump the rule specified in the config file to the file 'comfig/apathy/dumps/builtin-rule.json'.",
		"It will also dump the automatically optimized rule out to 'config/apathy/dumps/builtin-rule-opt.json'.",
		"Maybe this is handy if you'd like to see what the .json format looks like for a particular config file."
	})
	public boolean debugBuiltinRule = false;
	
	@Comment({
		"If 'true', Apathy will dump the rule specified in mobs.json to the file 'config/apathy/dumps/json-rule.json'.",
		"It will also dump the automatically optimized rule out to 'config/apathy/dumps/json-rule-opt.json'."
	})
	public boolean debugJsonRule = false;
}
