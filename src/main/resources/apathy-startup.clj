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
	"A rule combinator that sequences other rules together.
	Evaluates rules one-by-one, returning the value of the first one that didn't return nil. Returns nil itself if none matched."
	[& rules]
	(fn [mob player]
		(some #(% mob player) rules)))

(defn debug-rule
	"Wraps another rule, logs a message when it's invoked, and logs whatever it output."
	[message rule]
	(fn [mob player]
		(do
			(log-msg message)
			(let [result (rule mob player)]
				(log-msg (str "Result: " result))
				result))))

(defn always-allow
	"A rule that always returns :allow ."
	[] (constantly :allow))

(defn always-deny 
	"A rule that always returns :deny ."
	[] (constantly :deny))

(defn always-pass
	"A rule that always returns nil."
	[] (constantly nil))

(defn difficulty-map [rules]
	(fn [mob player]
		(if-let [f (get rules (keyword (Api/difficultyOf mob)))]
			(f mob player))))

(defn difficulty-case [& cases]
	(difficulty-map (apply assoc {} cases)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; partial rules

;; What is a partial rule?
;; A partial rule is a predicate on [mob player]. It returns a Boolean instead of :allow :deny or nil.
;; Many predicates over [mob player] can be interpreted to mean "true means 'yes, attack'" or "true means 'yes, don't attack'".
;; allow-if and deny-if lift a partial rule into a rule, following one of those two interpretations.

(defn allow-if
	"Lifts a partial rule (predicate) into a rule. The rule returns :allow if the predicate is true, and nil if it's not.
	Example: (allow-if (attacker-has-tag 'mymodpack:bosses))"
	[partial]
	(fn [mob player] (if (partial mob player) :allow nil)))

(defn deny-if
	"Lifts a partial rule (predicate) into a rule. The rule returns :deny if the predicate is true, and nil if it's not.
	Example: (deny-if (difficulty 'easy))"
	[partial]
	(fn [mob player] (if (partial mob player) :deny nil)))

(defn attacker-has-tag
	"Partial rule. true if the attacker has this entity tag, false if they don't.
	Example: (attacker-has-tag 'minecraft:raiders)"
	[& tags]
	(let [tagset (set (map #(Api/parseEntityTypeTag %) tags))]
		(fn [mob player] (some #(Api/entityHasTag mob %) tagset))))

(defn attacker-is
	"Partial rule. true if the argument list contains the attacker's entity ID, false if it doesn't.
	Pass entity IDs as keywords, symbols, or strings.
	Example: (attacker-is 'minecraft:creeper)"
	[& types]
	(let [typeset (set (map #(Api/parseEntityType %) types))]
		(fn [mob player] (contains? typeset (Api/entityTypeOf mob)))))

(defn difficulty
	"Partial rule. true if the argument list contains the world's current difficulty, false if it doesn't.
	Pass difficulties as keywords, symbols, or strings.
	Example: (difficulty :hard)
	Example: (difficulty 'easy :normal)"
	[& diffs]
	(let [diffset (set (map #(Api/parseDifficulty %) diffs))]
		(fn [mob player] (contains? diffset (Api/difficultyOf mob)))))

(defn boss
	"Partial rule. Returns true if the entity is a boss. Bossness is determined via inclusion in the apathy:bosses entity type tag." 
	[]
	(attacker-has-tag 'apathy:bosses))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; rule state

(defn set-rule!
	"Sets the current rule. Calling with more than one parameter chains them together using chain-rule."
	([rule] (set! (Api/rule) rule))
	([first & more] (set! (Api/rule) (apply chain-rule first more))))

; Clojurians help me here, idk how "getters" work in clj especially because it's a functional language and there aren't really getters anyway.
(defn get-rule!
	"Thunk that gets the current rule."
	[] (Api/rule))

(defn reset-rule!
	"Resets the rule to 'all mobs can attack anyone'."
	[]
	(set-rule! (fn [mob player] nil)))

(defn add-rule!
	"Chains this rule onto the end of the current rule. Calling with more than one parameter chains them on as well, in order."
	[& next-rules]
	(let [cur (get-rule!)]
		(set-rule! cur next-rules)))

; Can't hurt
(def set-rules! set-rule!)
(def get-rules! get-rule!)
(def reset-rules! reset-rule!)
(def add-rules! add-rule!)

(defn set-recheck-interval!
	"As an optimization, mobs that are currently targeting a player don't recheck that it's still okay to do so, every single tick.
	This number, which defaults to 20 (once a second), is how often they will do that."
	[interval]
	(set! (Api/recheckInterval) interval))

(defn get-recheck-interval!
	"Thunk that gets the current recheck interval."
	[] (Api/recheckInterval))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; startup bits and bobs

; rule starts as null in java, game will summarily crash without this
(reset-rule!)

(set-recheck-interval! 20)

; called from java
(set! (Api/ruleOutputToBool)
	(fn [thing]
		(case thing
			:deny  true  ; prevent the mob from changing its target
			:allow false ; don't prevent the mob from changing its target
			false)))