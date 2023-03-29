class BossConfig {
	//...
	
	@Override
	protected Config upgrade() {
		if(configVersion == 0) {
			//noDragon option was replaced with a couple separate options for the Bliss modpack
			if(unknownKeys != null && unknownKeys.getOrDefault("noDragon", "false").trim().toLowerCase(Locale.ROOT).equals("true")) {
				dragonInitialState = DragonInitialState.CALM;
				portalInitialState = PortalInitialState.OPEN_WITH_EGG;
				resummonSequence = ResummonSequence.SPAWN_GATEWAY;
			}
			
			configVersion = 1; //Finished upgrading to v1
		}
		
		if(configVersion == 1) {
			//noWither option was replaced with a difficulty set. If contained in the set, the wither is enabled.
			//therefore noWither = true should map to an empty difficulty set
			if(unknownKeys != null && unknownKeys.getOrDefault("noWither", "false").trim().toLowerCase(Locale.ROOT).equals("true")) {
				witherDifficulties = new HashSet<>();
			}
			
			configVersion = 2; //Finished upgrading to v2
		}
		
		return this;
	}
	
	//...
}