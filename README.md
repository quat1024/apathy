# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for ~~Fabric~~ Fabric and Forge ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.18.2~~ ~~1.19.4~~ ~~1.18.2 again?~~ ~~1.18.2 and 1.19.4~~ ~~1.18.2 and 1.19.2 and 1.19.4~~ 1.16.5 (not forge) and 1.18.2 and 1.19.2 and 1.19.4. Are you happy yet.

For documentation, view the `docs/2.x/` folder.

## todo

* 1.19.2 forge is broken (at least in dev) and this time i cant figure out why. it kinda deserves it for being 1.19.2

## wow the code is a mess

Originally each version was developed in a separate branch; when making a cross-version fix I'd run a bunch of `git checkout`s and `cherry-pick`s. This sucked so it's now subprojects. But if you see anything inconsistent it might be a relic of that time.

Roadmap:

* publish a `2.5` for 1.18, 1.19, and maybe 1.16 that finally uses a shared codebase in `core`.
* publish a `3.0` that addresses some long-standing issues in the mod (i am so, so sorry about this "json config format")

### where's 1.19.2/.3?

# **Please Stop Playing 1.19.2**

**If You're Going To Insist On Playing Modded On The Latest Version Of Minecraft, At Least Have Conviction. I Cannot Support Every Random Point Release And Snapshot Of The Game Indefinitely**

### where's 1.17?

I've decided to drop 1.17 due to extremely low adoption from the playerbase. I think the mod got like 22 downloads and most of those were surely the 9minecaft bots.

### where's 1.16 forge?

As far as I can tell it's impossible to do multiloader 1.16 mods with a "shared" source-set (and apathy has a lot of shared source) because Forge 1.16's "official" mappings channel is a lie and not actually official mappings.

* somehow convince mcpconfig to use real not-lies official names (will $100% break forge)
* paste the common sourceset into the forge project and rename all the classes (will have to wait until i have some spare patience for that task >.>)

## license yadda yadda

lgpl 3 or later