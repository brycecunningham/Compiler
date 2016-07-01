import java.io.*;
import java.util.*;

// Project 8 CodeWriter

public class CodeWriter {

	private BufferedWriter bw;
	private String currfile;
	private int jumpflag = 1;		// append flag to jump labels to distinguish between each set of assembly commands
	private int retaddflag = 1;		// same for return address labels
	//private List<String> functions = new ArrayList<String>();	// keep track of which function name we're on for label naming
	//private int funcindex = 0;

	public CodeWriter(File outputfile) throws IOException {

		bw = new BufferedWriter(new FileWriter(outputfile));

	}

	public void setFileName (String filename) {
		currfile = filename;
	}

	public void writeArithmetic(String command) throws IOException {

		switch (command) {
			case "add": bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"M=M+D\n");
						break;
			case "sub": bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"M=M-D\n");
						break;
			case "and": bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"M=M&D\n");
						break;
			case "or": 	bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"M=M|D\n");
						break;	
			case "neg": bw.write(	"@SP\n" +
									"A=M-1\n" +
									"M=-M\n");
						break;
			case "not": bw.write(	"@SP\n" +
									"A=M-1\n" +
									"M=!M\n");
						break;
			case "gt": 	bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"D=M-D\n" +
									"@FALSE" + jumpflag + "\n" +
									"D;JLE\n" +
									"@SP\n" +
									"A=M-1\n" +
									"M=-1\n" +
									"@CONTINUE" + jumpflag + "\n" +
									"0;JMP\n" +
									"(FALSE" + jumpflag + ")\n" +
									"@SP\n" +
									"A=M-1\n" +
									"M=0\n" +
									"(CONTINUE" + jumpflag + ")\n");
						jumpflag++;										// increment flag so different labels the next time gt, lt, or eq is called
						break;
			case "lt": 	bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"D=M-D\n" +
									"@FALSE" + jumpflag + "\n" +
									"D;JGE\n" +
									"@SP\n" +
									"A=M-1\n" +
									"M=-1\n" +
									"@CONTINUE" + jumpflag + "\n" +
									"0;JMP\n" +
									"(FALSE" + jumpflag + ")\n" +
									"@SP\n" +
									"A=M-1\n" +
									"M=0\n" +
									"(CONTINUE" + jumpflag + ")\n");
						jumpflag++;
						break; 
			case "eq": 	bw.write(	"@SP\n" +
									"AM=M-1\n" +
									"D=M\n" +
									"A=A-1\n" +
									"D=M-D\n" +
									"@FALSE" + jumpflag + "\n" +
									"D;JNE\n" +
									"@SP\n" +
									"A=M-1\n" +
									"M=-1\n" +
									"@CONTINUE" + jumpflag + "\n" +
									"0;JMP\n" +
									"(FALSE" + jumpflag + ")\n" +
									"@SP\n" +
									"A=M-1\n" +
									"M=0\n" +
									"(CONTINUE" + jumpflag + ")\n");
						jumpflag++;
						break;
			default: bw.write("");
		}
	}

	public void writePushPop(Parser.Command cmdtype, String segment, int index) throws IOException {
	
		// handle push commands

		if (cmdtype == Parser.Command.C_PUSH) {	
			
			switch (segment) {
				
				case "constant":	bw.write(	"@" + index + "\n" +
												"D=A\n" +
												"@SP\n" +
												"A=M\n" +
												"M=D\n" +
												"@SP\n" +
												"M=M+1\n");
									break;
				case "local":		pushPointerContent("LCL", index);	
									break;
				case "argument":	pushPointerContent("ARG", index);
									break;
				case "this":		pushPointerContent("THIS", index);
									break;
				case "that":		pushPointerContent("THAT", index);
									break;
				case "static":		pushSymbolContent(currfile + "." + index);
									break;
				case "pointer":		pushSymbolContent(Integer.toString(3 + index));  // pointer accesses RAM 3+i (i is 0 or 1)
									break;
				case "temp":		pushSymbolContent(Integer.toString(5 + index));  // temp accesses RAM 5+i (i is 0 - 7)
									break;
				default:
			}
		
		// handle pop commands

		} else if (cmdtype == Parser.Command.C_POP) {
	
			switch (segment) {
				
				case "local":		popIntoPointer("LCL", index);	
									break;
				case "argument":	popIntoPointer("ARG", index);
									break;
				case "this":		popIntoPointer("THIS", index);
									break;
				case "that":		popIntoPointer("THAT", index);
									break;
				case "static":		popIntoSymbol(currfile + "." + index);
									break;
				case "pointer":		popIntoSymbol(Integer.toString(3 + index));
									break;
				case "temp":		popIntoSymbol(Integer.toString(5 + index));
									break;
				default:
			}		
		}
	}

	public void writeInit() throws IOException {
		bw.write(	"@256\n" +
					"D=A\n" +
					"@SP\n" +
					"M=D\n");		// set SP to map to RAM[256]
		writeCall("Sys.init", 0);	// call Sys.init
	}

	public void writeLabel(String label) throws IOException {
		/*if (funcindex != 0) {
			bw.write("(" + functions.get(funcindex-1) + "$" + label + ")\n");
		} else {
			bw.write("(" + label + ")\n");
		}*/
		bw.write("(" + label + ")\n");
	}

	public void writeGoto(String label) throws IOException {
		/*String templabel;
		if (funcindex !=0) {
			templabel = functions.get(funcindex-1) + "$" + label;
		} else {
			templabel = label;
		}*/
		
		bw.write(	"@" + label + "\n" +
					"0;JMP\n");
	}

	public void writeIf(String label) throws IOException {
		/*String templabel;
		if (funcindex !=0) {
			templabel = functions.get(funcindex-1) + "$" + label;
		} else {
			templabel = label;
		}	*/	

		bw.write(	"@SP\n" +			// pop top element off stack
					"AM=M-1\n" +
					"D=M\n" +
					"@" + label + "\n" +
					"D;JNE\n");			// jump if it's not 0
	}

	public void writeCall(String functionName, int numArgs) throws IOException {
		
		bw.write(	"@RETURN-ADDRESS" + retaddflag + "\n" +		// push return-address onto the stack
					"D=A\n" +
					"@SP\n" +
					"A=M\n" +
					"M=D\n" +
					"@SP\n" +
					"M=M+1\n");

		pushSymbolContent("LCL");	// push LCL (not RAM[LCL])
		pushSymbolContent("ARG");	// push ARG
		pushSymbolContent("THIS");	// push THIS
		pushSymbolContent("THAT");	// push THAT

		bw.write(	"@SP\n" +
					"D=M\n" +
					"@LCL\n" +			
					"M=D\n" +								// LCL = SP
					"@" + numArgs + "\n" +
					"D=D-A\n" +
					"@5\n" +
					"D=D-A\n" +
					"@ARG\n" +
					"M=D\n" +								// ARG = SP - numArgs - 5
					"@" + functionName + "\n" +	
					"0;JMP\n" +								// goto functionName
					"(RETURN-ADDRESS" + retaddflag + ")\n");
		retaddflag++;
	}

	public void writeFunction(String functionName, int numLocals) throws IOException {
		
		//functions.add(functionName);
		//funcindex++;

		bw.write("(" + functionName + ")\n");

		for (int i = 0; i < numLocals; i++)						
			writePushPop(Parser.Command.C_PUSH, "constant", 0);		// initialize each of the function's local variables to 0 on the stack
	}

	public void writeReturn() throws IOException {
		bw.write(	"@LCL\n" +
					"D=M\n" +
					"@R11\n" +
					"M=D\n" +		// FRAME = LCL	(used R5 for FRAME)
					"@5\n" +
					"A=D-A\n" +
					"D=M\n" +
					"@R12\n" +
					"M=D\n");		// RET = *(FRAME - 5)	(used R6 for RET)
		
		popIntoPointer("ARG", 0);	// *ARG = pop()

		bw.write(	"@ARG\n" +
					"D=M\n" +
					"@SP\n" +
					"M=D+1\n" +		// SP = ARG + 1
					"@R11\n" +
					"D=M\n" +
					"AM=D-1\n" +	// decrement the value of FRAME to reduce number of steps in the (FRAME - 2), (FRAME - 3),
					"D=M\n" +		// (FRAME - 4) parts to follow. this is fine since FRAME is only a temporary variable
					"@THAT\n" +
					"M=D\n" +		// THAT = *(FRAME - 1)
					"@R11\n" +
					"D=M\n" +
					"AM=D-1\n" +
					"D=M\n" +
					"@THIS\n" +
					"M=D\n" +		// THIS = *(FRAME - 2)
					"@R11\n" +
					"D=M\n" +
					"AM=D-1\n" +
					"D=M\n" +
					"@ARG\n" +
					"M=D\n" +		// ARG = *(FRAME - 3)
					"@R11\n" +
					"D=M\n" +
					"AM=D-1\n" +
					"D=M\n" +
					"@LCL\n" +
					"M=D\n" +		// LCL = *(FRAME - 4)
					"@R12\n" +
					"A=M\n" +
					"0;JMP\n");		// goto RET (return address stored in R6)
		//funcindex--;
	}

	// helper functions for pushing and popping

	public void pushPointerContent(String segment, int index) throws IOException {
		bw.write(	"@" + segment + "\n" +
					"D=M\n" +
					"@" + index + "\n" +
					"A=D+A\n" +
					"D=M\n" +
					"@SP\n" +
					"A=M\n" +
					"M=D\n" +
					"@SP\n" +
					"M=M+1\n");	
	}

	public void popIntoPointer(String segment, int index) throws IOException {
		bw.write(	"@" + segment + "\n" +
					"D=M\n" +
					"@" + index + "\n" +
					"D=D+A\n" +
					"@R13\n" +
					"M=D\n" +
					"@SP\n" +
					"AM=M-1\n" +
					"D=M\n" +
					"@R13\n" +
					"A=M\n" +
					"M=D\n");
	}

	public void pushSymbolContent(String segment) throws IOException {
		bw.write(	"@" + segment + "\n" +
					"D=M\n" +
					"@SP\n" +
					"A=M\n" +
					"M=D\n" +
					"@SP\n" +
					"M=M+1\n");
	}

	public void popIntoSymbol(String segment) throws IOException {
		bw.write(	"@SP\n" +
					"AM=M-1\n" +
					"D=M\n" +
					"@" + segment + "\n" +
					"M=D\n");	
	}

	public void close() throws IOException {

		bw.close();

	}	
}
