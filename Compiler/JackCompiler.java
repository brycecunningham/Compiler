import java.io.*;
import java.util.*;

public class JackCompiler {

	public static void main(String[] args) throws IOException {

		File source = new File(args[0]);
		List<File> inputfiles = new ArrayList<File>();
		File outputfile;
		File outputfileT;

		
		// source is either a directory of files
		if (source.isDirectory()) {
			for (File f : source.listFiles())
				if (f.getName().endsWith(".jack")) {
					inputfiles.add(f);
				}
		
		// or a .jack file
		} else {	
			inputfiles.add(source);
		}

		for (File f : inputfiles) {
			String inputstring = f.getPath();
			String fileprefix = inputstring.substring(0, inputstring.length() - 5);
			outputfile = new File(fileprefix + ".vm");

			JackTokenizer jt = new JackTokenizer(f);
			CompilationEngine ce = new CompilationEngine(jt, outputfile);
			ce.compileClass();
		}
	}
}