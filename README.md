# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for Fabric 1.16.

## TODO:

* Expose player-lists to Clojure
* Expose revenge timer to Clojure
* Expose "attacker tagged with ____" rule to regular config
* Check that the config behaves like Apathetic Mobs's config file
* Implement special behavior for bosses

Also maybe there is a better config idiom?

## Clojure?

If [Clojure Lib](https://www.curseforge.com/minecraft/mc-mods/clojure-lib) is installed and Clojure integration is enabled in the config file, a Clojure config API is made available.

Apathy will read Clojure files from:

* `config/apathy.clj` in the config directory,
* any `.clj` files placed in the "apathy" directory of a datapack (todo example).

These files are re-submitted to the Clojure runtime on every world load and `/reload` command. Errors while parsing these files will be printed to the log.

The standard library and documentation is available in `src/main/resources/apathy-startup.clj` in this repository. (Due to remapping issues, it's not possible to reliably refer to classes and methods from Minecraft within Clojure, so all you get is the API. Sorry about that, holler if you need a new API function.)

### Scripting overview

The API symbols are available in the `apathy.api` namespace, defined at `src/main/resources/apathy-startup.clj`. The following is an overview of the API concepts.

#### Rules

A "rule" is a function of two arguments - the attacking mob, and the defending player. It can return one of three things:

* `:allow` - Rule says: the mob *is* allowed to attack the player.
* `:deny` - Rule says: the mob *is not* allowed to attack the player.
* `nil` - The rule is not applicable right now. (More on this later.)

You may set the current Clojure rule with `(set-rule!)`, delete it with `(reset-rule!)`, and retrieve it with `(get-rule!)`. (There's also an `(add-rule!)`, more on that in a bit.)

Here is an example of a rule that *never* allows the mob to attack the player.

```clojure
(set-rule! (fn [mob player] :deny))

; This rule is defined in the API:
(set-rule! (always-deny))
```

Here is an example of a rule that only allows attacking on Hard:

```clojure
(def hard (Api/parseDifficulty :hard)) ; womp womp
(set-rule! (fn [mob player] (if (= hard (Api/difficultyOf mob)) :allow)))

; This rule is also defined in the API:
(set-rule! (allow-if (difficulty :hard)))
```

#### Moving up a level of abstraction

A "partial rule" is the same sort of function, but returns either `true` or `false` instead of a tri-state. In other words, it's a predicate over the mob and player.

Here is a function that returns a partial rule:
```clojure
(defn difficulty
	[diff]
	(let [parseDiff (Api/parseDifficulty diff)]
		(fn [mob player] (= parseDiff (Api/difficultyOf mob)))))

; This is a partial rule:
(difficulty :hard)
```

You might ask - does `true` mean "allow the mob to attack", or "deny the mob from attacking"? The answer is "I don't know, you tell me"; `(allow-if)` and `(deny-if)` are functions that lift a partial rule into a rule, via either interpretation:
```clojure
; Allow the mob to attack when the difficulty is Hard.
(set-rule! (allow-if (difficulty :hard)))
; Prevent the mob from attacking when the difficulty is Hard.
(set-rule! (deny-if (difficulty :hard)))
```

`(allow-if)` and `(deny-if)` return `:allow` or `:deny` when the predicate passes, and `nil` when it fails.

🌟 Btw, `(difficulty)` exists readymade in the API, but it takes an arbitrary number of arguments so you can do `(difficulty :normal :hard)` for either Normal or Hard.

#### Moving up another level of abstraction

I mentioned earlier that a rule may return `nil`. By convention, a rule returning `nil` means "check the next rule".

`(chain-rule)` is a *rule combinator*, allowing you to sequence any number of rules together: if the first returns `nil`, it will try the second, if the second returns `nil`, it will try the third... If all of them return `nil`, it will itself return `nil`.

```clojure
; On Easy, no mobs can attack.
; On Normal, only bosses can attack.
; On Hard, every mob can attack.
(set-rule!
	(chain-rule 
		(allow-if (difficulty :hard)) 
		(deny-if  (difficulty :easy)) 
		(allow-if (boss))))
```

Some sugar exists surrounding this:

* `(set-rule!)` will paste its arguments together with `(chain-rule)` if you pass more than one;
* `(add-rule! x)` exists, to "chain off the end" of the existing rule: it calls `(chain-rule (get-rule!) x)`;
* `(add-rule!)` *also* pastes its arguments together if there's more than one.
* Just for fun: because these functions take more than one argument, they're all aliased to `(set-rules!)`, `(add-rules!)` etc.

```clojure
; The following five blocks produce rules that are functionally the same:

(set-rule! (chain-rule x y))

(set-rule! x y)

(set-rule! x)
(add-rule! y)

(reset-rule!)
(add-rule! x y)

(reset-rule!)
(add-rule! x)
(add-rule! y)
```

If the *entire* as-defined-in-Clojure rule returns `nil`, Apathy will then try the regular rule in the config file.

### Gotchas

The Clojure environment is *never* reset, not even when you `/reload`, log out and back in in singleplayer, etc. If your scripts use `(add-rule!)`, remember to call `(reset-rule!)` somewhere in your script first.

### Quick note on security

Yes, Clojure is a Lisp with the full power of Java behind it. No attempts at sandboxing are made. You know what else has the full power of Java? Every single mod you have installed ;)

Scripts are never sent over the network, scripts from the server are never executed on the client.