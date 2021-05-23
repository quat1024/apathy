(ns apathy.load
	(:use apathy.api))

;(inspect (get-rule!))
;
;(add-rule!
; (debug-rule "easy" (deny-if (difficulty :easy)))
; (debug-rule "hard" (allow-if (difficulty :hard))))
;
;(inspect (get-rule!))
;
;(add-rule!
; (debug-rule "attacker" (deny-if (attacker-is 'minecraft/creeper))))
;
;(inspect (get-rule!))

;(set-rule! (debug-rule "always-deny" always-deny))

(reset-rule!)

(set-rule!
 (allow-if (attacker-is 'minecraft:creeper :minecraft/spider))
 always-deny)