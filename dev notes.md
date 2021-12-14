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