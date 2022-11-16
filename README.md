# Apathy

Overconfigurable port/rewrite of Apathetic Mobs for ~~Fabric~~ Fabric and Forge ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.18.2~~ ~~1.19.2~~ fucking everything.

For documentation, view the `docs/` folder.

## Hell Era

This is the Hell Era branch, in this branch I attempt to get my money's worth out of those RAM sticks I bought a while ago.

* `(loader)-(version)` - Loader-specific and version-specific code.
* `(loader)-xver` - Loader-specific code that is not version-specific. Loaders are moving targets too so I imagine I won't have much of these.
* `Xplat-(version)` - Version-specific, but not loader-specific code.
* `Xplat-Xver` - Code shared among all releases. Doesn't touch Minecraft at all.

There is still going to be a lot of copy-pasting; making shit like "forge code for 1.16 through 1.18 but it broke in 1.19" is going to be a bridge too far, I'd rather paste it into the relevant forge-1.1x branches.

The goal is to release eight artifacts: (1.16, 1.17, 1.18, 1.19) * (forge, fabric). Ten when 1.20 rolls around.

#### where quilt

My rate for this will be fifty bucks an hour, including IntelliJ loading time.

Or use qfapi.

# license yadda yadda

lgpl 3 or later