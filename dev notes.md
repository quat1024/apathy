# I'm really good at forgetting how Forge configs work

it's weird but not that complicated

* Make a `ForgeConfigSpec.Builder`, add properties to it, then call `.build()` (or use the `configure` convenience method)
* Keep the config spec around. As soon as possible, call `ModLoadingContext.get().registerConfig()`, passing it as an argument
* The `ForgeConfigSpec.Builder` will return `ConfigValue`/`IntValue`/etc objects. Hold on to them too, at runtime they are to be queried for the live config values, and will update without any user intervention.
* If you want a notification that the config was refreshed, subscribe to `ModConfigEvent.Loading` and `ModConfigEvent.Reloading` on the fml java mod loading context mod event bus. The event fires with a reference to the config in question (and I think it's correct to check it's actually one of yours before taking action)

Builder stuff:

* Use `push` to define a new section and `pop` to end one. Use `comment` before calling other methods to define a config comment. You can put comments on sections.
* For properties, the general case is `ConfigValue<T>`, but you will sometimes get stuff like `BooleanValue` and `DoubleValue` etc. They're not actually unboxed. it's weird

# zombles chasing players in creative

getting called from `NearestAttackableTargetGoal` which seems to be used by lots and lots entities (slime, creeper, enderman, polarbear, vex, evoker, ghast; to name a few), even though only zombies seem to target players in creative :thinking:

it affects zombies, drowned, husks, and zombie villagers, but not piglins/zombified piglins?

it does call `Mob.setTarget` selecting the player, apathy's rules kick in and have no effect, so the default fallthroguh behavior of "allow" is invoked. but that's weird, why is it calling `setTarget` at all, it's a creative-mode player, in vanilla (untested claim) i don't think it would call settarget on creative players

## Where Does Is The Call Come From

* zombie#tick 219, super() call
* mob#tick 325, super() call
* livingentity#tick 2086, this.aiStep
* zombie#aiStep 244
  * unrelated helmet-sunlight-damage calculation
  * super call
* monster#aiStep 50
  * swing time, no action time, super call
* mob#aiStep 497
  * super call (then item pickup)
* livingentity#aiStep 2299
  * jump delay
  * packet sync
  * position/angle/velocity update
  * serverAiStep
* mob#serverAiStep 691
  * tick sensing
  * tick target selector (goal selector)
* goalselector#tick 100
  * buncha stuff but iterates through all goals and calls start() on each one that can be started?
  * second one is a WrappedGoal of NearestAttackableTargetGoal
  * im not really sure what wrappedgoal is but it seems to independently keep an `isRunning` flag so that the wrapee can't be started when it's already started or stopped when it's already stopped
* nearestattackabletargetgoal#start
  * calls mob.setTarget with `this.target`
  * hey wait a minute why is `this.target` being set

new stacktrace, when does `NearestAttackableTargetGoal#setTarget` get called

* blaming "Library source does not match the bytecode" for this but the stacktrace is going straight from `NearestAttackableTargetGoal#start` to `setTarget`

different direction: where is the actual "don't target creative mode players" check implemented?

starting in `TargetingConditions`:

* eventually it calls `source.canAttack(target)`
* Apathy hooks the shit out of this but the super implementation of it calls `target.canBeSeenAsEnemy`
  * apathy's hooks only ever make it return `false` though...
* that is `!isInvulnerable && canBeSeenByAnyone`, but the implementation in `Player` adds an additional constraint on `!getAbilities().invulnerable` (which is set in creative mode)

RIGHT but, apathy hooks `Zombie#canAttack` and i probably should have checked thatSHIT!!! misbracketed ternary!! lmfao ok that was actually easy

# following stuff is pretty old:

# Wither

setTarget system handles the entity the wither is physically chasing but heads do their own thing

- Periodically calls world(/entityview).getTargets to scan for targets, picks a random one, sets resultant entity ID on trackeddata
  - Mitigated in EntityViewMixin by filtering getTargets (fortunately it takes a parameter of the entity doing the targeting)
- Evicts targets if LivingEntity#canTarget becomes false / entity disappears / distance check
  - Mitigated in LivingEntityMixin by patching canTarget

Both of those are kinda wide-reaching mixins idk

# Ender dragon

Idk if the setTarget system is even used. Most attacking stuff happens directly in PhaseManager phases

- HoldingPattern: transitions to StrafePlayer or LandingApproach
  - Mitigated in `HoldingPatternMixin` by not transitioning to StrafePlayer if the player can't be targeted
- StrafePlayer: This is the one where she shoots a fireball at you
  - Mitigated in `StrafePlayerMixin` by transitioning away from this phase if the player can't be targeted
- SittingScanning: uses world/entityview.findClosestPlayer to transition to ChargingPlayer
  - Mitigated in `SittingScanningMixin` by not transitioning to ChargingPlayer if the player can't be targeted
- ChargingPlayer: Vec3d of player position to charge at
  - Mitigated by not transitioning to this phase from SittingScanning, the only one that goes to this phase

areas of improvement:

- HoldingPatternMixin could search for the nearest player allowed to be targeted, instead of only the nearest player and discarding them if they can't be targeted

note that SittingAttacking isn't an actual attack phase, she just roars (cute)