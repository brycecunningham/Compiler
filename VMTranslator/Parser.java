import java.io.*;


public class Parser {

	private BufferedReader br;
	private String currCommand;
	private static String arithCommands[] = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};

	public Parser(File inputfile) throws IOException {

		br = new BufferedReader(new FileReader(inputfile));

	}

	public boolean hasMoreCommands() throws IOException {
		
		while ((currCommand = br.readLine()) != null) {
			if (currCommand.contains("//"))
                currCommand = currCommand.substring(0, currCommand.indexOf("//"));
            currCommand = currCommand.trim();
            if (currCommand.length() != 0)
            	break;
        }
        return (currCommand != null) ? true : false;	
	}	

	public enum Command {
	
		C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
	
	}

	public Command commandType() {
		
		// first check for Command keyword
		if (currCommand.startsWith("push"))
			return Command.C_PUSH;
		if (currCommand.startsWith("pop"))
			return Command.C_POP;
		if (currCommand.startsWith("label"))
			return Command.C_LABEL;
		if (currCommand.startsWith("goto"))
			return Command.C_GOTO;
		if (currCommand.startsWith("if-goto"))
			return Command.C_IF;
		if (currCommand.startsWith("function"))
			return Command.C_FUNCTION;
		if (currCommand.startsWith("return"))
			return Command.C_RETURN;
		if (currCommand.startsWith("call"))
			return Command.C_CALL;

		// otherwise check for an arithmetic command by looping through the array 
		for (String a : arithCommands)
			if (currCommand.startsWith(a))
				return Command.C_ARITHMETIC;

		return null;
	}

	public String arg1() {	// assume won't be called if command type is C_RETURN

		// if arithmetic command, arg1 is just the command
		if (commandType() == Command.C_ARITHMETIC)
			return currCommand;

		// otherwise split the command by white space and take the 2nd token
		return currCommand.split(" ")[1];
	}

	public int arg2() {	// assume will only be called if command type is C_PUSH, C_POP, C_FUNCTION, or C_CALL

		// 3rd token
		return Integer.parseInt(currCommand.split(" ")[2]);
	}

	public void close() throws IOException {

		br.close();
	
	}
}
