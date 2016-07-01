import java.io.*;


public class Assembler {

	public static void main(String[] args) throws IOException {

		String inputFilename = args[0];

		
		// strip white space

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

        

		String filenamePrefix = inputFilename.substring(0, inputFilename.length() - 3); 	// input files are *.asm
		String outputFilename = filenamePrefix + "hack"; 
		BufferedWriter outputwriter = new BufferedWriter(new FileWriter(outputFilename));

		Parser p = new Parser(tempfile);
		Code c = new Code();
		SymbolTable symtable = new SymbolTable();

		
		// first pass to store labels to symbol table
		
		int romcount = 0;
		
		while (p.hasMoreCommands()) {
			if (p.commandType() == Parser.Command.L_COMMAND) {
				String lsym = p.symbol();
				if (!symtable.contains(lsym))
					symtable.addEntry(lsym, romcount);
			} else {
				romcount++;										// only increment counter if it's C or A command
			}
		}
		p.close();
		
		p = new Parser(tempfile);	

		
		// second pass to parse commands and deal with variables

		int currRAM = 16;

		while (p.hasMoreCommands()) {
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
