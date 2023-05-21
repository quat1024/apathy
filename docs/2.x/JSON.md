# `apathy-mobs.json` / `apathy/mobs.json`

The mobs json file is *not created by default*, but will be loaded if it exists. **On Forge**, the path to this file is `config/apathy-mobs.json`. On **other platforms**, the path is `config/apathy/mobs.json`. 

If mobs.cfg's `ruleOrder` option does not contain `json`, or a rule ordered before it returns `allow` or `deny`, the rules in the JSON file will not be examined.

See the bottom of this page for notes about versions older than v2.5 (which was a pretty significant rewrite of Apathy).

## Dumping

`general.cfg` has options for dumping the built-in rule as a JSON file if you would like to see an example. If you turn that on, the file will be dumped to `apathy-dumps/builtin-rule.json`.

(If you'd like to use the dumped rule as a starting point to create your own `mobs.json`, remember to remove the `evaluate_json_file` rule!!!)

# Json Format

An example. This rule encodes the following process:

* If the difficulty is set to Easy, return "deny".
* If not, and the attacker is tagged with `apathy:bosses`, return "allow".
* If not, return "deny".

```json
{
	"type": "chain",
	"rules": [
		{
			"type": "predicated",
			"predicate": {
				"type": "difficulty_is",
				"difficulties": ["easy"]
			},
			"if_true": "deny"
		},
		{
			"type": "predicated",
			"predicate": {
				"type": "attacker_is_boss"
			},
			"if_true": "allow"
		},
		{
			"type": "always",
			"value": "deny"
		}
	]
}
```

A *rule* is a JSON object consisting of at least one property: `type`. The rest of the properties in the object are different, depending on the `type`.

Here are the possible rule `type`s.

## `always`
Arguments:
* `value`: Can be one of `"allow"`, `"deny"`, or `"pass"`.

The rule always allows, denies, or passes.

### Example

```json
{
	"type": "always",
	"value": "allow"
}
```

This rule always returns "allow".

## `chain`
Arguments:
* `rules`: An array of other rules.

The rules are checked top-to-bottom. The first rule that doesn't evaluate to `"pass"` is returned. If all rules evaluated to `"pass"`, then `pass` is returned. 

### Example

First, the `predicated` rule is evaluated. If it returns `pass`, the `always` rule is evaluated.

```json
{
	"type": "chain",
	"rules": [
		{
			"type": "predicated",
			"...": "..."
		},
		{
			"type": "always",
			"value": "allow"
		}
	]
}
```

## `predicated`
Arguments:
* `predicate`: A predicate to check against.
* `if_true`: Optional. Can be one of `"allow"`, `deny"`, or `"pass"`. Defaults to `"pass"` if you do not specify it.
* `if_false`: Optional. Can be one of `"allow`, `"deny"`, or `"pass"`. Defaults to `"pass"` if you do not specify it.

The predicate is tested. If it is true, the rule evaluates to `if_true`. If it is false, the rule evaluates to `if_false`.

A list of predicates is available in the next section.

### Example

If the `revenge_timer` predicate passes, the rule evaluates to `allow`. If not, the rule evaluates to `pass`.

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "pass",
	"predicate": {
		"type": "revenge_timer",
		"timeout": 60
	}
}
```

### `allow_if` and `deny_if` (deprecated 2.5)

`allow_if` is a synonym for `predicated` with `if_true` pre-set to `allow` and `if_false` pre-set to `pass`. `deny_if` is the same, but `if_true` is pre-set to `deny`.

These are deprecated because it's just a more confusing way of accessing `predicated` - it does the same thing.

## `if` **(new in 2.6)**
Arguments:
* `predicate`: A predicate to check against.
* `if_true`: Optional. A rule. Defaults to `{"type": "always", "value": "pass"}` if you do not specify it.
* `if_false`: Optional. A rule. Defaults to `{"type": "always", "value": "pass"}` if you do not specify it.

The predicate is tested. If it is true, the `if_true` rule is evaluated. If it is false, the `if_false` rule is evaluated instead.

### Example

If the `revenge_timer` predicate passes (more detals on predicates in the next section), the rule evaluates to the result of the `chain` rule. If not, the rule evaluates to the result of the `predicated` rule.

```json
{
	"type": "if",
	"if_true": {
		"type": "chain",
		"rules": [
			{
				"type": "..."
			}
		]
	},
	"if_false": {
		"type": "predicated",
		"predicate": {
			"type": "..."
		}
	},
	"predicate": {
		"type": "revenge_timer",
		"timeout": 60
	}
}
```

## `debug`
Arguments:
* `message`: Any string you want.
* `rule`: A rule to wrap in debug output.

Whenever the rule is evaluated: the message, and the evaluation of the rule, are printed to the server log.

### Example

When this rule is evaluated:

```json
{
	"type": "debug",
	"message": "Evaluating my rule!",
	"rule": {
		"type": "always",
		"value": "allow"
	}
}
```

the console will print:

```console
rule: Evaluating my rule!
returned: allow
```

and the rule will evaluate to `allow`.

## `difficulty_case`
Arguments:
* `cases`: A JSON object.

The object may have the following fields:

* `peaceful`: A rule to evaluate on Peaceful difficulty (although this is kind of moot lol)
* `easy`: A rule to evaluate on Easy difficulty.
* `normal`: A rule to evaluate on Normal difficulty.
* `hard`: A rule to evaluate on Hard difficulty.

A different rule is tested depending on the current world difficulty.

If the current world difficulty is not included in `cases`, the rule evaluates to `pass`.

### Example

This rule evaluates to `deny` on Easy, `pass` on Normal/Peaceful, and `allow` on Hard.

```json
{
	"rule": "difficulty_case",
	"cases": {
		"easy": {
			"rule": "always",
			"value": "deny"
		},
		"hard": {
			"rule": "always",
			"value": "allow"
		}
	}
}
```

## `evaluate_json_file`
Arguments: None.

This rule evaluates the contents of the `mobs.json` file as a rule. Needless to say, if you include this in the `mobs.json` file itself, the game will go into an infinite loop!

It will show up in dumps when you dump the builtin rule, so, watch out for that.

# Predicates
Much like a rule, a predicate is a JSON object of at least one field - `type` - and the rest of the fields depend on the `type`. These can be passed to the `predicated`/`allow_if`/`deny_if` rules. And it's where the meat of the mod is.

## `always`
Arguments:
* `value`, can be `true` or `false` (no double quotes).

Always evaulates to true or false.

### Example

This rule always evaluates to "allow", because the predicate always evaluates to "true".

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "deny",
	"predicate": {
		"type": "always",
		"value": true
	}
}
```

## `attacker_tagged_with`
Arguments:
* `tags`, an array of strings such as `["minecraft:raiders"]`.

The predicate returns `true` if the attacker has one of these tags.

### Example

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "deny",
	"predicate": {
		"type": "attacker_tagged_with",
		"tags": [
			"minecraft:raiders",
			"somemod:sometag"
		]
	}
}
```

## `attacker_is_boss`
Arguments: None.

Synonym for `attacker_tagged_with` with the tag `apathy:bosses`. This tag includes the Ender Dragon, Wither, Warden, and the contents of the `c:bosses` and `forge:bosses` tags if they exist.

## `attacker_is`
Arguments: `mobs`, an array of mob IDs such as `["minecraft:creeper"]`.

The predicate returns `true` if the attacker is one of these mobs.

### Example

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "deny",
	"predicate": {
		"type": "attacker_is",
		"mobs": [
			"minecraft:creeper",
			"minecraft:spider"
		]
	}
}
```

## `in_player_set`
Arguments: `player_sets`, an array of strings such as `["my-cool-set"]`.

The predicate returns `true` if the defending player is part of one of these player sets. Player sets can be managed with the `/apathy set` and `/apathy set-admin` commands.

### Example

This rule honors a "no-mobs" player set. If the player is in that set, all mobs will be denied from attacking.

```json
{
	"type": "predicated",
	"if_true": "deny",
	"if_false": "pass",
	"predicate": {
		"type": "in_player_set",
		"player_sets": [
			"no-mobs"
		]
	}
}
```

## `revenge_timer`
Arguments: `timeout`, a number like `60`.

The predicate returns `true` while the mob was last attacked within this many ticks (1/20ths of a second).

For example, if `timeout` is `60`, the predicate will return `true` if the mob was attacked within the last 3 seconds. After the time expires, the predicate will start returning `false`.

### Example

This rule allows mobs to retaliate for 20 seconds after being attacked.

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "pass",
	"predicate": {
		"type": "revenge_timer",
		"timeout": 400
	}
}
```

## `difficulty_is`
Arguments: `difficulties`, an array of difficulty strings like `["easy", "normal"]`.

The predicate returns `true` if the current world difficulty appears in the array.

### Example

This rule acts a litlte bit like `difficulty_case`. Note that this example makes use of the `if` rule, which is new in version 2.6.

```json
{
	"type": "if",
	"if_true": {
		"type": "chain",
		"rules": [...]
	},
	"if_false": {
		"type": "predicated",
		"predicate": {...}
	},
	"predicate": {
		"type": "difficulty_is",
		"difficulties": [
			"easy",
			"normal"
		]
	}
}
```

## `score`
Arguments:
* `objective`, any string
* `who`, either `"attacker"` (the attacking mob) or `"defender"` (the defending player) (if not specified, defaults to `"defender"` in 1.18.2)
* `thresholdMode`, either `"at_least"`, `"at_most"`, or `"equal"`
* `threshold`, any integer

The predicate tests a scoreboard value of either the attacker or the defending player (choose with `who`). It returns `true` if the test passes, and `false` if it does not. A `"thresholdMode"` of `"at_least"` performs a "greater than or equal to" test. `"at_most"` performs a "less than or equal to" test.

If the scoreboard objective does not exist, this predicate will always return `false`.

### Example

This rule returns `"allow"` when the defending player has >= 10 points on the scoreboard objective "fruit".

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "pass",
	"predicate": {
		"type": "score",
		"objective": "fruit",
		"who": "defender",
		"thresholdMode": "at_least",
		"threshold": 10
	}
}
```

## `team` **(NEW in 2.6)**
Arguments:
* `team`, a string

`true` if the defending player is on the named scoreboard team. `false` if the player is not on the team, or if the team does not exist.

### Example

```json
{
	"type": "predicated",
	"if_true": "deny",
	"if_false": "pass",
	"predicate": {
		"type": "team",
		"team": "nomobs"
	}
}
```

## `random` **(NEW in 2.6)**
Arguments:
* `chance`, a double somewhere in the range 0 to 1

Returns "true" on approximately chance% of attacking mobs. For example, if `"chance": 0.7`, about 70% of mobs will pass this test.

Randomness is seeded from the mob's UUID, so the same mob will always pass or fail the test.

### Example

This makes use of the `all` predicate, documented later, to make half of all zombies passive towards the player.

```json
{
	"type": "predicated",
	"if_true": "deny",
	"if_false": "pass",
	"predicate": {
		"type": "all",
		"predicates": [
			{
				"type": "attacker_is",
				"mobs": [ "minecraft:zombie" ]
			},
			{
				"type": "random",
				"chance": 0.5
			}
		]
	}
}
```

## `advancements`
Arguments:
* `advancements`, an array of strings corresponding to advancement IDs, like `["minecraft:story/enter_the_end", "minecraft:story/enter_the_nether"]`

The predicate returns `true` if the defending player has at least one of the mentioned advancements, and `false` if they do not have any.

### Example

This rule makes mobs passive until the player enters the End for the first time.

```json
{
	"type": "predicated",
	"if_true": "allow",
	"if_false": "deny",
	"predicate": {
		"type": "advancements",
		"advancements": [
			"minecraft:story/enter_the_end"
		]
	}
}
```

## `,location`
Arguments:
* `predicate`, a vanilla `LocationPredicate`.
* `who`, either `"attacker"`, `"defender"`, or `"attacker_spawn_location"` (the default)
* `uniqueId`, any String you want. Required for `attacker_spawn_location`.

This predicate does the following:

* picks a *location* dependent on the value of `who`:
  * `attacker`: the attacking mob's location
  * `defender`: the defending player's location
  * `attacker_spawn_location`: the first location where the attacking mob was noticed
* returns `true` if the location is loaded and passes the `LocationPredicate`.

LocationPredicates can test x/y/z ranges, biomes, dimensions, features, light levels, and a couple other oddities (like whether an entity is in campfire smoke). Please check [the minecraft wiki](https://minecraft.fandom.com/wiki/Predicate), Ctrl-F the page for "location_check", and open the box labeled "Tags common to all locations". The `offsetX`/`offsetY`/`offsetZ` keys are also supported.

`uniqueId` is required because the result of `"who": "attacker_spawn_location"` predicates is tested only one time and cached on the entity. This helps on performance, and fixes issues relating to the attacker's spawn location being unloaded.

### Example

This rule returns `"deny"` if the defending player is standing in a Stronghold structure, and `"pass"` otherwise.

```json
{
	"type": "predicated",
	"if_true": "deny",
	"if_false": "pass",
	"predicate": {
		"type": "location",
		"who": "defender",
		"predicate": {
			"feature": "stronghold"
		}
	}
}
```

## `all` and `any`
Arguments: `predicates`, an array of more predicates.

`all` returns `true` only when *all of* the component predicates return true.

`any` returns `true` when *at least one of* the component predicates returns true.

### Example

```json
{
	"type": "predicated",
	"if_true": "deny",
	"if_false": "pass",
	"predicate": {
		"type": "and",
		"predicates": [
			{
				"type": "attacker_is",
				"mobs": [ "minecraft:zombie" ]
			},
			{
				"type": "random",
				"chance": 0.5
			}
		]
	}
}
```

## `not`
Arguments: `predicate`, a single predicate.

Returns `true` whenever its component predicate returns `false`, and vice versa.

### Example

This rule returns `"allow"` when the difficulty is *not* Easy.

```json
{
	"rule": "predicated",
	"if_true": "allow",
	"if_false": "deny",
	"predicate": {
		"type": "not",
		"predicate": {
			"type": "difficulty_is",
			"difficulties": [
				"easy"
			]
		}
	}
}
```

# Gotcha

Rules and predicates happen to be both specified in fields named "`type`", but they cannot be interchanged. Rules evaluate to "allow", "deny", or "pass", but predicates are a true/false thing. There's several possible mappings from true/false to allow/deny/pass - you have to use the `predicated` rule and set `if_true` and `if_false` to tell the mod which mapping you want.

# Version differences to be aware of

||pre-2.5|2.5 and later|
|---|---|---|
|File location|`config/apathy/mobs.json` always|`config/apathy-mobs.json` (on Forge)<br>`config/apathy/mobs.json` (on Fabric)|
|Dump directory|`config/apathy/dumps/`|`apathy-dumps/`|
|Rule and predicate IDs|**Must be prefixed with `apathy:`**|The `apathy:` prefix is not required|
|Dumps look weird and confusing?|Yes, DataFixerUpper quirk|No, I rewrote all the json code|
|`advancements`, `location`, and `score` rules|Not available|Available|