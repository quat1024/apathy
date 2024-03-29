## `CONCEPTS.md`

Details how the mod works. Reading this is helpful if you want to write a detailed config file.

## `MOBS.md`

Documents the config options in `config/apathy-mobs.toml` or `config/apathy/mobs.cfg`. This file configures the main "is X allowed to attack Y" rule.

## `GENERAL.md`

Documents the config options in `config/apathy-general.toml` or `config/apathy/general.cfg`, which control details of how the mod works and debugging options.

## `BOSS.md`

Documents the config options in `config/apathy-boss.toml` or `config/apathy/boss.cfg`, which control parameters of the Ender Dragon and Wither boss sequences.

## `JSON.md` (✨ contains new content in 1.18 ✨)

Describes the format of `config/apathy/mobs.json`, an optional file for creating more advanced rules than `mobs.cfg` allows for.

# quat, this is too much documentation. How do I ______?

## ...disable all mob aggression?

I added an option specifically for people who don't want to deal with this mess of a config situation. :)

Open `mobs.cfg` and set `nuclearOption` to `true`. It's one of the first config options.

## ...make mobs only attack players on Hard?

Mess with the options in the `mobs.cfg` "difficulty rule" category.

* Set `difficultySet` to `hard`.
* Set `difficultySetIncluded` to `allow`. When the current difficulty *is* Hard, mobs are *allowed* to attack.
* Set `difficultySetExcluded` to `deny`. When the current difficulty is *not* Hard, mobs are *denied* from attacking.

Equivalently,

* Set `difficultySet` to `easy, normal`.
* Set `difficultySetIncluded` to `deny`.
* Set `difficultySetExcluded` to `allow`.

## ...only allow *bosses* to attack players?

* In `mobs.cfg`, set `boss` to `allow`, and using the other rules, deny other mobs from attacking.

(By the way, this is when we get into the weeds with "rule priorities" and stuff. If you want boss-fights to *take priority over* difficulty, e.g. "No mobs can attack on Easy, except for bosses", change `ruleOrder` so that "boss" comes before "difficulty".)

## ...emulate the "player whitelist" option from Apathetic Mobs?

I have a "player sets" option, which is like a generalized version of that mod's "player whitelist".

* Set `playerSetName` to something like `no-mobs`, or whatever you want really.
* Set `playerSetIncluded` to `deny`.
* Set `playerSetExcluded` to `allow`.

Now, mobs are allowed to attack anyone *not* in the player set.

Players can add themselves to the player set using `/apathy set join no-mobs`. If you don't want that, and only want operators to manage the player set, set `playerSetSelfSelect` to `false`.

(Rule priority note: Put this before "boss" if you want players in `no-mobs` to opt-out of boss attacks, too.)

## ...create aggressive sheep and cows?

Out of scope for this mod. Preventing mob aggression from occuring is a fundamentally different problem from adding mob aggression where there wasn't any before - this mod does not add or remove AI tasks at all, it only prevents calling `setTarget` in a fairly low-level way.

You'll have to use another mod for that. (If you do, it's likely that you'll be able to use Apathy to further refine that behavior.)

# I still don't get it

That's completely understandable - I did overengineer this mod quite a bit...

Open an issue or hit me up on [discord](https://highlysuspect.agency/discord) with your questions. I'll try to get back to you.