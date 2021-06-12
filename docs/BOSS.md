# Boss config

# Dragon

Sorry there aren't too many options to control the Ender Dragon boss. I added the "no dragon" setting for myself, because the fight is disturbing to me, so I can't test any more granular settings about the fight itself. Pull-requests welcome!

## No dragon

Disables the Ender Dragon fight.

* The Ender Dragon will not spawn when you visit the End.
* The exit End portal will already be open, and have the Egg on top.
* Placing four end crystals on the End portal generates a new End Gateway.

Notes:

* Existing Ender Dragons will be deleted.
* You will get the "Free the End" advancement.
* Placing four end crystals grants the advancement for resummoning the dragon.

# Wither

## No wither

Disables the Wither boss fight.

* Building the Wither multiblock will simply spawn a nether star item.

Notes:

* Existing Withers are *not* deleted. You can still summon Withers using commands.
* The Wither is *created* but never *spawned*.
* The drops are created by simulating an (anvil) kill. You can use a loot table to further customize drops.
* You will get the advancement for killing the Wither.

## Wither controls

Several options to control the Wither boss:

* Whether the Wither is allowed to target players.
* Whether the Wither is allowed to target mobs.
* Whether the Wither is allowed to fire black skulls at the things it targets.
* Whether the Wither is allowed to fire blue skulls on Normal and Hard difficulty.
* Whether the Wither breaks blocks around itself after it gets damaged.