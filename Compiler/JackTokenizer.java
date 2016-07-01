import java.io.*;
import java.util.*;

public class JackTokenizer {

	private BufferedReader br;
	private String currCommand;
	private String currToken;
	private String commandTokens[];
	private int tokenIndex;
	private List<String> tokens;

	private int strTokenInc;
	private boolean justAdvanced;

	private static String keywords[] = 	{"class", "constructor", "function", "method", "field", "static", "var", "int", "char",
										"boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return"};

	private static String symbols[] = 	{"{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~"};

	

	public JackTokenizer(File inputfile) throws IOException {

		br = new BufferedReader(new FileReader(inputfile));

		tokens = new ArrayList<String>();

		// fill list with tokens

		boolean blockcomment = false;

		while ((currCommand = br.readLine()) != null) {

			if (blockcomment) {	// if beginning of block comment was seen on an earlier line, look for ending
				if (currCommand.indexOf("*/") == -1)
					continue;
				currCommand = currCommand.substring(currCommand.indexOf("*/")+2);
				blockcomment = false;
			}
			
			if (currCommand.contains("//"))
            	currCommand = currCommand.substring(0, currCommand.indexOf("//"));
            
            // checks for beginning of block comment
            if (currCommand.contains("/*")) {
            	if (currCommand.contains("*/")) {	// if entire block comment is on the same line
            		int begin = currCommand.indexOf("/*");
            		int end = currCommand.indexOf("*/");
            		if (end - begin >= 2)	// ensures correct ordering of block comment indicators
            			currCommand = currCommand.substring(0, begin) + currCommand.substring(end+2);
            	} else {
            		currCommand = currCommand.substring(0, currCommand.indexOf("/*"));
            		blockcomment = true;
            	}
            }
            currCommand = currCommand.trim();
            if (currCommand.length() == 0)
            	continue;
            String commandTokens[] = currCommand.split("(?=[\\s\\{\\}\\(\\)\\[\\]\\.\\,\\;\\+\\-\\*\\/\\&\\|\\<\\>\\=\\~])|(?<=[\\s\\{\\}\\(\\)\\[\\]\\.\\,\\;\\+\\-\\*\\/\\&\\|\\<\\>\\=\\~])");
            for (String s : commandTokens) {
            	if (!s.equals(" "))
            		tokens.add(s);
            }

		}

	}

	public void produceTokenList(File outputfile) throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));

		int startingTokenIndex = tokenIndex;

		bw.write("<tokens>\n");

		while (hasMoreTokens()) {
			advance();

			if (tokenType() == Token.KEYWORD) {
				bw.write("<keyword> " + getCurrToken() + " </keyword>\n");
			} else if (tokenType() == Token.SYMBOL) {
				if (symbol() == '<') {
					bw.write("<symbol> &lt; </symbol>\n");
				} else if (symbol() == '>') {
					bw.write("<symbol> &gt; </symbol>\n");
				} else if (symbol() == '&') {
					bw.write("<symbol> &amp; </symbol>\n");
				} else {
					bw.write("<symbol> " + symbol() + " </symbol>\n");
				}
			} else if (tokenType() == Token.IDENTIFIER) {
				bw.write("<identifier> " + identifier() + " </identifier>\n");
			} else if (tokenType() == Token.INT_CONST) {
				bw.write("<integerConstant> " + intVal() + " </integerConstant>\n");
			} else if (tokenType() == Token.STRING_CONST) {
				bw.write("<stringConstant> " + stringVal() + " </stringConstant>\n");
			}
		}

		bw.write("</tokens>\n");
		bw.close();

		tokenIndex = startingTokenIndex;

	}

	public String getCurrToken() {
		return currToken;
	}

	public boolean hasMoreTokens() {

    	if (tokenIndex == tokens.size())
    		return false;
    	return true;
	
	}

	public void advance() {

		// handle for case where string constants were split because of spaces.
		// justAdvanced and strTokenInc used to manage goBack() in these unique string cases

		if (tokens.get(tokenIndex).startsWith("\"")){
			if (tokens.get(tokenIndex).endsWith("\"")) {
				currToken = tokens.get(tokenIndex++);
				justAdvanced = false;
			} else {
				currToken = tokens.get(tokenIndex++);
				justAdvanced = true;
				strTokenInc = 0;
				while (!tokens.get(tokenIndex-1).endsWith("\"")) {
					strTokenInc++;
					currToken = currToken + " " + tokens.get(tokenIndex++);
				}
			}
		} else {
			currToken = tokens.get(tokenIndex++);
			justAdvanced = false;
		}
	
	}

	public void goBack() {

		if (!justAdvanced) {
			tokenIndex--;
			currToken = tokens.get(tokenIndex-1);
		} else {
			tokenIndex--;
			for (int i=0; i<strTokenInc; i++)
				tokenIndex--;
			currToken = tokens.get(tokenIndex-1);
			strTokenInc = 0;
			justAdvanced = false;
		}

	}

	public enum Token {
	
		KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
	
	}

	public enum Keyword {

		CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, 
		TRUE, FALSE, NULL, THIS

	}

	public Token tokenType() {
		
		for (String a : keywords)
			if (currToken.equals(a))
				return Token.KEYWORD;

		for (String a : symbols)
			if (currToken.equals(a))
				return Token.SYMBOL;

		try {
			if (Integer.parseInt(currToken) >= 0 && Integer.parseInt(currToken) <= 32767)
				return Token.INT_CONST;	
		} catch (NumberFormatException e) {}

		if (currToken.startsWith("\""))
			return Token.STRING_CONST;

		if (currToken.length() != 0)
			return Token.IDENTIFIER;

		return null;
	}

	public Keyword keyWord() {

		return Keyword.valueOf(currToken.toUpperCase());

	}

	public char symbol() {

		return currToken.charAt(0);

	}

	public String identifier() {

		return currToken;

	}

	public int intVal() {

		return Integer.parseInt(currToken);

	}

	public String stringVal() {

		return currToken.substring(1, currToken.length() - 1);

	}

	public void close() throws IOException {

		br.close();
	
	}
}