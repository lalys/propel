## Description
For our problem, we chose to do small or large from the General Program Synthesis Benchmark Suite. We wanted to do something similar to the fizz buzz problem that we went over in class to make sure that we are understanding it. This is also something we've never done before.

## How we set up the problem
To set up the problem, we looked at the fizz buzz problem and used some ideas from it like the extra characters. Of course, we had to add our own constants and functions to make it work. 

## Results
Ours first results weren't that good because we had our range set up to only have one small number which made the tests become filled with only large. Sometimes it would solve it but other times it wouldn't and it took about 350 generations. It seems like it had trouble returning nothing as an output causing generations to be the same for awhile. We tried to figure out why it coulnd't figure out how to return nothing but we still don't know why. 


## What we tried
We got some largesmall and smalllarge prints which isn't what we want. In our constants we had string_concat which was causing the smalllarge and largesmall which we removed. We also tried to implement levenshtein distance but it doesn't work. 

We added negative numbers to see if it still works and it does. We also tried really big numbers and decimal numbers which worked too. 
 


