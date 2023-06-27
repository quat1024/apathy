package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.config.ConfigProperty;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;

import java.util.Set;

public class VerBossOptions {
	public static final ConfigProperty<Set<ApathyDifficulty>> wardenDarknessDifficuties =
		ConfigProperty.difficultySetOpt("wardenDarknessDifficulties", ApathyDifficulty.allDifficultiesNotPeaceful(), 
			"Difficulties where the Warden is allowed to apply the Darkness effect.", 
			"(To completely disable the Warden, please use the doWardenSpawning gamerule.)").build();
	
	//lmao what was i thinking when i named this property
	public static final ConfigProperty<Boolean> wardenDarknessOnlyToPlayersItCanTarget =
		ConfigProperty.boolOpt("wardenDarknessOnlyToPlayersItCanTarget", false,
			"When this setting is 'true', if the Warden is prevented from targeting a player", 
			"due to Apathy rules, it will also not apply the Darkness effect to that player.").build();
	
	public static void visit(ConfigSchema schema) {
		schema.section("Warden", wardenDarknessDifficuties, wardenDarknessOnlyToPlayersItCanTarget);
	}
}
