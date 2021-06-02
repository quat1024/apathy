# Concepts

## Rules

This mod is based around a list of *rules*. Whenever a mob (the "attacker") thinks about targeting a player (the "defender"), it first checks the list of rules to see if it is allowed to do so.

A rule either says "yes, you are **allowed** to attack", "no, you are **denied** from attacking", or **pass**es to the next rule in the sequence.

## Config file

The config file allows you to configure these rules.

* *difficulty rule*: The attacker can target depending on the world difficulty.
* *boss rule*: The attacker can target if it is a "boss" (i.e. in the `apathy:bosses` tag.)
* *mob set rule*: The attacker can target depending on whether its ID appears in a list of mob IDs.
* *player set rule*: The attacker can target players depending on their inclusion in a player set.
* *revenge rule*: The attacker can target if they were recently provoked.
* *last resort rule*: If *all* of the previous rules returned "pass", this determines their behavior.

Rules are evaluated top-to-bottom (in an order you specify) and the first one that doesn't `pass` is the end result.

If you need more control over rules, such as AND/ORing, getting more copies of each rule, or applying radically different rules for each difficulty:

* see the json system (Not implemented)
* also see the Clojure scripting documentation below.

## Player sets

A player set is... well, it's a set of players. Each player may either be in the set, or not in the set.

The *player set rule* determines what happens when players are in a particular set.

You can choose to make player sets *self-select*, where players can add themselves to the set using `/apathy set join <set name>`. Server operators can add anyone to the set using `/apathy set-admin join <player selector> <set name>`.

