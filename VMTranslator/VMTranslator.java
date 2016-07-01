import java.io.*;
import java.util.*;

// Project 8 VMTranslator

public class VMTranslator {

	public static void main(String[] args) throws IOException {

		File source = new File(args[0]);
		List<File> inputfiles = new ArrayList<File>();
		File outputfile;

		
		// source is either a directory of files
		if (source.isDirectory()) {
			for (File f : source.listFiles())
				if (f.getName().endsWith(".vm")) {
					inputfiles.add(f);
				}
			outputfile = new File(source + "/" + source.getName() + ".asm");
		
		// or a .vm file
		} else {	
			inputfiles.add(source);
			String inputstring = source.getPath();
			String fileprefix = inputstring.substring(0, inputstring.length() - 3);
			outputfile = new File(fileprefix + ".asm");
		}
	
		// one codewriter to handle all input files
		CodeWriter cw = new CodeWriter(outputfile);

		cw.writeInit();

		for (File f : inputfiles) {
			// a different parser for each input file
			Parser p = new Parser(f);
			cw.setFileName(f.getName());

			while (p.hasMoreCommands()) {						// hasMoreCommands also advances the line and parses comments and blank lines
				Parser.Command cmdtype = p.commandType();
				switch (cmdtype) {
					case C_ARITHMETIC:	cw.writeArithmetic(p.arg1()); 
										break;
					case C_PUSH:		cw.writePushPop(cmdtype, p.arg1(), p.arg2()); 
										break;
					case C_POP:			cw.writePushPop(cmdtype, p.arg1(), p.arg2()); 
										break;
					case C_LABEL:		cw.writeLabel(p.arg1()); 
										break;
					case C_GOTO:		cw.writeGoto(p.arg1()); 
										break;
					case C_IF:			cw.writeIf(p.arg1()); 
										break;
					case C_CALL:		cw.writeCall(p.arg1(), p.arg2());
										break;
					case C_FUNCTION:	cw.writeFunction(p.arg1(), p.arg2());
										break;
					case C_RETURN:		cw.writeReturn();
										break;
					default:

				}

			}
			p.close();
		}
		cw.close();
	}
}