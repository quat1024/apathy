# Config File

The mob config file lives at `/config/apathy/mobs.cfg`.

## When is the config file parsed?

* Once at startup
* Whenever datapacks are loaded, which happens:
	* whenever you join a world in singleplayer
	* whenever you run `/reload`
* When an admin runs the command `/apathy reload`

## Sections

### Nuclear Option

If this is set to `true`, all of the (rule) options in the config file are ignored, and mobs are never allowed to attack any players.

(I added this option because the config file got pretty long.)

### Rules

The next couple sections in the config file deal with built-in rules.

#### Rule Order

Rules are evaluated left-to-right in this order. The default order behaves the same as the mod "Apathetic Mobs."

Reordering this config option can resolve conflicts between rules in exactly the way you want.

Consider:

* `difficultySet` is `easy`
* `difficultySetIncluded` is `deny`
* `difficultySetExcluded` is `pass`
* `boss` is `allow`

Here, we say "no matter what, mobs are not allowed to attack on Easy", and "no matter what, bosses are always allowed to attack". We have a conflict - what happens to bosses on Easy?

* If `ruleOrder` has "boss" before "difficulty", the boss rule is evaluated first, and the boss is allowed to attack.
* If `ruleOrder` has "difficulty" before "boss", the difficulty rule is evaluated first, and the boss is denied from attacking.

Quick note:

* There's a fairly aggressive rule optimizer included in the mod. The rules engine only keeps checking rules if the previous one returned `pass`, and if a rule returns the same thing when it succeeds and when it fails, it's never executed at all. If you're not using a rule, there's no need to remove it from the server.

#### Difficulty

* `difficultySet` lists off some game difficulties (`easy`, `normal`, `hard`, technically `peaceful` as well).
* `difficultySetIncluded` describes what happens to mobs when the current difficulty is *included* in the set.
* `difficultySetExcluded` describes what happens to mobs when the current difficulty is *not* included in the set.

You can set the `...Included` or `...Excluded` settings to `allow`, `deny`, or `pass`.

* `allow` and `deny` allow the mob to attack, and deny the mob from attacking, respectively.
* `pass` means "it doesn't matter", and the next rule in `ruleOrder` will be evaluated instead.

#### Boss

* `boss` describes what happens when a boss tries to attack. Can be `allow`, `deny`, or `pass`.

This rule always returns `pass` for mobs that aren't bosses.

#### Mob Set

* `mobSet` is a comma-separated list of mobs, like `minecraft:creeper, minecraft:spider`.
* `mobSetIncluded` describes what happens when a mob *included* in the set tries to attack.
* `mobSetExcluded` describes what happens when a mob *not* included in the set tries to attack.

#### Player Set

* `playerSetName` is the name of a player set. Can be anything you want - exposed in the `/apathy set join` command.
* `playerSetSelfSelect`:
	* If `true`, players can add themself to the set using `/apathy set join`.
	* If `false`, server admins can add people to the set using `/apathy set-admin join`.
* `playerSetIncluded` describes what happens when a mob tries to attack someone *included* in the player set.
* `playerSetExcluded` describes what happens when a mob tries to attack someone *not* included in the player set.

With this rule, you can emulate the "player whitelist" option from Apathetic Mobs, or emulate it but inverted (so PvE is opt-in instead of opt-out).

Yes, you can only define one player-set with the config file. I'm sorry. (Scripting lets you add more.)

#### Revenge

* `revengeTimer` - how many ticks (20ths of a second) that mobs that were attacked are allowed to attack back. `-1` disables the mechanic.

This controls a rule that returns `allow` when the mob was attacked within this timeframe, and `pass` otherwise.

Few notes:

* There is no "perma-revenge" option like Apathetic Mobs; it's unneeded, since the maximum value of the revenge timer is several billion years.
* Revenge timers do not cause *any* strain on the server whatsoever. They don't actively "tick down" or anything, if you were worried about that.
* It's indiscriminate - mobs don't attempt to fight back against specifically the person who attacked them.

#### Fallthrough

If every single rule returned `pass`, `fallthrough` controls whether the mob will be allowed to attack. `allow` means they can, `deny` means they can't.

### Clojure

Enable this option to enable the Clojure scripting subsystem. This lets you define rules in the Clojure language. This lets you do things like, define more player-sets, or define one set of rules for Easy and another set for Hard.

More on that in `CLOJURE.md`.

### Optimization

By default, mobs that are currently attacking a player do not check *every* tick if it's still okay to do so. There's an option to set the interval they'll use instead.

It's staggered a bit, based off the sequential entity ID, so they don't all check at once and cause a big lagspike on the server.