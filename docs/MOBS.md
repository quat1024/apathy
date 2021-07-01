# `mobs.cfg`

The mob config file lives at `/config/apathy/mobs.cfg`.

It allows you to place these predefined rules in any order and configure their properties.

* *JSON rule*: If `mobs.json` exists, its rule is evaluated.
* *difficulty rule*: The attacker can target depending on the world difficulty.
* *boss rule*: The attacker can target if it is a "boss" (i.e. in the `apathy:bosses` tag.)
* *mob set rule*: The attacker can target depending on whether its ID appears in a list of mob IDs.
* *tag set rule*: The attacker can target depending on whether one of its tags appears in a list of mob tags.
* *player set rule*: The attacker can target players depending on their inclusion in a player set.
* *revenge rule*: The attacker can target if they were recently provoked.
* *fallthrough rule*: If *all* of the previous rules returned "pass", this determines the mob's behavior.

Rules are evaluated in a configurable order, and the first one that doesn't `pass` is the final result.

Unfortunately the config file only allows you to place one copy of each rule. This is fine for most typical use-cases (it's still a superset of what Apathetic Mobs can do), but if you need even *more* control over rules - such as AND/OR operations, more copies of each rule, or applying radically different rules for each game difficulty/player set - please see the documentation for `mobs.json`.

# Nuclear Option

If this is set to `true`, all of the rule options in the config file are ignored, and mobs are never allowed to attack any players.

(I added this option because the config file got pretty long, lol)

# Rules

## Rule Order

Rules are evaluated left-to-right in this order. The default order is in the list above, and behaves the same as the mod "Apathetic Mobs."

Reordering this config option can resolve conflicts between rules in exactly the way you want. Consider:

* `difficultySet` is `easy`
* `difficultySetIncluded` is `deny`
* `difficultySetExcluded` is `pass`
* `boss` is `allow`

Here, we say "no matter what, mobs are not allowed to attack on Easy", and "no matter what, bosses are always allowed to attack". We have a conflict - what happens to bosses on Easy?

* If `ruleOrder` has "boss" before "difficulty", the boss rule is evaluated first, and the boss is allowed to attack.
* If `ruleOrder` has "difficulty" before "boss", the difficulty rule is evaluated first, and the boss is denied from attacking.

Neither of these is obviously the "right answer", so you get to pick.

Quick note: There's a fairly aggressive rule optimizer included in the mod. The rules engine only keeps checking rules if the previous one returned `pass`, if a rule returns the same thing whether it succeeds or fails it's never executed at all, and a few other things. If you're not using a rule, there's no need to remove it from the rule order.

## JSON

This rule does not have any options in the config file, but its ordering relative to other rules may be specified in `ruleOrder`.

## Difficulty

* `difficultySet` lists off some game difficulties (`easy`, `normal`, `hard`, technically `peaceful` as well).
* `difficultySetIncluded` describes what happens to mobs when the current difficulty is *included* in the set.
* `difficultySetExcluded` describes what happens to mobs when the current difficulty is *not* included in the set.

You can set the `...Included` or `...Excluded` settings to `allow`, `deny`, or `pass`.

* `allow` and `deny` allow the mob to attack, and deny the mob from attacking, respectively.
* `pass` means "it doesn't matter", and the next rule in `ruleOrder` will be evaluated instead.

## Boss

* `boss` describes what happens when a boss tries to attack. Can be `allow`, `deny`, or `pass`.

This rule always returns `pass` for mobs that aren't bosses.

## Mob Set

* `mobSet` is a comma-separated list of mobs, like `minecraft:creeper, minecraft:spider`.
* `mobSetIncluded` describes what happens when a mob *included* in the set tries to attack.
* `mobSetExcluded` describes what happens when a mob *not* included in the set tries to attack.

## Tag Set

* `tagSet` is a comma-separated list of mob tags, like `minecraft:raiders, minecraft:skeletons`.
* `tagSetIncluded` describes what happens when a mob with *any* of its tags appearing in the set tries to attack.
* `tagSetExcluded` describes what happens when a mob with *none* of its tags appearing in the set tries to attack.

## Player Set

* `playerSetName` is the name of a player set. Can be anything you want - exposed in the `/apathy set join` command.
* `playerSetSelfSelect`:
	* If `true`, players can add themself to the set using `/apathy set join`.
	* If `false`, server admins can add people to the set using `/apathy set-admin join`.
* `playerSetIncluded` describes what happens when a mob tries to attack someone *included* in the player set.
* `playerSetExcluded` describes what happens when a mob tries to attack someone *not* included in the player set.

With this rule, you can emulate the "player whitelist" option from Apathetic Mobs, or emulate it but inverted (so PvE is opt-in instead of opt-out).

Yes, you can only define one player-set with the config file, i'm sorry. (You can refer to more sets using the json system.)

The `/apathy set-admin delete` and `edit` commands cannot modify the player set named in the config file.

## Revenge

* `revengeTimer` - how many ticks (20ths of a second) that mobs that *were* attacked are allowed to attack *back.* `-1` disables the mechanic.

This controls a rule that returns `allow` when the mob was attacked within this timeframe, and `pass` otherwise.

Few notes:

* There is no "perma-revenge" option like Apathetic Mobs. Instead, the maximum value of the revenge timer is several billion years.
* Revenge timers do not cause *any* strain on the server whatsoever - they don't actively "tick down" or anything, if you were worried about that.
* It's indiscriminate. Mobs don't attempt to fight back against *specifically* the person who attacked them.

## Fallthrough

If every single rule returned `pass`, `fallthrough` controls whether the mob will be allowed to attack. `allow` means they can, `deny` means they can't.