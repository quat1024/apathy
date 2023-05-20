# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for ~~Fabric~~ Fabric and Forge ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.18.2~~ ~~1.19.4~~ ~~1.18.2 again?~~ ~~1.18.2 and 1.19.4~~ ~~1.18.2 and 1.19.2 and 1.19.4~~ 1.16.5 (not forge) and 1.18.2 and 1.19.2 and 1.19.4. Are you happy yet.

For documentation, view the `docs/2.x/` folder.

## road map

* publish a `3.0` that addresses some long-standing issues in the mod (i am so, so sorry about this "json config format")

# Code note

Versions prior to 2.5 were developed separately, one codebase per version. Maintaining that was difficult, so everything is now in a giant Gradle subproject. This is also unwieldy but in a different way - if you're having trouble fitting the damn thing into RAM, comment out a few subprojects from `settings.gradle`.

* `core` - Truly version-independent code, only depends on a (slightly old version of) google gson. Lowest-common-denominator code.
* `common-xxx` - Allows accessing Minecraft (through [minivan](https://github.com/CrackedPolishedBlackstoneBricksMC/minivan), formerly [VanillaGradle](https://github.com/SpongePowered/VanillaGradle)) and writing the code of mixins. Contains glue between the version-independent core and the Minecraft version in question.
* `fabric-xxx` and `forge-xxx` - Can refer to Minecraft as well as features from the specific modloader. This is generally just a tiny bit of glue code, initialization using modloader services, platform-specific mixins, blah blah.

Each layer includes the code of the previous, both as a Gradle dependency and textually (the sources are compiled together). The stuff in the core is compiled many times over. It's not great.

The `collect.sh` script will dig into each subproject's `build/libs/` directory, copy all jars into a `collect/` directory at the top level, and organize them a bit. This is done in CI to make the artifacts download more convenient.

### where's 1.19.2/.3?

If You're Going To Insist On Playing Modded On The Latest Version Of Minecraft, At Least Have Conviction, I Cannot Support Every Random Point Release And Snapshot Of The Game Indefinitely

### where's 1.17?

I've decided to drop 1.17 due to extremely low adoption from the playerbase. The mod got like 22 downloads; most of those were surely the 9minecaft bots.

### where's 1.16 forge?

As far as I can tell it's impossible to do multiloader 1.16 mods with a "shared" source-set (and apathy has a lot of shared source) because Forge 1.16's "official" mappings channel is a lie and not actually official mappings. It uses method names from official mappings but class names from the previous MCP version.

## license yadda yadda

lgpl 3 or later