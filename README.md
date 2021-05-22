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