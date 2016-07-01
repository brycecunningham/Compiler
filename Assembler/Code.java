import java.io.*;


public class Code {

	public String dest(String s) {
		switch (s) {
			case "": return "000"; //0b000;
			case "M": return "001"; //0b001;
			case "D": return "010"; //0b010;
			case "MD": return "011"; //0b011;
			case "A": return "100"; //0b100;
			case "AM": return "101"; //0b101;
			case "AD": return "110"; //0b110;
			case "AMD": return "111"; //0b111;
			default: return "000"; //0b000;
		}
	}

	public String comp(String s) {
		switch (s) {
			case "0": return "0101010"; //0b0101010
			case "1": return "0111111"; //0b0111111
			case "-1": return "0111010"; //0b0111010
			case "D": return "0001100"; //0b0001100
			case "A": return "0110000"; //0b0110000
			case "!D": return "0001101"; //0b0001101
			case "!A": return "0110001"; //0b0110001
			case "-D": return "0001111"; //0b0001111
			case "-A": return "0110011"; //0b0110011
			case "D+1": return "0011111"; //0b0011111
			case "A+1": return "0110111"; //0b0110111
			case "D-1": return "0001110"; //0b0001110
			case "A-1": return "0110010"; //0b0110010
			case "D+A": return "0000010"; //0b0000010
			case "D-A": return "0010011"; //0b0010011
			case "A-D": return "0000111"; //0b0000111
			case "D&A": return "0000000"; //0b0000000
			case "D|A": return "0010101"; //0b0010101
			case "M": return "1110000"; //0b1110000
			case "!M": return "1110001"; //0b1110001
			case "-M": return "1110011"; //0b1110011
			case "M+1": return "1110111"; //0b1110111
			case "M-1": return "1110010"; //0b1110010
			case "D+M": return "1000010"; //0b1000010
			case "D-M": return "1010011"; //0b1010011
			case "M-D": return "1000111"; //0b1000111
			case "D&M": return "1000000"; //0b1000000
			case "D|M": return "1010101"; //0b1010101
			default: return "0000000"; //0b0000000
		}

	}

	public String jump(String s) {
		switch (s) {
			case "": return "000"; //0b000
			case "JGT": return "001"; //0b001
			case "JEQ": return "010"; //0b010
			case "JGE": return "011"; //0b011
			case "JLT": return "100"; //0b100
			case "JNE": return "101"; //0b101
			case "JLE": return "110"; //0b110
			case "JMP": return "111"; //0b111
			default: return "000"; //0b000
		}
	}
	
}
