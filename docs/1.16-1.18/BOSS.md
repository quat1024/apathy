# `boss.cfg`

This file contains controls for various boss-fight sequences in Minecraft. You can disable or modify them.

# Dragon (before mod version 2.3)

## No dragon

Completely removes the Ender Dragon from the game.

* When you visit the End for the first time:
	* The Ender Dragon will not spawn when you visit the End.
	* You will get the "Free the End" advancement.
	* The exit End portal will already be open, complete with an Egg on top.
* If you place four end crystals on the End portal:
	* A new End Gateway will be generated.
	* You will get the "The End... Again..." advancement.
* Existing Ender Dragons *will* be deleted.

# Dragon (after mod version 2.3)

The "No dragon" option has been split into several different options, instead of one massive "completely remove the mob" setting.

If you are playing in a world with an older version of the mod, the requisite upgrades will be made for you (dragonInitialState -> `calm`, portalInitialState -> `open_with_egg`, resummonSequence -> `spawn_gateway`)

## Dragon initial state

Controls the initial state of the Ender Dragon.

* `default`: No changes are made to the vanilla. This is the default value.
* `passive_dragon`: No changes are made, except the Ender Dragon will not attack any players that didn't attack her first.
* `calm`: The initial Ender Dragon is removed. 
  * You probably still want to exit the End, so look at the next option.

This setting must be set before the first player enters the End.

## Portal initial state

Controls the initial state of the exit End Portal at the center of the island.

* `closed`: The portal is closed. This is the default value.
* `open`: The portal is already open, and can be used to exit the End.
* `open_with_egg`: The portal is open, and a Dragon Egg is placed on top.

This setting must be set before the first player enters the End.

## Initial End Gateway count

A number from 0 to 20. The corresponding number of End Gateways will already be opened.

This setting must be set before the first player enters the End.

## Resummon Sequence

Controls what happens when a player places four End Crystals on the exit End Portal.

* `default`: An Ender Dragon is summoned, just like vanilla. This is the default value.
* `spawn_gateway`: A short animation plays, and an End Gateway is created. No fight sequence is required.
* `disabled`: Nothing happens.

## Simulacra Dragon Advancements

This enables some miscellaneous features relating to vanilla advancements and Apathy's replacements for the mechanics those advancements are for. It defaults to `true`.

* If `dragonInitialState` is `calm` (removing it), players automatically obtain "Free the End" when they first visit the End.
* If `resummonSequence` is `spawn_gateway`, players obtain the advancement for resummoning the Ender Dragon when they trigger the custom gateway respawn cutscene.

# Dragon control (all versions)

`dragonFlies` - Port of the option from Apathetic Mobs, replaces some flying-attack related dragon phases with an instruction to go perch, dunno the exact gameplay effect.

`dragonSits` - Same, with some different phases tho (replaces perch-attack phases with an instruction to leave the perch)

`dragonDamage` - If false, dragon doesn't deal contact damage.

`dragonKnockback` - If false, dragon doesn't knock-back entities in her hitbox. Also seems to control damage when perched, uses the same method in the code. Separating these is a more complicated mixin that I can't test, so someone will have to write for me.

# Wither

## No wither

Disables the Wither boss fight.

* Building the Wither multiblock will simply spawn a nether star item.

Notes:

* Existing Withers are *not* deleted!
* The Wither is *created* but never *spawned*.
* The drops are created by simulating an (anvil) kill. You can use a loot table to further customize drops.
* You will get the advancement for killing the Wither.

## Wither controls

Several options to control the Wither boss:

* Whether the Wither is allowed to target players.
* Whether the Wither is allowed to target mobs.
* Whether the Wither is allowed to fire black skulls at the things it targets.
* Whether the Wither is allowed to fire blue skulls (on Normal and Hard difficulty.)
* Whether the Wither breaks blocks around itself after it gets damaged.