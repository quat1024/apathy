# `mobs.json`

The rule json file lives at `/config/apathy/mobs.json`. It is *not created by default*, but will be loaded if it exists.

If mobs.cfg's `ruleOrder` option does not contain `json`, or a rule ordered before it returns `allow` or `deny`, the rules in the JSON file will not be examined.

## Dumping

`general.cfg` has options for dumping the built-in rule as a JSON file (in `/config/apathy/dumps/builtin-rule.json` and `builtin-rule-opt.json`), if you would like to see an example.  Note that the auto-dumper puts the values in kind of a weird order - like it puts `type` at the bottom and stuff - sorry about that! I don't know why that happens.

(If you'd like to use the dumped rule as a starting point to create your own `mobs.json`, remember to remove the `apathy:evaluate_json_file` rule!)

# Json Format

Let's start things off with an illustrative example:

```json
{
	"type": "apathy:chain",
	"rules": [
		{
			"type": "apathy:predicated",
			"predicate": {
				"type": "apathy:difficulty_is",
				"difficulties": ["easy"]
			},
			"if_true": "deny"
		},
		{
			"type": "apathy:predicated",
			"predicate": {
				"type": "apathy:attacker_is_boss"
			},
			"if_true": "allow"
		},
		{
			"type": "apathy:always",
			"value": "allow"
		}
	]
}
```

A *rule* is a JSON object consisting of at least one property: `type`. The rest of the properties in the object are different, depending on the `type`.

Here are the possible rule `type`s.

## `apathy:always`
Arguments:
* `value`: Can be one of `"allow"`, `"deny"`, or `"pass"`.

The rule always allows, denies, or passes.

## `apathy:chain`
Arguments:
* `rules`: An array of other rules.

The rules are checked top-to-bottom. The first rule that doesn't evaluate to `"pass"` is used.

## `apathy:predicated`
Arguments:
* `predicate`: A predicate to check against. (More on these below - there's a bunch.)
* `if_true`: Optional. Can be one of `"allow"`, `deny"`, or `"pass"`. Defaults to `"pass"` if you do not specify it.
* `if_false`: Optional. Can be one of `"allow`, `"deny"`, or `"pass"`. Defaults to `"pass"` if you do not specify it.

The predicate is tested. If it is true, the rule evaluates to `if_true` and same for `if_false`.

## `apathy:allow_if`
Arguments:
* `predicate`.

Synonym for `predicated`, with `if_true` set to `"allow"`, and `if_false` set to `"pass"`.

## `apathy:deny_if`
Arguments:
* `predicate`.

Synonym for `predicated`, with `if_true` set to `"deny"`, and `if_false` set to `"pass"`.

## `apathy:debug`
Arguments:
* `message`: Any string you want.
* `rule`: A rule to wrap in debug output.

Whenever the rule is tested: the message, and the evaluation of the rule, are printed to the server log.

## `apathy:difficulty_case`
Arguments:
* `cases`: A JSON object.

The object may have the following fields:

* `peaceful`: A rule to evaluate on Peaceful difficulty (although this is kind of moot lol)
* `easy`: A rule to evaluate on Easy difficulty.
* `normal`: A rule to evaluate on Normal difficulty.
* `hard`: A rule to evaluate on Hard difficulty.

A different rule is tested depending on the current world difficulty.

## `apathy:evaluate_json_file`
Arguments: None.

This rule evaluates the contents of the `mobs.json` file as a rule. Needless to say, if you include this in the `mobs.json` file itself, the game will go into an infinite loop!

It will show up in dumps when you dump the builtin rule, so, watch out for that.

# Predicates
Much like a rule, a predicate is a JSON object of at least one field - `type` - and the rest of the fields depend on the `type`. These can be passed to the `predicated`/`allow_if`/`deny_if` rules. And it's where the meat of the mod is.

## `apathy:always`
Arguments:
* `value`, can be `true` or `false` (no double quotes).

Always evaulates to true or false.

## `apathy:attacker_tagged_with`
Arguments:
* `tags`, an array of strings such as `["minecraft:raiders"]`.

The predicate returns `true` if the attacker has one of these tags.

## `apathy:attacker_is_boss`
Arguments: None.

Synonym for `attacker_tagged_with` with the tag `apathy:bosses`.

## `apathy:attacker_is`
Arguments: `mobs`, an array of mob IDs such as `["minecraft:creeper"]`.

The predicate returns `true` if the attacker is one of these mobs.

## `apathy:in_player_set`
Arguments: `player_sets`, an array of strings such as `["my-cool-set"]`.

The predicate returns `true` if the defending player is part of one of these player sets.

## `apathy:revenge_timer`
Arguments: `timeout`, a number like `60`.

The predicate returns `true` while the mob was last attacked within this many ticks (1/20ths of a second).

For example, if `timeout` is `60`, the predicate will return `true` if the mob was attacked within the last 3 seconds. After the time expires, the predicate will start returning `false`.

## `apathy:difficulty_is`
Arguments: `difficulties`, an array of difficulty strings like `["easy", "normal"]`.

The predicate returns `true` if the current world difficulty appears in the array.

## `apathy:score` (✨ NEW in 1.18.1 ✨)
Arguments:
* `objective`, any string
* `who`, either `"attacker"` (the attacking mob) or `"defender"` (the defending player)
* `thresholdMode`, either `"at_least"`, `"at_most"`, or `"equal"`
* `threshold`, any integer

The predicate tests a scoreboard value of either the attacker or the defending player (choose with `who`). It returns `true` if the test passes, and `false` if it does not.

For example, this predicate:

```json
{
	"type": "apathy:score",
	"objective": "fruit",
	"who": "defender",
	"thresholdMode": "at_least",
	"threshold": 10
}
```

will return `true` when the defending player has >=10 points on the scoreboard objective named "fruit". If the scoreboard objective does not exist, this predicate will always return `false`.

## `apathy:advancements` (✨ NEW in 1.18.2 ✨)
Arguments:
* `advancements`, an array of strings corresponding to advancement IDs, like `["minecraft:story/ender_the_end", "minecraft:story/ender_the_nether"]`

The predicate returns `true` if the defending player has at least one of the mentioned advancements, and `false` if they do not have any.

## `apathy:all` and `apathy:any`
Arguments: `predicates`, an array of more predicates.

`all` returns `true` only when all of the component predicates return true.

`any` returns `true` when at least one of the component predicates returns true.

## `apathy:not`
Arguments: `predicate`, a single predicate.

Returns `true` whenever its component predicate returns `false`, and vice versa.

# Gotchas

Note that this does *not* work (although I wish it did):

```json
{
	"type": "apathy:allow_if",
	"predicate": "apathy:attacker_is_boss"
}
```

You have to do it like this:

```json
{
	"type": "apathy:allow_if",
	"predicate": {
		"type": "apathy:attacker_is_boss"	
	}
}
```

Note that rules and predicates are both specified with the parameter `type` and they cannot be interchanged. A rule can be lifted into a predicate with `allow_if`, `deny_if`, or `predicated`.