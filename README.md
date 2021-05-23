# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for Fabric 1.16. Needs [mc-clojure-lib](https://github.com/eutro/mc-clojure-lib) at runtime.

Yes, Clojure. The mod is configured in Clojure.

(This is overdesigned as hell and I apologize in advance.)

## Scripting

The first script that executes is `apathy-startup.clj`, a built-in one to set up Apathy's Clojure library. It's not configurable.

This is the *plan*:

* execute `cfg/apathy.clj` from the config file. (Default config file will have *plenty* of examples, dw if you're not a clojure programmer. I'm not either.)
* execute clojure scripts from data packs

### quick note on security

Yes, Clojure is a Lisp with the full power of Java behind it. No attempts at sandboxing are made. You know what else has the full power of Java? Every single mod you have installed ;)

Scripts are never sent over the network, scripts from the server are never executed on the client.

## Blah

I want to write more complete docs, but in the mean time the Clojure API surface can be discovered in `src/main/resources/apathy-startup.clj`, and the Java API it calls into can be discovered in `Api.java`. I am not a good Clojurian, please forgive me :)

Due to remapping issues, it's not possible to reliably refer to classes and methods from Minecraft within Clojure. Sorry about that, holler if you need a new API function.

## Examples

I wrote docs like 5 minutes ago but they're already out of date Lol. Mod is still WIP...

See `apathy-startup.clj` for the standard library

## Gotchas

The Clojure environment is never reset, not even when you `/reload`, log out and back in in singleplayer, etc. If you use `(add-rule!)` instead of `(set-rule!)`, remember to call `(reset-rule!)` in your script.