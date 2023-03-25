# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for ~~Fabric~~ Fabric and Forge ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.18.2~~ 1.19.4.

For documentation, view the `docs/2.x/` folder.

## wow there's not any code-sharing across versions

I know. Originally each version was developed in a separate branch; when making a cross-version fix I'd run a bunch of `git checkout`s and `cherry-pick`s. This sucked, so I've now hastily pasted the source of everything as subprojects into the same file tree. But the sources are otherwise unmodified.

Eventually I hope to publish a v3 that addresses some long-standing issues in the mod (i am so, so sorry about this "json config format") while moving most of the logic into a reusable, version-independent "core" module. That will come at a later time.

### where's 1.17?

I've decided to drop 1.17 due to extremely low adoption from the playerbase.

### where's 1.16 forge?

I only ported the mod to Forge in 1.18. The Gradle subproject structure is an anachronism and there was never a 1.16 Forge version.

I am interested in producing a port to this version (provided ForgeGradle cooperates).

## license yadda yadda

lgpl 3 or later