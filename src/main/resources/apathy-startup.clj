; Defines symbols for use in your apathy config files. Not overridable with a datapack, sorry!
; See the mod's README for examples.

(ns apathy.api
	(:import agency.highlysuspect.apathy.Api))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; stuff

(Api/log "hello from Clojure!")

(defn log-msg
	"Log a message from the mod's logger as a side effect."
	[x]
	(Api/log x))

(defn inspect [x] (Api/inspect x)) ; debugger breakpoint

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; rule combinators

(defn chain-rule
	"A rule combinator that sequences other rules together. Evaluates rules one-by-one, returning the value of the first one that didn't return nil. Returns nil itself if none matched."
	[& rules]
	(fn [mob player]
		(->> rules
		     (map #(% mob player))
		     (filter #(not= % nil))
		     (first))))

(defn debug-rule
	"Wraps another rule, logs a message when it's invoked, and logs whatever it output."
	[message rule]
	(fn [mob player]
		(do
			(log-msg message)
			(let [result (rule mob player)]
				(log-msg (str "Result: " result))
				result))))

(def always-allow (fn [mob player] :allow))
(def always-deny (fn [mob player] :deny))
(def always-pass (fn [mob player] nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; partial rules

;; What is a partial rule?
;; A partial rule is a predicate on [mob player]. It returns a Boolean instead of :allow :deny or :pass.
;; Many predicates over [mob player] can be taken to mean "true means 'yes, attack'" or "true means 'yes, don't attack'".
;; allow-if and deny-if can lift a partial rule into a rule, following those steps.

(defn allow-if
	"Lifts a partial rule (predicate) into a rule. The rule returns :allow if the predicate is true, and nil if it's not.
	Example: (allow-if (attacker-has-tag 'mymodpack:bosses))"
	[a]
	(fn [mob player] (if (a mob player) :allow nil)))

(defn deny-if
	"Lifts a partial rule (predicate) into a rule. The rule returns :deny if the predicate is true, and :pass if it's not.
	Example: (deny-if (difficulty 'easy))"
	[a]
	(fn [mob player] (if (a mob player) :deny nil)))


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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; rule state

(defn set-rule!
	"Sets the current rule. Calling with more than one parameter chains them together using rule-chain."
	([fn] (set! (Api/rule) fn))
	([first & more] (set! (Api/rule) (apply chain-rule first more))))

(defn get-rule!
	"Thunk that gets the current rule."
	; Clojurians help me here, idk how "getters" work in clj especially because it's a functional language and there aren't really getters anyway.
	[]
	(Api/rule))

(defn reset-rule!
	"Resets the rule to 'all mobs can attack anyone'."
	[]
	(set-rule! (fn [mob player] nil)))

(defn add-rule!
	"Chains this rule onto the end of the current rule. Calling with more than one parameter chains them on as well, in order."
	[& next-rules]
	(let [cur (get-rule!)]
		(set-rule! cur next-rules)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; startup bits and bobs

; rule starts as null in java, game will summarily crash without this
(reset-rule!)

; called from java
(set! (Api/toPreventTargetChangeBool)
	(fn [thing]
		(cond
			(= thing :allow) false ; don't prevent the mob from changing its target
			(= thing :deny)  true  ; prevent the mob from changing its target
			:else            false)))