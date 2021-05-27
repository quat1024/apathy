(ns apathy.load
	(:use apathy.api))

(reset-rule!)

(set-rule!
 (allow-if (difficulty "hard"))
 (deny-if  (difficulty "easy"))
 (allow-if (boss))
)

;(set-rule!
; (difficulty-case 
;  :easy (always-deny)
;  :normal (chain-rule (allow-if (boss)) (always-deny))
;  :hard (always-allow)))