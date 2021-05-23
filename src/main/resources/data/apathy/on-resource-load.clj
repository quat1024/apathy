(ns apathy.load
	(:use apathy.api))

(log-msg "Hello, resource load!")

(def noisy-rule (fn [mob player] (do (log-msg "Hello from noisy-rule") false)))

(set-rule!
	(rule-chain
	 (deny-if (difficulty 'easy))
	 (deny-if (difficulty 'normal))
	 (deny-if (attacker-is 'minecraft/creeper))
	 (deny-if noisy-rule)))