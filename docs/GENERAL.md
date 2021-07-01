# `general.cfg`

This file contains miscellaneous config options.

# Optimization

## Recheck Interval

By default, mobs that are currently attacking a player do not check *every* tick if it's still okay to do so. There's an option to set the interval they'll use instead. (It's staggered a bit, based off the sequential entity ID, so they don't all check at once and cause a big lagspike on the server.)

## Run rule optimizer

If `true`, Apathy will try to compile rules to a smaller and more compact form before loading them into the game. No-op rules will be removed, "and"/"or" rules with only one entry will be unwrapped, and some other things that should keep the *behavior* of the rule the same.

You can turn this off if it's overzealous (although report any bugs with it to me, please)

# Debug

## Debug rules

You can debug the config file rule and the JSON rule. When these options are enabled, the rule will be serialized and written to the directory `config/apathy/dumps/`. If the rule optimizer is enabled, rules will also be debugged after the optimization pass to a separate file, so you can observe the effects of the optimizer.