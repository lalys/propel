## Problem Description
For our problem, we chose to do small or large from the General Program Synthesis Benchmark Suite. We wanted to do something similar to the fizz buzz problem we went over in class to make sure we are understanding evolution. This is also something we've never done before so we wanted to keep it relatively simple so we weren't biting off more than we can chew. The problem was to print out "small" if a number was less than 1000, "" if it was between 1000 and 2000, and "large" if the number is greater than or equal to 2000.

## How we set up the problem
To set up the problem, we looked at the fizz buzz problem and used some ideas from it like the punishing extra characters fitness. Of course, we had to add our own constants and functions to make it work. We knew that we needed to give it small, large, and an empty string to get it to work. Instructions such as integer_to_string and string_drop were vital to make our problem work. 
Fitness:
``` clojure
(defn small-large
  [n]
  (cond
    (< n 1000) "small"
    (>= n 2000) "large"
    :else ""))
```

String Drop
``` clojure
(defn string_drop
  [state]
  (make-push-instruction state
                         #(apply str (drop %1 %2))
                         [:integer :string]
                         :string))
```

Integer to string
``` clojure
(defn integer_to_string
  [state]
  (make-push-instruction state str [:integer] :string))
```

## Results
Our first results weren't that good because we had our range set up to only have one small number which made the tests become filled with only large because it got lazy and had a low error score. To fix this we gave it more small inputs. For lexicase it would solve it the majority of the time, although it was random how many generations it would take. It seems like it had trouble learning to return nothing as an output causing generations to have the same error score for awhile. We tried to figure out why it did that but coulnd't figure out how to help it return nothing. Maybe we could punish it more for returning something when its supposed to return nothing. Lexicase selection is the best kind of selection for our problem.
```
-------------------------------------------------------
               Report for Generation 497
-------------------------------------------------------
Best plushy: ("abcdefghijklmnopqrstuvwxyz" string_= 2000 in1 "small" integer_- boolean_not string_take string_take -250 250 in1 -250 1250 string_includes? integer_+ "small" integer_- integer_+ string_drop 2250 250 exec_if string_reverse integer_= 750 3000 string_includes? 2500 1500 1250 "" "" integer_to_string "abcdefghijklmnopqrstuvwxyz" integer_to_string 3000 integer_* string_drop false "large" "large" 2000 exec_if exec_if)
Best program: ("abcdefghijklmnopqrstuvwxyz" string_= 2000 in1 "small" integer_- boolean_not string_take string_take -250 250 in1 -250 1250 string_includes? integer_+ "small" integer_- integer_+ string_drop 2250 250 exec_if (string_reverse integer_= 750 3000 string_includes? 2500 1500 1250 "" "" integer_to_string "abcdefghijklmnopqrstuvwxyz" integer_to_string 3000 integer_* string_drop false "large" "large" 2000 exec_if (exec_if () ()) ()) ())
Best total error: 0
Best errors: (0 0 0 0 0 0 0 0 0 0 0 0 0 0)
Best behaviors: (small small small small small     large large large large large)
```
The big gap in the middle is it returning nothing, which for those numbers is what we want.

```
-------------------------------------------------------
               Report for Generation 146
-------------------------------------------------------
Best plushy: (in1 string_= 1000 integer_+ string_length string_includes? "large" exec_if 0 string_length 250 "" string_= 1000 string_reverse exec_dup in1 string_includes? "large" exec_if 2250 250 string_take integer_- close string_drop in1 boolean_and boolean_= 1000 boolean_and "abcdefghijklmnopqrstuvwxyz" integer_- "small" integer_- string_take integer_-)
Best program: (in1 string_= 1000 integer_+ string_length string_includes? "large" exec_if (0 string_length 250 "" string_= 1000 string_reverse exec_dup (in1 string_includes? "large" exec_if (2250 250 string_take integer_-) (string_drop in1 boolean_and boolean_= 1000 boolean_and "abcdefghijklmnopqrstuvwxyz" integer_- "small" integer_- string_take integer_-))) ())
Best total error: 0
Best errors: (0 0 0 0 0 0 0 0 0 0 0 0 0 0)
Best behaviors: (small small small small small     large large large large large)
```

## What we tried
We got some largesmall and smalllarge prints which isn't what we want. In our constants we had string_concat which was causing the smalllarge and largesmall which we removed. We also tried to implement levenshtein distance but that didn't work out. 

We added negative numbers to see if it still works and it does. We also tried really big numbers and decimal numbers which worked too.