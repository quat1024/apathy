# Apathy

port/rewrite of Apathetic Mobs

## Config syntax

(None of this is implemented at the moment)

The thing is, there's actually a fairly complex series of conditions involved in "should X be allowed to attack Y"

So you get a Lisp.

```
(allowAttackIf (boss))
(allowAttackIf (revengeTimer 50))

(denyAttackIf (difficulty 'easy 'normal))
(denyAttackIf (playerSelfSelect))
(denyAttackIf (attackerIs 'minecraft:zombie 'minecraft:cow 'minecraft:pig))
(denyAttackIf (attackerIsTag 'something:badmobs))
(denyAttackIf (playerHas 'minecraft:apple))
(denyAttackIf (playerHasTag 'minecraft:logs))
```

There's `(define x y)` as well, but no user-defined functions.

## Some doc

The Clojure API surface can be discovered in `src/main/resources/apathy-startup.clj`. I am not a good Clojurian, please forgive me :)

Due to remapping issues, it's not possible to refer directly to Java classes and methods from Minecraft from Clojure. Sorry about that. I've tried to expose as much as is needed, hmu if you need some more API functions. My hope is that enough built-in functions are exposed from Clojure that you don't need to refer to things directly.

All functions that take Identifiers coerce them from Strings (`"namespace:path"`), Clojure symbols (`'namespace/path`), and Clojure keywords (`':namespace/path`). Symbols are recommended.