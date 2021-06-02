# TODO:

* Expose "attacker tagged with my:custom_tag" rule to regular config
* Check that the default config behavior is the same as Apathetic Mobs's default config
* Implement the special handling for bosses
* impl Spirit's JSON concept!
* Check rule optimizer behavior for things like "mobSet is empty, mobSetExcluded isn't pass"
* Write tests for the rule optimizer

## Wishlist

* "No boss" config options that remove the associated fight.
	* no Wither -> Building the Wither multiblock just gives you a nether star
	* no dragon -> Exit end portal is already open. Place 4 nether crystals to spawn an End gateway
* (internal note) Might be worth it to transition from TriState to a dedicated enum with better names. I'm a bit entrenched though