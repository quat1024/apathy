# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for Fabric 1.16. Needs [mc-clojure-lib](https://github.com/eutro/mc-clojure-lib) at runtime.

Yes, Clojure. The mod is configured in Clojure.

(This is overdesigned as hell and I apologize in advance.)

## Scripting

The first script that executes is `apathy-startup.clj`, a built-in one to set up Apathy's Clojure library. It's not configurable.

This is the *plan*:

* execute `cfg/apathy.clj` from the config file. (Default config file will have *plenty* of examples, dw if you're not a clojure programmer.)
* execute clojure scripts from data packs

### quick note on security

Yes, Clojure is a Lisp with the full power of Java behind it. No attempts at sandboxing are made. You know what else has the full power of Java? Every single mod you have installed ;)

Scripts are never sent over the network.

This is the *plan*:

* Scripts are not executed on the client. (I just need to make a server entry point, shoudn't be hard at all.)

## Blah

I want to write more complete docs, but in the mean time the Clojure API surface can be discovered in `src/main/resources/apathy-startup.clj`, and the Java API it calls into can be discovered in `Api.java`. I am not a good Clojurian, please forgive me :)

Due to remapping issues, it's not possible to reliably refer to classes and methods from Minecraft within Clojure. Sorry about that, holler if you need a new API function.

## Examples

Every script should begin with the following (you can put whatever you want as `your.namespace`):

```clojure
(ns your.namespace
  (:use apathy.api))
```

For brevity this header will be omitted from the rest of the examples.

Hello world, just to test that things are working. You can also use `(println)` of course, but `log-msg` only works if you imported `apathy.api` correctly.

```clojure
(log-msg "Hello, world!")
```

Prevent mobs from attacking in Easy:

```clojure
(set-rule! (deny-if (difficulty 'easy)))
```

Prevent mobs from attacking in Easy and Normal:

```clojure
(set-rule!
 (rule-chain
  (deny-if (difficulty 'easy))
  (deny-if (difficulty 'normal))))
```

Prevent creepers from attacking:

```clojure
(set-rule! (deny-if (attacker-is 'minecraft/creeper)))
```

`rule-chain` is a rule that evaluates other rules top-to-bottom, and its result is the *first* one that returns `:allow`or `:deny`. Earlier rules may override later ones. So for example, the following rule prevents all mobs from attacking on Easy, prevents only creepers from attacking on Normal, and allows everything on Hard.

```clojure
(set-rule!
 (rule-chain
  (deny-if  (difficulty 'easy))
  (allow-if (difficulty 'hard))
  (deny-if  (attacker-is 'minecraft/creeper))))
```

## Gotchas

Clojure is technically a compiled language in that it compiles to Java bytecode, but Clojure doesn't do much in the way of typechecking. Unless you typoed a function name, if your program has an error, it's likely to compile just fine and blow up at runtime. Test in-game.

Rules are *functions* that take `[mob player]` and return `:allow`, `:deny`, or `:pass`; so if you want a function that produces rules, you must write a function that *returns a function.*

`allow-if` and `deny-if` take functions that return true/false *booleans* and upgrade them into rules. If you return something else from your function, `allow-if` and `deny-if` will not upgrade them correctly. Watch out for that.

```clojure
(def always-passes (fn [mob player] :pass))
(def always-false (fn [mob player] false))

(set-rule! always-passes)           ; always passes
(set-rule! (deny-if always-false))  ; always passes
(set-rule! (deny-if always-passes)) ; always *denies*, probably not what you want
```