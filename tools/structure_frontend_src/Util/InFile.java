package Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class InFile extends BufferedReader {
	public InFile(String filename) throws FileNotFoundException {

		super(new InputStreamReader(new FileInputStream(filename)));
	}

}
