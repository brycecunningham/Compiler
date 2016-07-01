import java.util.*;


public class SymbolTable {

	private Map<String,ArrayList<String>> classcope;
	private Map<String,ArrayList<String>> subscope;
	private static int staticIndex = 0;
	private static int fieldIndex = 0;
	private static int argIndex = 0;
	private static int varIndex = 0;

	SymbolTable() {

		classcope = new HashMap<String,ArrayList<String>>();
		subscope = new HashMap<String,ArrayList<String>>();

	}

	public void startSubroutine() {
		subscope.clear();
		argIndex = 0;
		varIndex = 0;
	}

	public void define(String name, String type, String kind) {
		
		ArrayList<String> data = new ArrayList<>();
		data.add(type);
		data.add(kind);

		if (kind.equals("static")) {
			data.add(Integer.toString(staticIndex++));
			classcope.put(name, data);
		} else if (kind.equals("field")) {
			data.add(Integer.toString(fieldIndex++));
			classcope.put(name, data);
		} else if (kind.equals("argument")) {
			data.add(Integer.toString(argIndex++));
			subscope.put(name, data);
		} else if (kind.equals("var")) {
			data.add(Integer.toString(varIndex++));
			subscope.put(name, data);
		}
	}

	public int varCount(String kind) {
		switch (kind) {
			case "static": 		return staticIndex;
			case "field":		return fieldIndex;
			case "argument":	return argIndex;
			case "var":			return varIndex;
		}
		return 0;
	}

	public String kindOf(String name) {
		if (classcope.containsKey(name))
			return classcope.get(name).get(1);
		if (subscope.containsKey(name))
			return subscope.get(name).get(1);
		return null;
	}

	public String typeOf(String name) {
		if (classcope.containsKey(name))
			return classcope.get(name).get(0);
		if (subscope.containsKey(name))
			return subscope.get(name).get(0);
		return null;
	}

	public int indexOf(String name) {
		if (classcope.containsKey(name))
			return Integer.parseInt(classcope.get(name).get(2));
		if (subscope.containsKey(name))
			return Integer.parseInt(subscope.get(name).get(2));
		return 0;
	}

}
