import java.io.*;

// **** NEW NOTE: I finally figured out the problem with switching from a File Reader to a
// **** String Reader: the ready() method I was using in my hasMoreCommands() method wasn't
// **** working the same way it was for files, that is, it was returning true even when
// **** the getLine() method was null. To fix it, I streamlined. No more advance(). Instead
// **** I combined checking for more commands and reading each line in the hasMoreCommands()
// **** method. 




// **** My assembler worked fine before I added the strip white space section at the beginning.
// **** Before I added it I ran files through the separate project 0 program, then used the output
// **** of that as the input here. Once I added the white space code here I attempted to change
// **** the code to write to a string writer, and then wrap a string reader in the parser.
// **** Looks like I'm getting null pointer exceptions when I run it now. I'll keep working on it
// **** and if I figure it out I'll submit the fixed version ASAP. Thanks.



public class Assembler {

	public static void main(String[] args) throws IOException {

		String inputFilename = args[0];

		
		// strip white space code from project 0

		BufferedReader br = new BufferedReader(new FileReader(inputFilename));
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);

        String inputLine = null;
        String outputLine = null;


        while ((inputLine = br.readLine()) != null) {
            outputLine = inputLine.replaceAll("\\s", "");

            if (outputLine.contains("//"))
                outputLine = outputLine.substring(0, outputLine.indexOf("//"));

            bw.write(outputLine);
            if (outputLine.length() != 0) // this ensures that a blank line in input doesn't get copied in output
                bw.newLine();
        }
        bw.flush();
        String tempfile = sw.toString();
        br.close();
        bw.close();

        //System.out.println(tempfile);



		String filenamePrefix = inputFilename.substring(0, inputFilename.length() - 3); 	// input files are *.asm
		String outputFilename = filenamePrefix + "hack"; 
		BufferedWriter outputwriter = new BufferedWriter(new FileWriter(outputFilename));

		//System.out.println(tempfile);
		Parser p = new Parser(tempfile);
		Code c = new Code();
		SymbolTable symtable = new SymbolTable();

		
		// first pass to store labels to symbol table
		
		int romcount = 0;
		
		while (p.hasMoreCommands()) {
			//p.advance();
			//System.out.println(p.commandType());
			if (p.commandType() == Parser.Command.L_COMMAND) {
				String lsym = p.symbol();
				if (!symtable.contains(lsym))
					symtable.addEntry(lsym, romcount);
			} else {
				romcount++;										// only increment counter if it's C or A command
			}
		}
		p.close();
		
		p = new Parser(tempfile);							// files are small enough that creating new Parser (and buffered reader) is fine

		
		// second pass to parse commands and deal with variables

		int currRAM = 16;

		while (p.hasMoreCommands()) {
			//p.advance();
			String symstring = p.symbol();
			Parser.Command currcommand = p.commandType();			
			
			// deal with A commands
			if (currcommand == Parser.Command.A_COMMAND) {		
				if (symstring.matches("[0-9]+")) {				// if numeric then convert to binary string
					outputwriter.write(p.numericAtoBinString(symstring));
					outputwriter.newLine();
				} else {
					// add variables to symtable and increment next available RAM location
					if (!symtable.contains(symstring)) {
						symtable.addEntry(symstring, currRAM);
						outputwriter.write(p.numericAtoBinString(Integer.toString(currRAM)));
						outputwriter.newLine();
						currRAM++;								
					} else {
						// if variables are already in symtable just get the address and convert to binary string
						outputwriter.write(p.numericAtoBinString(Integer.toString(symtable.getAddress(symstring))));
						outputwriter.newLine();
					}
				}	
			} 

			// deal with C commands
			else if (currcommand == Parser.Command.C_COMMAND) {
				String cString = "111" + c.comp(p.comp()) + c.dest(p.dest()) + c.jump(p.jump());
				outputwriter.write(cString);
				outputwriter.newLine();
			} else {	
				// L command; these get erased in binary output file
			}
		}
		outputwriter.close();
		p.close();
	}


	
}