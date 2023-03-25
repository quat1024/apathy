# `general.cfg`

This file contains miscellaneous config options.

# Optimization

## Recheck Interval

By default, mobs that are currently attacking a player do not check *every* tick if it's still okay to do so. There's an option to set the interval they'll use instead. (It's staggered a bit, based off the sequential entity ID, so they don't all check at once and cause a big lagspike on the server.)

## Run rule optimizer

If `true`, Apathy will try to compile rules to a smaller and more compact form before loading them into the game. No-op rules will be removed, "and"/"or" rules with only one entry will be unwrapped, and some other things that should keep the *behavior* of the rule the same.

You can turn this off if it's overzealous (although report any bugs with it to me, please)

# Wow even more misc options **(since 2.4)**

yeah idk where to put this stuff

## zombieAttackVillagerDifficulties

Comma-separated list of difficulties where zombies are allowed to chase after villagers. Note that this also pretty much removes the ability to cheese villagers by threatening them with a zombie, and also kinda breaks iron farms. Use with caution.

# Revenge Spread **(since 2.3)**

When a player attacks a mob, Apathy sets its "last attack time" on that mob to the current world time. The options in this category allow other nearby mobs to obtain the "last attack time", too, which pertains to the behavior of Apathy "revenge timer" rules.

## angryPiggies

Ok I lied a bit above, this is a comma-separated list of difficulties where zombified pigmen will alert their friends after one is attacked, using the *vanilla* mechanics. But if the mechanic is enabled, it also will spread the Apathy revengeTimer to them, too.

## sameTypeRevengeSpread and differentTypeRevengeSpread

If you attack a mob, nearby mobs within this radius will *also* get the revengeTimer.

`sameType` controls mobs of the same type (zombies alerting other zombies), and `differentType` controls mobs of any type (zombies alerting skeletons, and also other zombies... maybe i should rename it)

# Debug

Options that might help when you are making a config file.

## Debug rules

You can debug the config file rule and the JSON rule. When these options are enabled, the rule will be serialized and written to the directory `config/apathy/dumps/`. If the rule optimizer is enabled, rules will also be debugged after the optimization pass to a separate file, so you can observe the effects of the optimizer.