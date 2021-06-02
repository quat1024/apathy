; Defines symbols for use in your apathy config files. Not overridable with a datapack, sorry!
; See the mod's README for examples.

(ns apathy.api
	(:import agency.highlysuspect.apathy.clojure.Api
	         agency.highlysuspect.apathy.rule.Rule
	         agency.highlysuspect.apathy.rule.Partial
	         net.fabricmc.fabric.api.util.TriState))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; stuff

(Api/log "hello from Clojure!")

(defn log-msg
	"Log a message from the mod's logger as a side effect."
	[x]
	(Api/log x))

(defn inspect [x] (Api/inspect x)) ; debugger breakpoint

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; rules and rule combinators

(def allow
	"Return 'allow' from a rule to specify that the attacker is allowed to attack the defender."
	(. TriState/TRUE))

(def deny
	"Return 'deny' from a rule to specify that the attacker is to be prevented from attacking the player."
	(. TriState/FALSE))

(def pass
	"Return 'pass' from a rule if the rule does not have anything to say about whether the attacker should be able to attack the player."
	(. TriState/DEFAULT))

(defn chain-rule
	"A rule combinator that sequences other rules together.
	Evaluates rules one-by-one, returning the value of the first one that didn't return nil. Returns nil itself if none matched."
	[& rules]
	(. Rule/chainMany rules))

(defn debug-rule
	"Wraps another rule, logs a message when it's invoked, and logs whatever it output."
	[message rule]
	(. Rule/debug message rule))

(defn always-allow
	"A rule that always returns allow.
	Prefer this over making your own rule, the rule engine knows how to optimize this."
	[] (. Rule/ALWAYS_ALLOW))

(defn always-deny
	"A rule that always returns deny.
	Prefer this over making your own rule, the rule engine knows how to optimize this."
	[] (. Rule/ALWAYS_DENY))

(defn always-pass
	"A rule that always returns pass.
	Prefer this over making your own rule, the rule engine knows how to optimize this."
	[] (. Rule/ALWAYS_PASS))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; partial rules

(defn predicated
	"Lift a Partial into a Rule. The rule returns ifTrue if the partial succeeds, and ifFalse if it fails."
	[partial ifTrue ifFalse]
	(. Rule/predicated partial ifTrue ifFalse))

; (predicated (x) allow pass) 
(defn allow-if [partial] (. Rule/allowIf partial))
; (predicated (x) deny pass)
(defn deny-if [partial] (. Rule/denyIf partial))

(defn always-true
	"A partial that always succeeds.
	Prefer this over making your own partial, the rule engine knows how to optimize this."
	[] (. Partial/ALWAYS_TRUE))

(defn always-false
	"A partial that always fails.
	Prefer this over making your own partial, the rule engine knows how to optimize this."
	[] (. Partial/ALWAYS_FALSE))

; Inverts a partial
(defn not [partial] (. Partial/not partial))
; True if all its arguments return true
(defn all [& partials] (. Partial/all (set partials)))
; True if any of its arguments return true
(defn any [& partials] (. Partial/any (set partials)))
; True if an odd number of its arguments return true, aka "xor"
(defn odd [& partials] (. Partial/odd (set partials)))

(defn difficulty-is
	"A partial that succeeds if the world difficulty is set to one of the given values.
	Difficulties can be specified as strings, Clojure symbols, or Clojure keywords.
	Example: (difficulty-is :easy :hard)"
	[& diffs]
	(. Partial/difficultyIsAny (set (map #(Api/parseDifficulty %) diffs))))

(defn attacker-has-tag
	"A partial that succeeds if the attacker has any of the given entity tags.
	Tags can be specified as strings, Clojure symbols, or Clojure keywords.
	Example: (attacker-has-tag :minecraft/raiders)"
	[& tags]
	(. Partial/attackerTaggedWithAny (set (map #(Api/parseEntityTypeTag %) tags))))

(defn attacker-is-boss
	"Synonym for (attacker-has-tag :apathy/bosses)."
	[]
	(. Partial/attackerIsBoss))

(defn attacker-is
	"A partial that succeeds if the attacker is of one of the given entity types.
	Entity types can be specified as strings, Clojure symbols, or Clojure keywords.
	Example: (attacker-has-tag :minecraft/creeper :minecraft/spider)"
	[& types]
	(. Partial/attackerIsAny (set (map #(Api/parseEntityType %) types))))

(defn player-in-set
	"A partial that succeeds if the player is in the given player set.
	Player sets are specified as strings.
	Example: (player-in-set \"no-mobs\")"
	[& sets]
	(. Partial/inAnyPlayerSetNamed (set sets)))

(defn revenge
	"A partial that succeeds if the attacker has themselves been attacked some time within the last 'timer' ticks."
	[timer]
	(. Partial/revengeTimer timer))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; rule state

(defn set-rule!
	"Sets the current Clojure rule.
	A rule is a function that takes two arguments - the attacking mob and the defending player - and returns one of :allow, :deny, or nil.
	
	An :allow return means the mob is allowed to attack the player.
	A :deny return means the mob is not allowed to attack the player.
	A nil return means 'I dunno, pass to the next rule'. The 'next rule' could be the next in a chain-rule, or
	if all rules have been exhausted and there's still no non-nil returns, it is the rule defined in the mod's config file."
	([rule] (set! (Api/clojureRule) rule))
	([first & more] (set! (Api/clojureRule) (apply chain-rule first more))))

; Clojurians help me here, idk how "getters" work in clj especially because it's a functional language and there aren't really getters anyway.
(defn get-rule!
	"Thunk that gets the current Clojure rule."
	[] (Api/clojureRule))

(defn reset-rule!
	"Removes the current Clojure rule. Now, every 'can this mob attack this player' question is answered by the config file only."
	[]
	(set-rule! (fn [mob player] nil)))

(defn add-rule!
	"Chains this rule onto the end of the current Clojure rule. Calling with more than one parameter chains them on as well, in order."
	[& next-rules]
	(let [cur (get-rule!)]
		(set-rule! cur next-rules)))

; Can't hurt
(def set-rules! set-rule!)
(def get-rules! get-rule!)
(def reset-rules! reset-rule!)
(def add-rules! add-rule!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; startup bits and bobs

; rule starts as null in java, game will summarily crash without this
(reset-rule!)