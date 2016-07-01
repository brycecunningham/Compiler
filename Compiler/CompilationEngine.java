import java.io.*;
import java.util.*;

public class CompilationEngine {

	private JackTokenizer jt;
	private VMWriter vmw;
	private SymbolTable symTable;
	private String className;
	private String currSubroutineName;
	private String currSubroutineKind;
	private String currSubroutineType;
	private int ifIndex = 0;
	private int whileIndex = 0;

	public CompilationEngine(JackTokenizer _jt, File outputfile) throws IOException {

		jt = _jt;
		vmw = new VMWriter(outputfile);
		symTable = new SymbolTable();

	}

	public void compileClass() throws IOException {
		
		jt.advance(); // class
		jt.advance(); // class name
		
		className = jt.getCurrToken();

		jt.advance();

		compileClassVarDec();
		compileSubroutine();
		
		jt.advance();

		jt.close();
		vmw.close();
	}

	public void compileClassVarDec() throws IOException {

		jt.advance();
		
		// no classVarDec or subroutineDec
		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '}') {
			jt.goBack();
			return;
		}

		// no classVarDec; next segment is subroutineDec
		if (jt.tokenType() == JackTokenizer.Token.KEYWORD && (jt.keyWord() == JackTokenizer.Keyword.CONSTRUCTOR || 
			jt.keyWord() == JackTokenizer.Keyword.FUNCTION || jt.keyWord() == JackTokenizer.Keyword.METHOD)) {
			jt.goBack();
			return;
		}

		String kind = jt.getCurrToken();	// static or field

		jt.advance();

		String type = jt.getCurrToken();	// class name, int, char, or boolean

		jt.advance();

		String name = jt.getCurrToken();	// varName

		symTable.define(name, type, kind);

		jt.advance();

		// handles additional variables separated by commas
		while (true) {

			if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == ';')
				break;
			
			jt.advance();
			
			name = jt.getCurrToken();
			symTable.define(name, type, kind);	// add'l vars
			
			jt.advance();
		}

		compileClassVarDec();

	}

	public void compileSubroutine() throws IOException {

		jt.advance();

		// no subroutineDec
		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '}') {
			jt.goBack();
			return;
		}

		symTable.startSubroutine();	// reset subroutine scope symbol table

		currSubroutineKind = jt.getCurrToken();	// constructor, function, or method

		// store "this" in symbol table as first argument for subroutine methods
		if (currSubroutineKind.equals("method"))
			symTable.define("this", className, "argument");
		jt.advance();

		currSubroutineType = jt.getCurrToken(); // class name, void, int, char, or boolean

		jt.advance();

		currSubroutineName = jt.identifier();	// subroutine name

		jt.advance();

		compileParameterList();

		jt.advance();
		
		// subroutine body
		jt.advance();
		
		compileVarDec();

		// write function vm code once local variables have been added to subscope symbol table in compileVarDec()
		vmw.writeFunction(className + "." + currSubroutineName, symTable.varCount("var"));

		// For constructors, Memory.alloc must be called to set "this" pointer. Size of object for .alloc's
		// arg is number of instance variables (fields).
		if (currSubroutineKind.equals("constructor")) {
			vmw.writePush("constant", symTable.varCount("field"));
			vmw.writeCall("Memory.alloc", 1);
			vmw.writePop("pointer", 0);
		}
		// For methods, "this" pointer must be set
		if (currSubroutineKind.equals("method")) {
			vmw.writePush("argument", 0);
			vmw.writePop("pointer", 0);
		}
		
		compileStatements();
		
		jt.advance();

		compileSubroutine();

	}

	public void compileParameterList() throws IOException {

		jt.advance();

		// no parameters
		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == ')') {
			jt.goBack();
			return;
		}
		
		// handles all params
		while (true) {

			String type = jt.getCurrToken(); // class name, int, char, or boolean

			jt.advance();

			String name = jt.getCurrToken(); // var name

			// each parameter in a subroutine is stored in the subscope symbol table
			symTable.define(name, type, "argument");

			jt.advance();
			
			// no more items in param list
			if (jt.tokenType() == JackTokenizer.Token.SYMBOL && (jt.symbol() != ',')) {
				jt.goBack();
				break;
			} else {
				jt.advance();
			}			

		}

	}

	public void compileVarDec() throws IOException {

		jt.advance();

		// no varDec
		if (jt.keyWord() != JackTokenizer.Keyword.VAR) {
			jt.goBack();
			return;
		}

		jt.advance();
		
		String type = jt.getCurrToken();	// class name, int, char, or boolean

		jt.advance();

		String name = jt.getCurrToken();	// var name

		// add local variables to subscope symbol table
		symTable.define(name, type, "var");

		jt.advance();

		// handles additional variables separated by commas
		while (true) {

			if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == ';')
				break;

			jt.advance();

			name = jt.getCurrToken();	// add'l var names

			symTable.define(name, type, "var");

			jt.advance();
		}

		compileVarDec();

	}

	public void compileStatements() throws IOException {

		jt.advance();

		// no statements
		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '}') {
			jt.goBack();
			return;
		}	

		if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.LET) {
			compileLet();
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.IF) {
			compileIf();
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.WHILE) {
			compileWhile();
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.DO) {
			compileDo();
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.RETURN) {
			compileReturn();
		}

		compileStatements();

	}

	public void compileLet() throws IOException {

		jt.advance();

		String varname = jt.getCurrToken();
		String varkind = symTable.kindOf(varname);
		String varsegment;
		switch (varkind) {
			case "static": varsegment = "static"; break;
			case "field": varsegment = "this"; break;
			case "argument": varsegment = "argument"; break;
			case "var": varsegment = "local"; break;
			default: varsegment = "";
		}
		int varindex = symTable.indexOf(varname);
		jt.advance();	

		//	if varname[expression]
		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '[') {

			// push base of the array to the stack
			vmw.writePush(varsegment, varindex);

			compileExpression();

			vmw.writeArithmetic("add"); // add array offset to base

			jt.advance();
			jt.advance();

			// don't pop the target array address into pointer yet in case expression needs it
			compileExpression();

			// pop expression result into temp and then pop address into pointer
			vmw.writePop("temp", 0);
			vmw.writePop("pointer", 1);
			vmw.writePush("temp", 0);
			vmw.writePop("that", 0);

			jt.advance();
		
		//  no array, just a variable
		} else {

			compileExpression();

			// pop expresssion result directly into variable
			vmw.writePop(varsegment, varindex);

			jt.advance();

		}

	}

	public void compileIf() throws IOException {

		ifIndex++;
		int tempIndex = ifIndex;	// temp variable to prevent index issues after compileStatements() command below

		jt.advance();

		compileExpression();

		jt.advance();
		jt.advance();

		// if ~(cond) go to "else"
		vmw.writeArithmetic("not");
		vmw.writeIf("IF-ELSE" + tempIndex);

		compileStatements();

		vmw.writeGoto("IF-END" + tempIndex);

		jt.advance();
		jt.advance();

		vmw.writeLabel("IF-ELSE" + tempIndex);

		//	if else{statements}
		if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.ELSE) {

			jt.advance();

			compileStatements();

			jt.advance();
			jt.advance();
		}
		jt.goBack();

		vmw.writeLabel("IF-END" + tempIndex);

	}

	public void compileWhile() throws IOException {

		whileIndex++;
		int tempIndex = whileIndex;	// temp variable to prevent index issues after compileStatements() command below
		vmw.writeLabel("WHILE" + tempIndex);

		jt.advance();

		compileExpression();

		// if ~(cond) go to end
		vmw.writeArithmetic("not");
		vmw.writeIf("WHILE-END" + tempIndex);

		jt.advance();	
		jt.advance();

		compileStatements();

		jt.advance();

		vmw.writeGoto("WHILE" + tempIndex);

		vmw.writeLabel("WHILE-END" + tempIndex);

	}

	public void compileDo() throws IOException {

		jt.advance();
		
		String name = jt.getCurrToken();	// subroutine, class, or var name

		jt.advance();

		int nArgs = 0;

		// subroutineName(expressionList)
		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '(') {

			// push "this" pointer onto the stack
			vmw.writePush("pointer", 0);
			
			// nArgs is number of args from expression list plus 1 for the object pointer
			nArgs = compileExpressionList() + 1;	

			vmw.writeCall(className + "." + name, nArgs);

			jt.advance();

		} 

		//	className/varName.subroutineName(expressionList)
		else {

			jt.advance();

			String subname = jt.getCurrToken();	// subroutine name

			// look for varName in the symbol table
			if (symTable.typeOf(name) != null) {
				
				String varsegment;
				switch (symTable.kindOf(name)) {
					case "static": varsegment = "static"; break;
					case "field": varsegment = "this"; break;
					case "argument": varsegment = "argument"; break;
					case "var": varsegment = "local"; break;
					default: varsegment = "";
				}
				// push the variable onto the stack to be used for the subsequent method call
				vmw.writePush(varsegment, symTable.indexOf(name));

				jt.advance();

				// number of args from expression list plust 1 for object pointer of the variable
				nArgs = compileExpressionList() + 1;

				jt.advance();

				vmw.writeCall(symTable.typeOf(name) + "." + subname, nArgs);

			// if name not found in the symbol table it must be a class
			} else {
				
				jt.advance();

				// nArgs doesn't add 1 since this isn't a method acting on a specific object
				nArgs = compileExpressionList();

				jt.advance();

				vmw.writeCall(name + "." + subname, nArgs);

			}

		}

		// pop return value into temp
		vmw.writePop("temp", 0);

	}

	public void compileReturn() throws IOException {

		jt.advance();		

		if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == ';') {
			
			// no expression so push 0 onto the stack
			vmw.writePush("constant", 0);

		} else {

			jt.goBack();
			compileExpression();
			jt.advance();
		}

		vmw.writeReturn();

	}

	public void compileExpression() throws IOException {

		compileTerm();

		jt.advance();

		// additional (op term)s
		while (jt.tokenType() == JackTokenizer.Token.SYMBOL && (jt.symbol() == '+' || jt.symbol() == '-' || 
																jt.symbol() == '*' || jt.symbol() == '/' ||
																jt.symbol() == '&' || jt.symbol() == '|' ||
																jt.symbol() == '<' || jt.symbol() == '>' ||
																jt.symbol() == '=')) {
			char symbol = jt.symbol();

			compileTerm();

			switch (symbol) {
				case '+':	vmw.writeArithmetic("add"); break;
				case '-':	vmw.writeArithmetic("sub"); break;
				case '*':	vmw.writeCall("Math.multiply", 2); break;
				case '/':	vmw.writeCall("Math.divide", 2); break;
				case '&':	vmw.writeArithmetic("and"); break;
				case '|':	vmw.writeArithmetic("or"); break;
				case '<':	vmw.writeArithmetic("lt"); break;
				case '>':	vmw.writeArithmetic("gt"); break;
				case '=':	vmw.writeArithmetic("eq"); break;
				default:
			}
			jt.advance();
		}
		jt.goBack();

	}

	public void compileTerm() throws IOException {

		jt.advance();

		if (jt.tokenType() == JackTokenizer.Token.INT_CONST) {	// int constant
			vmw.writePush("constant", jt.intVal());
		} else if (jt.tokenType() == JackTokenizer.Token.STRING_CONST) {	// string constant
			// must use String.new and String.appendChar
			String temp = jt.stringVal();
			vmw.writePush("constant", temp.length());
			vmw.writeCall("String.new", 1);
			for (int i = 0; i<temp.length(); i++) {
				vmw.writePush("constant", (int)temp.charAt(i));
				vmw.writeCall("String.appendChar", 2);
			}
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.TRUE) {	// keyword constant
			vmw.writePush("constant", 0);
			vmw.writeArithmetic("not");	// anything not 0 is true
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.FALSE) {
			vmw.writePush("constant", 0);
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.NULL) {
			vmw.writePush("constant", 0);
		} else if (jt.tokenType() == JackTokenizer.Token.KEYWORD && jt.keyWord() == JackTokenizer.Keyword.THIS) {
			vmw.writePush("pointer", 0);
		} else if (jt.tokenType() == JackTokenizer.Token.IDENTIFIER) {
			jt.advance();
			if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '[') {
				jt.goBack();
				
				String name = jt.getCurrToken();	// varname[expression]

				jt.advance();
				
				String varsegment;
				switch (symTable.kindOf(name)) {
					case "static": varsegment = "static"; break;
					case "field": varsegment = "this"; break;
					case "argument": varsegment = "argument"; break;
					case "var": varsegment = "local"; break;
					default: varsegment = "";
				}

				// push base address of array onto stack
				vmw.writePush(varsegment, symTable.indexOf(name));

				compileExpression();

				// add base address and offset from above expression and set pointer to it
				vmw.writeArithmetic("add");
				vmw.writePop("pointer", 1);
				vmw.writePush("that", 0);

				jt.advance();

			} else if (jt.tokenType() == JackTokenizer.Token.SYMBOL && (jt.symbol() == '(' || jt.symbol() == '.')) {
				jt.goBack();
				
				String name = jt.getCurrToken();

				jt.advance();

				int nArgs = 0;
				
				// subroutineName(expressionList)
				if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '(') {

					// push "this" pointer onto the stack
					vmw.writePush("pointer", 0);
					
					// nArgs is number of args from expression list plus 1 for the object pointer
					nArgs = compileExpressionList() + 1;	

					vmw.writeCall(className + "." + name, nArgs);

				} else {

					jt.advance();

					String subname = jt.getCurrToken();	// subroutine name

					// look for varName in the symbol table
					if (symTable.typeOf(name) != null) {
						
						String varsegment;
						switch (symTable.kindOf(name)) {
							case "static": varsegment = "static"; break;
							case "field": varsegment = "this"; break;
							case "argument": varsegment = "argument"; break;
							case "var": varsegment = "local"; break;
							default: varsegment = "";
						}
						// push the variable onto the stack to be used for the subsequent method call
						vmw.writePush(varsegment, symTable.indexOf(name));

						jt.advance();

						// number of args from expression list plust 1 for object pointer of the variable
						nArgs = compileExpressionList() + 1;

						vmw.writeCall(symTable.typeOf(name) + "." + subname, nArgs);

					// if name not found in the symbol table it must be a class
					} else {
						
						jt.advance();

						// nArgs doesn't add 1 since this isn't a method acting on a specific object
						nArgs = compileExpressionList();

						vmw.writeCall(name + "." + subname, nArgs);

					}

				}

			} else {

				jt.goBack();
				String name = jt.getCurrToken();	// varname
				String varsegment;
				switch (symTable.kindOf(name)) {
					case "static": varsegment = "static"; break;
					case "field": varsegment = "this"; break;
					case "argument": varsegment = "argument"; break;
					case "var": varsegment = "local"; break;
					default: varsegment = "";
				}
				vmw.writePush(varsegment, symTable.indexOf(name));

			}
		
		} else if (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() == '(') {

			compileExpression();
			jt.advance();

		} else {	// unary op term

			char c = jt.symbol();	// '-' or '~'

			compileTerm();

			vmw.writeArithmetic((c=='-') ? "neg" : "not");

		}

	}

	public int compileExpressionList() throws IOException {
		// made this function return number of args for use in compileDo() compileTerm()
		int nArgs = 0;
		jt.advance();

		// expression list not empty
		if (jt.tokenType() != JackTokenizer.Token.SYMBOL || jt.symbol() != ')') {
			jt.goBack();
			compileExpression();
			nArgs++;
			jt.advance();

			// if additional expressions
			while (jt.tokenType() == JackTokenizer.Token.SYMBOL && jt.symbol() != ')') {
				compileExpression();
				nArgs++;
				jt.advance();
			}
		}


		return nArgs;

	}

}
