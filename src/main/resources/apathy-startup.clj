; Defines symbols for use in your apathy config files.
; Not overridable with a datapack, sorry!
; See the mod's README for examples.

(ns apathy.api
	(:import agency.highlysuspect.apathy.Api))

(Api/log "This is printed from the top of the Clojure script :)")

(defn log-msg
	"Log a message from the mod's logger as a side effect."
	[x]
	(Api/log x))

(defn set-rule! [fn]
	"What is a rule?
	A rule is a function of two arguments: [mob player]
	where 'mob' is the mob thinking about performing an attack, and 'player' is the prospective target.
	A rule returns one of three keywords:
  :allow - The mob is allowed to attack the player.
  :deny  - The mob is not allowed to attack the player.
  :pass  - This rule doesn't apply right now."
	(set! (. Api rule) fn))

(defn reset-rule!
	"Reset the rule to 'all mobs can attack anyone'."
	[]
	(set-rule! (fn [mob player] :allow)))

(defn current-rule [] (Api/rule))

(defn if-pass [thing val] (if (= thing :pass) val thing))
(defn pass-if-nil [thing] (if (= thing nil) :pass thing))

(defn rule-chain
	"A rule that sequences other rules together. Returns the value of the first rule that didn't return :pass, or :pass if none of them matched."
	[& rules]
	(fn [mob player]
		(pass-if-nil
		 (->> (seq rules) ;"seq" is lazy, kinda like a java Stream, so rules are pay-as-you-go. nice.
		      (map #(% mob player)) 
		      (filter #(not= % :pass)) 
		      (first)))))

;; What is a partial rule?
;; A partial rule is a predicate on [mob player]. It returns a Boolean instead of :allow :deny or :pass.
;; Many predicates over [mob player] can be taken to mean "true means 'yes, attack'" or "true means 'yes, don't attack'".
;; allow-if and deny-if can lift a partial rule into a rule, following those steps.

(defn allow-if
	"Lifts a partial rule (predicate) into a rule. The rule returns :allow if the predicate is true, and :pass if it's not.
	Example: (allow-if (attacker-has-tag 'mymodpack:bosses))"
	[a]
	(fn [mob player] (if (a mob player) :allow :pass)))

(defn deny-if
	"Lifts a partial rule (predicate) into a rule. The rule returns :deny if the predicate is true, and :pass if it's not.
	Example: (deny-if (difficulty 'easy))"
	[a]
	(fn [mob player] (if (a mob player) :deny :pass)))


(defn attacker-has-tag
	"Partial rule. true if the attacker has this entity tag, false if they don't.
	Example: (attacker-has-tag 'minecraft:raiders)"
	[tag]
	(let [conv-tag (Api/toEntityTypeTag tag)]
		(fn [mob player] (Api/attackerHasTag mob conv-tag))))

(defn attacker-is
	"Partial rule. true if this is the attacker's entity ID, false if it's not.
	Example: (attacker-is 'minecraft:creeper)"
	[type]
	(let [conv-type (Api/toEntityType type)]
		(fn [mob player] (Api/attackerIs mob conv-type))))

(defn difficulty
	"Partial rule. true if this is the world's current difficulty, false if it's not.
	Pass difficulties as symbols.
	Example: (difficulty 'hard)"
	[diff]
	(let [conv-diff (Api/toDifficulty diff)] ; parse
		(fn [mob player] (Api/difficultyIs mob conv-diff))))

;;;;;;;;;;;;;;;;;;;

; and on startup, set the rule to the "always pass" one
(reset-rule!)

(Api/log "This is printed from the bottom of the Clojure script!")