Bryce Cunningham		Project 11


To compile:

	javac JackTokenizer.java
	javac JackCompiler.java
	javac SymbolTable.java
	javac VMWriter.java
	javac CompilationEngine.java

To run:

	java JackCompiler source

	where source is either one .jack file or a folder containing one or more .jack files.

This program takes each .jack file as input and outputs a corresponding .vm file (e.g. Square.vm for Square.jack)


To test, copy all provided Jack OS files to target folder before running the compiler. These files are called at
various points throughout the program.