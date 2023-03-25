# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for ~~Fabric~~ Fabric and Forge ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.18.2~~ 1.19.4.

For documentation, view the `docs/` folder.

### before compiling

* Run `clean` before `build` when releasing, mixin annotation processor seems to be a bit picky
* forge build prints some super worrying "invalid stream header" error from deep in the mixin AP, about refmaps, but i think it's okay..??

### Why are the fabric and forge subprojects lowercase in this one but all your other multiloader mods have them uppercase

I don't know

### license yadda yadda

lgpl 3 or later