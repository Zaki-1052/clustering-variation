package Util;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class FileCounter {

	private File file;
	private Hashtable<Integer, Integer> stat;

	public FileCounter(File f) {
		file = f;

	}

	private boolean countFile() {

		stat = new Hashtable<Integer, Integer>();
		String str = "";
		int row_count = 0;

		InFile infile = null;
		try {
			infile = new InFile(file.getAbsolutePath());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Can not Read Data Source",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		while (true) {
			try {
				str = infile.readLine();
			} catch (Exception re) {
			}

			if (str == null) {
				break;
			}

			StringTokenizer st = new StringTokenizer(str);
			if (!st.hasMoreTokens()) {
				continue;
			}
			//int col_count = 0;

			int word_count = 0;
			while (st.hasMoreTokens()) {
				st.nextToken();
				word_count++;
			}

			// write to stat hashtable
			Integer index = new Integer(word_count);
			if (stat.containsKey(index)) {
				Integer obj = stat.get(index);
				int objVal = obj.intValue();
				stat.put(index, new Integer(++objVal));
			} else {
				stat.put(new Integer(word_count), new Integer(1));
			}

			row_count++;
		}

		return true;
	}

	public void report() {
		if (stat == null) {
			if (!countFile()) {
				return;
			}
		}

		String info = file.getName() + "\n\n";
		Enumeration<Integer> keys = stat.keys();
		while (keys.hasMoreElements()) {
			Integer index = keys.nextElement();
			Integer obj = stat.get(index);
			info += new String(obj + " Lines with " + index + " Columns\n");
		}

		JOptionPane.showMessageDialog(null, info, "Structure: Open Data File",
				JOptionPane.INFORMATION_MESSAGE);

		return;
	}

}
