package Triangle;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;

public class DataParser {

	private boolean isInited = false;
	private Vector<Vector<String>> odata; // original data
	private double[][] tdata; // transfered data

	private int[] id;
	private Vector<String> idvector;

	private int[] label;
	private String filename;
	private boolean data_process;

	private int K;

	public DataParser(String filename) {
		this.filename = filename;
		data_process = false;
	}

	private void initStorage(int maxpops) {
		odata = new Vector<Vector<String>>(maxpops);
		for (int i = 0; i < maxpops; i++) {
			odata.addElement(new Vector<String>());
		}

		tdata = new double[maxpops][];
		idvector = new Vector<String>();
		isInited = true;
		return;
	}

	private void parseData() {

		StringBuffer buffer = new StringBuffer();
		String content = "";

		try {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();
			content = buffer.toString();
		} catch (IOException e) {
		}

		boolean ready = false;
		String str = new String();
		StringTokenizer file_st = new StringTokenizer(content, "\n");

		while (file_st.hasMoreTokens()) {

			str = file_st.nextToken();

			// wrong format found quit
			if (str.indexOf("Rep#") >= 0) {
				return;
			}

			StringTokenizer st = new StringTokenizer(str);
			if (!st.hasMoreTokens() && ready) {
				break;
			}

			String token = "";

			// check assumed cluster info
			if (str.indexOf("populations assumed") > 0) {
				token = st.nextToken();
				try {
					K = Integer.parseInt(token);

				} catch (Exception e) {
				}
				continue;
			}

			// find the sign to start the data session

			if (st.hasMoreTokens()) {
				token = st.nextToken();
			}

			if (!token.equals("")
					&& (token.equals("(%Miss)") || token.equals("(Miss)"))) {
				ready = true;
				continue;
			}

			if (!token.equals("") && token.equals("Label")) {
				if (st.hasMoreTokens()) {
					token = st.nextToken();
					if (!token.equals("")
							&& (token.equals("(%Miss)") || token
									.equals("(Miss)"))) {
						ready = true;
						continue;
					}
				}
			}

			if (ready && (token.equals("") || token.equals("Estimated"))) {
				break;
			}

			if (ready) {

				if (str.indexOf("|") < 0) {
					storeData(str);
				} else {
					storePopData(str);
				}
			}

		}
	}

	private void storePopData(String str) {

		StringTokenizer strSt = new StringTokenizer(str, "|");
		int count = 0;

		if (!isInited) {
			int ct = 0;
			while (strSt.hasMoreTokens()) {
				strSt.nextToken();
				ct++;
			}
			initStorage(ct - 1);
			strSt = new StringTokenizer(str, "|"); // rewind for data parsing
		}

		double first_prob = 1.0;
		int first_group = 0;

		int parse_count = 0; // count for No of columns

		while (strSt.hasMoreTokens()) {

			String sub = strSt.nextToken();
			int index = sub.indexOf(":");
			if (index < 0) {
				odata.elementAt(first_group - 1).addElement(Double.toString(first_prob));
				return;
			}

			String leftsub = sub.substring(0, index);
			String rightsub = sub.substring(index + 1, sub.length());

			// find the group by parsing the leftsub
			StringTokenizer st = new StringTokenizer(leftsub);
			String token = null;

			int group = 0;

			// jump to the last token
			while (st.hasMoreTokens()) {
				token = st.nextToken();
			}

			try {
				group = Integer.parseInt(token);
			} catch (Exception e) {
			}

			parse_count++;

			// the first_prob is computed differently
			if (parse_count == 1) {
				idvector.addElement(token);
				first_group = group;
				continue;
			}

			// find the prob by parsing the rightsub
			st = new StringTokenizer(rightsub);
			double prob = 0;
			int iter = 0; // count of iterations
			while (st.hasMoreTokens()) {
				token = st.nextToken();
				try {
					double data = Double.parseDouble(token);
					prob += data * Math.exp((Math.log(0.5) * iter));
					iter++;

				} catch (Exception e) {
				}
			}

			first_prob -= prob;
			odata.elementAt(group - 1).addElement(Double.toString(prob));

		}

		return;
	}

	private void storeData(String str) {

		StringTokenizer st = new StringTokenizer(str);

		String token = null;
		/*
		 * if(st.hasMoreTokens()){ token = st.nextToken(); }
		 */

		int index = str.indexOf(":");
		String data = str.substring(index + 1, str.length());

		st = new StringTokenizer(data);

		if (!isInited) {
			int ct = 0;
			while (st.hasMoreTokens()) {
				st.nextToken();
				ct++;
			}
			st = new StringTokenizer(data);
			initStorage(ct);
		}

		int count = 0;
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			odata.elementAt(count).addElement(token);
			count++;
		}

		// use leftsub to find out the popid
		String leftsub = str.substring(0, index);

		st = new StringTokenizer(leftsub);

		// jump to the last token
		while (st.hasMoreTokens()) {
			token = st.nextToken();
		}

		try {
			int group = Integer.parseInt(token);
			idvector.addElement(token);
		} catch (Exception e) {
		}

		return;
	}

	private void analyzeData() {
		if (odata == null) {
			return;
		}
		int num = odata.size();
		if (num == 0) {
			return;
		}
		int size = odata.elementAt(0).size();

		for (int i = 0; i < num; i++) {
			tdata[i] = new double[size];
			for (int j = 0; j < size; j++) {
				try {
					tdata[i][j] = Double.parseDouble(odata.elementAt(i).elementAt(j));
				} catch (Exception e) {
				}
			}
		}

	}

	public double[][] getData() {
		if (!data_process) {
			odata = null;
			tdata = null;
			label = null;

			isInited = false;

			parseData();
			analyzeData();

			data_process = true;
		}
		if (tdata == null || tdata.length == 0) {
			return null;
		}
		return tdata;
	}

	public int[] getId() {
		if (!data_process) {
			odata = null;
			tdata = null;
			label = null;

			isInited = false;

			parseData();
			analyzeData();
			data_process = true;
		}

		if (tdata == null || tdata.length == 0) {
			return null;
		}
		if (tdata[0].length != idvector.size()) {
			if (idvector.size() != 0) {
				System.err.println(tdata[0].length + "  " + idvector.size());
			}
			return null;
		}

		id = new int[idvector.size()];
		for (int i = 0; i < id.length; i++) {
			try {
				id[i] = Integer.parseInt((idvector.elementAt(i)));
			} catch (Exception e) {
			}
		}

		return id;

	}

	public int getK() {
		if (!data_process) {
			odata = null;
			tdata = null;
			label = null;

			isInited = false;

			parseData();
			analyzeData();
			data_process = true;
		}

		return K;
	}
}
