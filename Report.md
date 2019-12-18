## Description
For our problem, we chose to do small or large from the General Program Synthesis Benchmark Suite. We wanted to do something similar to the fizz buzz problem that we went over in class to make sure that we are understanding it. This is also something we've never done before. The problem was to print out "small" if a number was less than 1000, "" if it was between 1000 and 2000, and "large" if the number is greater than or equal to 2000.

## How we set up the problem
To set up the problem, we looked at the fizz buzz problem and used some ideas from it like the extra characters. Of course, we had to add our own constants and functions to make it work. We knew that we needed small, large, and an empty string to get it to work. Instructions such as integer_to_string and string_drop were vital to make our problem work. 
Fitness:
(defn small-large
  [n]
  (cond
    (< n 1000) "small"
    (>= n 2000) "large"
    :else ""))

## Results
Ours first results weren't that good because we had our range set up to only have one small number which made the tests become filled with only large because it got lazy and had a low error score for small. For lexicase it would solve it would solve the majority of the time and it took about 350 generations. It seems like it had trouble returning nothing as an output causing generations to be the same for awhile. We tried to figure out why it did that but coulnd't figure out how to help it return nothing. We still don't know why. Lexicase is the best kind of selection from what we could tell.


## What we tried
We got some largesmall and smalllarge prints which isn't what we want. In our constants we had string_concat which was causing the smalllarge and largesmall which we removed. We also tried to implement levenshtein distance but that didn't work out. 

We added negative numbers to see if it still works and it does. We also tried really big numbers and decimal numbers which worked too. 
 


