(ns apathy.load
	(:use apathy.api))

(reset-rule!)

(set-rule!
 (allow-if (difficulty-is "hard"))
 (deny-if  (difficulty-is "easy"))
 (allow-if (attacker-is-boss))
)

;(set-rule!
; (difficulty-case 
;  :easy (always-deny)
;  :normal (chain-rule (allow-if (boss)) (always-deny))
;  :hard (always-allow)))