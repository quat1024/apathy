# `boss.cfg`

This file contains controls for various boss-fight sequences in Minecraft. You can disable or modify them.

# Dragon

## No dragon

Disables the Ender Dragon fight.

* When you visit the End for the first time:
	* The Ender Dragon will not spawn when you visit the End.
	* You will get the "Free the End" advancement.
	* The exit End portal will already be open, complete with an Egg on top.
* If you place four end crystals on the End portal:
	* A new End Gateway will be generated.
	* You will get the "The End... Again..." advancement.
* Existing Ender Dragons *will* be deleted.

## Dragon control

All of these are untested, unfortunately (I added the "No dragon" option for myself, because I find the fight disturbing). They're relatively simple and should work okay. Let me know if there's issues and I'll try and find someone to fix them for me.

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