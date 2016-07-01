import java.util.*;


public class SymbolTable {

	Map<String,Integer> symtable;

	SymbolTable() {

		symtable = new HashMap<String,Integer>();

		symtable.put("SP", 0);
		symtable.put("LCL", 1);
		symtable.put("ARG", 2);
		symtable.put("THIS", 3);
		symtable.put("THAT", 4);
		symtable.put("R0", 0);
		symtable.put("R1", 1);
		symtable.put("R2", 2);
		symtable.put("R3", 3);
		symtable.put("R4", 4);
		symtable.put("R5", 5);
		symtable.put("R6", 6);
		symtable.put("R7", 7);
		symtable.put("R8", 8);
		symtable.put("R9", 9);
		symtable.put("R10", 10);
		symtable.put("R11", 11);
		symtable.put("R12", 12);
		symtable.put("R13", 13);
		symtable.put("R14", 14);
		symtable.put("R15", 15);
		symtable.put("SCREEN", 16384);
		symtable.put("KBD", 24576);
	}

	public void addEntry(String symbol, int address) {
		symtable.put(symbol, address);
	}

	public boolean contains(String symbol) {
		return symtable.containsKey(symbol);
	}

	public int getAddress(String symbol) {
		return symtable.get(symbol);
	}

}
