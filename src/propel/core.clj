(ns propel.core
  (:gen-class))


; (propel.core/-main)

(def example-push-state
  {:exec '()
   :integer '(1 2 3 4 5 6 7)
   :string '("abc")
   :input {:in1 4}})

; Instructions must all be either functions that take one Push state and return another
; or constant literals.
; TMH: ERCs?
(def default-instructions
  (list
   'in1
   'integer_+
   'integer_-
   'integer_*
   'integer_=
   'integer_to_string
   'exec_dup
   'exec_if
   'boolean_and
   'boolean_or
   'boolean_not
   'boolean_=
   'string_=
   'string_take
   'string_drop
   'string_reverse
   'string_length
   'string_includes?
   'close
   true
   false
   -250
   0
   250
   500
   750
   1000
   1250
   1500
   1750
   2000
   2250
   2500
   2750
   3000
   ""
   "small"
   "large"
   "abcdefghijklmnopqrstuvwxyz"
   ))

(def opens ; number of blocks opened by instructions (default = 0)
  {'exec_dup 1
   'exec_if 2})

;;;;;;;;;
;; Utilities

(def empty-push-state
  {:exec '()
   :integer '()
   :string '()
   :boolean '()
   :input {}})

(defn abs
  "Absolute value."
  [x]
  (if (neg? x)
    (- x)
    x))

(defn not-lazy
  "Returns lst if it is not a list, or a non-lazy version of lst if it is."
  [lst]
  (if (seq? lst)
    (apply list lst)
    lst))

(defn push-to-stack
  "Pushes item onto stack in state"
  [state stack item]
  (update state stack conj item))

(defn pop-stack
  "Removes top item of stack."
  [state stack]
  (update state stack rest))

(defn peek-stack
  "Returns top item on a stack."
  [state stack]
  (if (empty? (get state stack))
    :no-stack-item
    (first (get state stack))))

(defn empty-stack?
  "Returns true if the stack is empty."
  [state stack]
  (empty? (get state stack)))

(defn get-args-from-stacks
  "Takes a state and a list of stacks to take args from. If there are enough args
  on each of the desired stacks, returns a map of the form {:state :args}, where
  :state is the new state and :args is a list of args from the stacks. If there
  aren't enough args on the stacks, returns :not-enough-args."
  [state stacks]
  (loop [state state
         stacks (reverse stacks)
         args '()]
    (if (empty? stacks)
      {:state state :args args}
      (let [stack (first stacks)]
        (if (empty-stack? state stack)
          :not-enough-args
          (recur (pop-stack state stack)
                 (rest stacks)
                 (conj args (peek-stack state stack))))))))

(defn make-push-instruction
  "A utility function for making Push instructions. Takes a state, the function
  to apply to the args, the stacks to take the args from, and the stack to return
  the result to. Applies the function to the args (taken from the stacks) and pushes
  the return value onto return-stack."
  [state function arg-stacks return-stack]
  (let [args-pop-result (get-args-from-stacks state arg-stacks)]
    (if (= args-pop-result :not-enough-args)
      state
      (let [result (apply function (:args args-pop-result))
            new-state (:state args-pop-result)]
        (push-to-stack new-state return-stack result)))))

;;;;;;;;;
;; Instructions

(defn in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack."
  [state]
  (push-to-stack state :exec (:in1 (:input state))))

(defn integer_+
  [state]
  (make-push-instruction state +' [:integer :integer] :integer))

(defn integer_-
  [state]
  (make-push-instruction state -' [:integer :integer] :integer))

(defn integer_*
  [state]
  (make-push-instruction state *' [:integer :integer] :integer))


(defn integer_to_string
  [state]
  (make-push-instruction state str [:integer] :string))

(defn integer_=
  [state]
  (make-push-instruction state = [:integer :integer] :boolean))

(defn exec_dup
  [state]
  (if (empty-stack? state :exec)
    state
    (push-to-stack state :exec (first (:exec state)))))

(defn exec_if
  [state]
  (make-push-instruction state
                         #(if %1 %3 %2)
                         [:boolean :exec :exec]
                         :exec))

(defn boolean_and
  [state]
  (make-push-instruction state #(and %1 %2) [:boolean :boolean] :boolean))

(defn boolean_or
  [state]
  (make-push-instruction state #(or %1 %2) [:boolean :boolean] :boolean))

(defn boolean_not
  [state]
  (make-push-instruction state not [:boolean] :boolean))

(defn boolean_=
  [state]
  (make-push-instruction state = [:boolean :boolean] :boolean))

(defn string_=
  [state]
  (make-push-instruction state = [:string :string] :boolean))

(defn string_take
  [state]
  (make-push-instruction state
                         #(apply str (take %1 %2))
                         [:integer :string]
                         :string))

(defn string_drop
  [state]
  (make-push-instruction state
                         #(apply str (drop %1 %2))
                         [:integer :string]
                         :string))

(defn string_reverse
  [state]
  (make-push-instruction state
                         #(apply str (reverse %))
                         [:string]
                         :string))


(defn string_length
  [state]
  (make-push-instruction state count [:string] :integer))

(defn string_includes?
  [state]
  (make-push-instruction state clojure.string/includes? [:string :string] :boolean))

;;;;;;;;;
;; Interpreter

(defn interpret-one-step
  "Takes a Push state and executes the next instruction on the exec stack."
  [state]
  (let [popped-state (pop-stack state :exec)
        first-raw (first (:exec state))
        first-instruction (if (symbol? first-raw)
                            (eval first-raw)
                            first-raw)]
    (cond
      (fn? first-instruction)
      (first-instruction popped-state)
      ;
      (integer? first-instruction)
      (push-to-stack popped-state :integer first-instruction)
      ;
      (string? first-instruction)
      (push-to-stack popped-state :string first-instruction)
      ;
      (seq? first-instruction)
      (update popped-state :exec #(concat %2 %1) first-instruction)
      ;
      (or (= first-instruction true) (= first-instruction false))
      (push-to-stack popped-state :boolean first-instruction)
      ;
      :else
      (throw (Exception. (str "Unrecognized Push instruction in program: "
                              first-instruction))))))

(defn interpret-program
  "Runs the given problem starting with the stacks in start-state."
  [program start-state step-limit]
  (loop [state (assoc start-state :exec program :step 1)]
    (if (or (empty? (:exec state))
            (> (:step state) step-limit))
      state
      (recur (update (interpret-one-step state) :step inc)))))

(defn push-from-plushy
  "Returns the Push program expressed by the given plushy representation."
  [plushy]
  (let [opener? #(and (vector? %) (= (first %) 'open))] ;; [open <n>] marks opens
    (loop [push () ;; iteratively build the Push program from the plushy
           plushy (mapcat #(if-let [n (get opens %)] [% ['open n]] [%]) plushy)]
      (if (empty? plushy)       ;; maybe we're done?
        (if (some opener? push) ;; done with plushy, but unclosed open
          (recur push '(close)) ;; recur with one more close
          push)                 ;; otherwise, really done, return push
        (let [i (first plushy)]
          (if (= i 'close)
            (if (some opener? push) ;; process a close when there's an open
              (recur (let [post-open (reverse (take-while (comp not opener?)
                                                          (reverse push)))
                           open-index (- (count push) (count post-open) 1)
                           num-open (second (nth push open-index))
                           pre-open (take open-index push)]
                       (if (= 1 num-open)
                         (concat pre-open [post-open])
                         (concat pre-open [post-open ['open (dec num-open)]])))
                     (rest plushy))
              (recur push (rest plushy))) ;; unmatched close, ignore
            (recur (concat push [i]) (rest plushy)))))))) ;; anything else

;;;;;;;;;
;; GP

(defn make-random-plushy
  "Creates and returns a new plushy."
  [instructions max-initial-plushy-size]
  (repeatedly (rand-int max-initial-plushy-size)
              #(rand-nth instructions)))

(defn tournament-selection
  "Selects an individual from the population using a tournament."
  [pop argmap]
  (let [tournament-size (:tournament-size argmap)
        tournament-set (take tournament-size (shuffle pop))]
    (apply min-key :total-error tournament-set)))

(defn lexicase-selection
  "Selects an individual from the population using lexicase selection."
  [pop argmap]
  (loop [survivors pop
         cases (shuffle (range (count (:errors (first pop)))))]
    (if (or (empty? cases)
            (empty? (rest survivors)))
      (rand-nth survivors)
      (let [min-err-for-case (apply min (map #(nth % (first cases))
                                             (map :errors survivors)))]
        (recur (filter #(= (nth (:errors %) (first cases)) min-err-for-case)
                       survivors)
               (rest cases))))))

(defn select-parent
  "Selects a parent from the population using the specified method."
  [pop argmap]
  (case (:parent-selection argmap)
    :tournament (tournament-selection pop argmap)
    :lexicase (lexicase-selection pop argmap)))

(defn crossover
  "Crosses over two individuals using uniform crossover. Pads shorter one."
  [plushy-a plushy-b]
  (let [shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat shorter (repeat length-diff :crossover-padding))]
    (remove #(= % :crossover-padding)
            (map #(if (< (rand) 0.5) %1 %2)
                 shorter-padded
                 longer))))

(defn uniform-addition
  "Randomly adds new instructions before every instruction (and at the end of
  the plushy) with some probability."
  [plushy instructions]
  (let [rand-code (repeatedly (inc (count plushy))
                              (fn []
                                (if (< (rand) 0.05)
                                  (rand-nth instructions)
                                  :mutation-padding)))]
    (remove #(= % :mutation-padding)
            (interleave (conj plushy :mutation-padding)
                        rand-code))))

(defn uniform-deletion
  "Randomly deletes instructions from plushy at some rate."
  [plushy]
  (remove (fn [x] (< (rand) 0.05))
          plushy))

(defn new-individual
  "Returns a new individual produced by selection and variation of
  individuals in the population."
  [pop argmap]
  {:plushy
   (let [prob (rand)]
     (cond
       (< prob 0.5) (crossover (:plushy (select-parent pop argmap))
                               (:plushy (select-parent pop argmap)))
       (< prob 0.75) (uniform-addition (:plushy (select-parent pop argmap))
                                       (:instructions argmap))
       :else (uniform-deletion (:plushy (select-parent pop argmap)))))})

(defn report
  "Reports information each generation."
  [pop generation]
  (let [best (first pop)]
    (println "-------------------------------------------------------")
    (println "               Report for Generation" generation)
    (println "-------------------------------------------------------")
    (print "Best plushy: ") (prn (:plushy best))
    (print "Best program: ") (prn (push-from-plushy (:plushy best)))
    (println "Best total error:" (:total-error best))
    (println "Best errors:" (:errors best))
    (println "Best behaviors:" (:behaviors best))
    (println)))

(defn propel-gp
  "Main GP loop."
  [{:keys [population-size max-generations error-function instructions
           max-initial-plushy-size]
    :as argmap}]
  (println "Starting GP with args:" argmap)
  (loop [generation 0
         population (repeatedly
                     population-size
                     #(hash-map :plushy
                                (make-random-plushy instructions
                                                    max-initial-plushy-size)))]
    (let [evaluated-pop (sort-by :total-error
                                 (map (partial error-function argmap)
                                      population))]
      (report evaluated-pop generation)
      (cond
        (zero? (:total-error (first evaluated-pop))) (println "SUCCESS")
        (>= generation max-generations) nil
        :else (recur (inc generation)
                     (repeatedly population-size 
                                 #(new-individual evaluated-pop argmap)))))))

;;;;;;;;;
;; small large fitness
(defn small-large
  [n]
  (cond
    (< n 1000) "small"
    (>= n 2000) "large"
    :else ""))


(def small-large-training
(map small-large (range -250 3001 250))
)

(defn small-large-output
  [max-error correct-output actual-output]
  (min max-error (-(count actual-output) (count correct-output))))

(def small-large-failure 100)

(defn small-large-failure-error
  [actual-output]
  (cond
    (every? #(clojure.string/includes? actual-output %) ["large" "small"])
    (+ 10 (small-large-output 5 "" actual-output))
    (some #(clojure.string/includes? actual-output %) ["small" "large"])
    (+ 20 (small-large-output 10 "" actual-output))
    :else small-large-failure))

(defn small-large-error
  [correct-output actual-output]
  (cond
    (= actual-output :no-stack-item) (* 100 small-large-failure)
    (clojure.string/includes? actual-output correct-output)
    (small-large-output 20 correct-output actual-output)
    :else small-large-failure))

(defn small-large-error-function
  [argmap individual]
  (let [program (push-from-plushy (:plushy individual))
        inputs (range -250 3001 250)
        correct-outputs small-large-training
        outputs (map (fn [input]
                       (peek-stack
                        (interpret-program
                         program
                         (assoc empty-push-state :input {:in1 input})
                         (:step-limit argmap))
                        :string))
                     inputs)
        errors (map small-large-error
                    correct-outputs
                    outputs)]
    (assoc individual
           :behaviors outputs
           :errors errors
           :total-error (apply +' errors))))

(comment "
(defn nextelt
  ;;Given two characters, the previous row, and a row we are
  ;;building, determine out the next element fot this row.
  [char1 char2 prevrow thisrow position]
  (if (= char1 char2)
    (prevrow (- position 1))
    (+ 1 (min
          (prevrow (- position 1))
          (prevrow position)
          (last thisrow))))")

(comment "
(defn nextrow
  ;;Based on the next character from string1 and the whole of string2
  ;;calculate the next row. Initially thisrow contains one number.
  [char1 str2 prevrow thisrow]
  (let [char2 (first str2)
        position (count thisrow)]
    (if (= (count thisrow) (count prevrow))
      thisrow
      (recur
       char1
       (rest str2)
       prevrow
       (conj thisrow (nextelt char1 char2 prevrow thisrow position))))))")

(comment "
(defn levenshtein1
  ;;Calculate the Levenshtein distance between two strings.
  ([str1 str2]
   (let [row0 (vec (map first (map vector (iterate inc 1) str2)))]
     (levenshtein1 1 (vec (cons 0 row0)) str1 str2)))
  ([row-nr prevrow str1 str2]
   (let [next-row (nextrow (first str1) str2 prevrow (vector row-nr))
         str1-remainder (.substring str1 1)]
     (if (= "" str1-remainder)
       (last next-row)
       (recur (inc row-nr) next-row str1-remainder str2)))))")

;;;;;;;;;
;; String classification

 
(defn string-classification-error-function
  ;Finds the behaviors and errors of the individual.
  [argmap individual]
  (let [program (push-from-plushy (:plushy individual))
        inputs ["GCG" "GACAG" "AGAAG" "CCCA" "GATTACA" "TAGG" "GACT"]
        correct-outputs [false false false false true true true]
        outputs (map (fn [input]
                       (peek-stack
                        (interpret-program
                         program
                         (assoc empty-push-state :input {:in1 input})
                         (:step-limit argmap))
                        :boolean))
                     inputs)
        errors (map (fn [correct-output output]
                      (if (= output :no-stack-item)
                        1000000
                        (if (= correct-output output)
                          0
                          1)))
                    correct-outputs
                    outputs)]
    (assoc individual
           :behaviors outputs
           :errors errors
           :total-error (apply +' errors))))

(defn -main
  "Runs propel-gp, giving it a map of arguments."
  [& args]
  (binding [*ns* (the-ns 'propel.core)]
    (propel-gp (update-in (merge {:instructions default-instructions
                                  :error-function small-large-error-function
                                  :max-generations 500
                                  :population-size 200
                                  :max-initial-plushy-size 50
                                  :step-limit 100
                                  ;;:parent-selection :levenshtein1
                                  ;;:parent-selection :tournament
                                  :parent-selection :lexicase
                                  :tournament-size 5}
                                 (apply hash-map
                                        (map read-string args)))
                          [:error-function]
                          #(if (fn? %) % (eval %))))))