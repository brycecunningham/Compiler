# Compiler
Systems project (compiler, vm translater, and assembler)

A compiler which translates a Java-like language (Jack), according to its context-free grammar, from programming code into assembly
code and then into binary machine code using a recursive descent algorithm.

First run Compiler component on .jack file(s) to get .vm file. 

  - compile JackTokenizer.java, JackCompiler.java, SymbolTable.java, VMWriter.java, CompilationEngine.java
  - to run: "$ java JackCompiler source" where source is a .jack file or folder containing one or more .jack files
  
Then run VMTranslator component on .vm file(s) to get .asm file.

  - compile Parser.java, CodeWriter.java, VMTranslator.java
  - to run: "$ java VMTranslator source" where source is a .vm file or folder containing one or more .vm files
  
Finally, run Assembler component on .asm file to get output .hack file containing binary machine code

  - compile Code.java, Parser.java, SymbolTable.java, Assembler.java
  - to run: "$ java Assembler source" where source is a .asm file
