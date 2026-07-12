package Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class CopyFile {

	public static PrintStream openOutputFile(String filename) {
		PrintStream out;
		try { // try to open output file as well...
			out = new PrintStream(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			System.err.println("**Error Opening File: unable to open output file '"
					+ filename + "'.");
			out = null;
		}// try-catch
		catch (SecurityException e) {
			System.err.println("**Error: no permission to write output file '"
					+ filename + "'.");
			out = null;
		}// try-catch


		return out;
	}

	public static boolean copyFile(String source, String target) {

		InFile infile = null;
		PrintStream out = openOutputFile(target);
		try {
			infile = new InFile(source);
		} catch (FileNotFoundException e) {
			System.err.println("**Error Copying File: unable to open file " + source);
			return false;
		}

		while (true) {
			String str = null;

			try {
				str = infile.readLine();
				if (str != null) {
					out.println(str);
				}
			} catch (Exception re) {
			}
			if (str == null) {
				break;
			}
		}

		out.close();
		return true;
	}
}
