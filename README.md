# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for ~~Fabric~~ Fabric and Forge ~~1.16~~ ~~1.17~~ ~~1.18~~ 1.18.2. For documentation, view the `docs/` folder.

## Thanks

This cursed abomination of a Gradle setup is courtesy of jaredlll08 and Darkhax's [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template). It's not a straight copy-paste, i edited things a little bit. I hope this is maintainable into the future lmao

### before compiling

* Run `clean` before `build` when releasing, mixin annotation processor seems to be a bit picky
* forge build prints some super worrying "invalid stream header" error from deep in the mixin AP, about refmaps, but i think it's okay..??

### license yadda yadda

lgpl 3 or later