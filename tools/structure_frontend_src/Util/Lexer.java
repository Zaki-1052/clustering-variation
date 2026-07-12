package Util;

import java.io.InputStream;

public class Lexer {

	// member varibles :
	private byte[] cur; // current character
	private int curr_line; // current line#
	private int curr_col; // current column#
	private int token_col;
	private int max_token_col;
	private InputStream input_data; // input file stream
	boolean EOF_FOUND; // End of file reached

	// constructor
	public Lexer(InputStream in) {
		cur = new byte[1];
		input_data = in;
		curr_line = 0;
		curr_col = 0;
		Read_A_Char();
		EOF_FOUND = false;
	}

	// private method : Read a single character
	private void Read_A_Char() {
		try {
			if (input_data.read(cur, 0, 1) != 1) {
				EOF_FOUND = true;
				return;
			}

			if (cur[0] == '\n' || cur[0] == '\r') {
				curr_line++;
				curr_col = 0;
				token_col = 0;
			} // a new line
			else {
				curr_col++;
			}
		} catch (Exception e1) {
			System.err.println("EXCEPTION in Lexer");
		}
	}

	public String getNextString() {

		/* If current character reached is EOF: */
		if (EOF_FOUND) {
			return null;
		}

		/* skip the whitespace except EOF */
		while (Character.isWhitespace((char) cur[0])) {
			Read_A_Char();
			if (EOF_FOUND) {
				return null;
			}
		}

		/* Record the initial position */
		int start_col = curr_col;
		int start_line = curr_line;

		/* record the initial state, prepare to find a string */
		String curr_string_token = "";
		curr_string_token += (char) cur[0]; // used to store found string

		/* serach longest string token: */
		for (;;) {
			Read_A_Char(); // stop when finds
			if (Character.isWhitespace((char) cur[0]) // a whitespace
					|| EOF_FOUND) {
				break;
			}
			curr_string_token += (char) cur[0];
		}

		token_col++;
		if (max_token_col < token_col) {
			max_token_col = token_col;
		}

		return curr_string_token;

	}

	public int getTokenCol() {
		return max_token_col;
	}

	public int getLine() {
		return curr_line;
	}

}
