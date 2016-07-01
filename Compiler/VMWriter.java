import java.io.*;
import java.util.*;


public class VMWriter {

	private BufferedWriter vmw;

	public VMWriter(File outputfile) throws IOException {

		vmw = new BufferedWriter(new FileWriter(outputfile));

	}

	public void writePush(String segment, int index) throws IOException {
		vmw.write("push " + segment + " " + index + "\n");
	}

	public void writePop(String segment, int index) throws IOException {
		vmw.write("pop " + segment + " " + index + "\n");
	}

	public void writeArithmetic(String command) throws IOException {
		vmw.write(command + "\n");
	}

	public void writeLabel(String label) throws IOException {
		vmw.write("label " + label + "\n");
	}

	public void writeGoto(String label) throws IOException {
		vmw.write("goto " + label + "\n");
	}

	public void writeIf(String label) throws IOException {
		vmw.write("if-goto " + label + "\n");
	}

	public void writeCall(String name, int nArgs) throws IOException {
		vmw.write("call " + name + " " + nArgs + "\n");
	}

	public void writeFunction(String name, int nLocals) throws IOException {
		vmw.write("function " + name + " " + nLocals + "\n");
	}

	public void writeReturn() throws IOException {
		vmw.write("return\n");
	}

	public void close() throws IOException {
		vmw.close();
	}
}