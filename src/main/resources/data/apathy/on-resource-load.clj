(ns apathy.load
	(:use apathy.api))

;(reset-rule!)

;(inspect (get-rule!))
;
;(add-rule!
; (debug-rule "easy" (deny-if (difficulty 'easy)))
; (debug-rule "hard" (allow-if (difficulty 'hard))))
;
;(inspect (get-rule!))
;
;(add-rule!
; (debug-rule "attacker" (deny-if (attacker-is 'minecraft/creeper))))
;
;(inspect (get-rule!))

;(set-rule! (debug-rule "always-deny" always-deny))

(log-msg "poggers!")
(inspect (get-rule!))

(set-rule! always-deny)
(inspect (get-rule!))

(set-rule! always-pass always-deny)
(inspect (get-rule!))