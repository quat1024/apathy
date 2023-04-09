# Concepts

## Config Files

Apathy contains 4 config files:

* `config/apathy-general.toml` or `config/apathy/general.cfg`: General options.
* `config/apathy-boss.toml` or `config/apathy/boss.cfg`: Options for controlling the behavior of the Ender Dragon and Wither.
* `config/apathy-mobs.toml` or `config/apathy/mobs.cfg`: Describes the main rule of the mod - whether mob X is allowed to attack player Y.
* `config/apathy/mobs.json`: An auxillary rule. It's more powerful than the `mobs` system, but more complicated to use.

These are documented in the files `GENERAL.md`, `BOSS.md`, `MOBS.md`, and `JSON.md` respectively.

**TOML files are used on Forge as of Apathy version 2.5 only**, everything else uses the `cfg` files (in their own subdirectory).

### When are config files loaded and parsed?

* ~~Once at startup~~
* Whenever datapacks are loaded, which happens:
    * whenever you join a world in singleplayer
    * whenever you run `/reload`
* When an admin runs the command `/apathy reload`

**most late 2.x versions:** Config files are not parsed on startup anymore. This was due to a silly bug; if you had anything modded in your config file and the other mod hadn't loaded yet, the config file would break. I hope to fix this in 2.5 or 3.

## Rules

This mod is based around a list of *rules*. Whenever a mob (the "attacker") thinks about targeting a player (the "defender"), it first checks the list of rules to see if it is allowed to do so.

A rule either says "yes, you are **allowed** to attack", "no, you are **denied** from attacking", or **pass**es to the next rule in the sequence.

## Player sets

A player set is... well, it's a set of players. Each player may either be in the set, or not in the set.

The *player set rule* determines what happens when players are in a particular set.

You can choose to make player sets *self-select*, where players can add themselves to the set using `/apathy set join <set name>`. Server operators can add anyone to the set using `/apathy set-admin join <player selector> <set name>`.