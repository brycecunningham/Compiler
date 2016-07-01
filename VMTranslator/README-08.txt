Bryce Cunningham    Project 8

To compile:

javac Parser.java
javac CodeWriter.java
javac VMTranslator.java

To run:

java VMTranslator source

where source is either a .vm file or a folder containing 1 or more .vm files.

One .asm file will be outputted to the folder where the input source is located.



Miscellaneous notes:


I initially got issues with stray space and tabs that weren't easily noticeable screwing up the parsing. To fix this, I trimmed each command
as it was read in hasMoreCommands.

I added a list to store each function name as it was called in order to name labels correctly (fn$label), but even with getting rid
of a function at every return command this method wasn't completely working correctly (ex: function b .... return .... then another return later
in the function). I didn't solve this, so I just commented all of it out. I was still able to pass the tests.