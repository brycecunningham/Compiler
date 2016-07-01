import java.io.*;


public class Parser {

	BufferedReader br;
	private String currCommand;

	public Parser(String inputfile) throws IOException {

		br = new BufferedReader(new StringReader(inputfile));
	
	}

	public boolean hasMoreCommands() throws IOException {
		return (currCommand = br.readLine()) != null;
	}	

	//public void advance() throws IOException {
		//currCommand = br.readLine();
	//}

	public enum Command {
		A_COMMAND, C_COMMAND, L_COMMAND
	}

	public Command commandType() {
		if (currCommand.charAt(0) == '@')
			return Command.A_COMMAND;
		if (currCommand.charAt(0) == '(')
			return Command.L_COMMAND;
		return Command.C_COMMAND;
	}

	public String symbol() { 
		if (commandType() == Command.L_COMMAND) {
			return currCommand.substring(1,currCommand.length()-1);
		} else {														// current command is A_COMMAND
			return currCommand.substring(1);
		}
	}
	
	public String dest() {
		return (currCommand.indexOf('=') == -1) ? "" : currCommand.substring(0,currCommand.indexOf('='));	// left of '=' is the destination
	}																										// null when no '='

	public String comp() {
		int leftlimit = currCommand.indexOf('=');
		int rightlimit = currCommand.indexOf(';');
		if ((leftlimit == -1) && (rightlimit == -1)) {		// a command like -1 where there's no '=' or ';'. (Not sure if this would ever happen)
			return currCommand;
		} else if (leftlimit == -1) {						// a command like D;JGT where there's no '=' but there is a ';'
			return currCommand.substring(0,rightlimit);
		} else if (rightlimit == -1) {						// a command like M=1 where there's no ';' but there is a '='
			return currCommand.substring(leftlimit+1);
		} else {											// a command like D=M;JGT where there's both a '=' and a ';'
			return currCommand.substring(leftlimit+1, rightlimit);
		}
	}

	public String jump() {
		return (currCommand.indexOf(';') == -1) ? "" : currCommand.substring(currCommand.indexOf(';')+1);		// right of ';' is jump
	}	

	public String numericAtoBinString(String s) {
		int numsymbol = Integer.parseInt(s);
		return String.format("%16s", Integer.toBinaryString(numsymbol)).replace(' ','0');
	}

	public void close() throws IOException {
		br.close();
	}																										// if no ';' then null
}