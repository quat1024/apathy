# Wither

setTarget system handles the entity the wither is physically chasing but heads do their own thing

- Periodically calls world(/entityview).getTargets to scan for targets, picks a random one, sets resultant entity ID on trackeddata
  - Mitigated in EntityViewMixin by filtering getTargets (fortunately it takes a parameter of the entity doing the targeting)
- Evicts targets if LivingEntity#canTarget becomes false / entity disappears / distance check
  - Mitigated in LivingEntityMixin by patching canTarget

Both of those are kinda wide-reaching mixins idk

# Ender dragon

Idk if the setTarget system is even used. Most attacking stuff happens directly in PhaseManager phases

- ChargingPlayer: Vec3d of player position to charge at, sittingScanning is in charge of setting this position
- HoldingPattern: transitions to StrafePlayer or LandingApproach
- StrafePlayer: This is the one where she shoots a fireball
- SittingScanning: uses world/entityview.findClosestPlayer