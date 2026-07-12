package Plot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class RstPlot {

	private boolean isInited = false;
	private Vector<Vector<String>>odata; // original data
	private double[][] tdata; // transfered data
	private int[] label;
	private String filename;

	private int[] popid; // population ID info , if provided
	private Vector<String> idvector; // population ID info

	public RstPlot(String filename) {
		this.filename = filename;
	}

	private void initStorage(int maxpops) {
		odata = new Vector<Vector<String>>(maxpops);
		for (int i = 0; i < maxpops; i++) {
			odata.addElement(new Vector<String>());
		}

		tdata = new double[maxpops][];
		idvector = new Vector<String>();
		isInited = true;
		label = null;
		popid = null;
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
		// System.out.print("content:\n"+content);
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
			if (st.hasMoreTokens()) {
				token = st.nextToken();
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

			if (!token.equals("") && token.equals("(%Miss)")) {
				ready = true;
				continue;
			}

			if (ready && token.equals("Estimated")) {
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

			while (st.hasMoreTokens()) {
				token = st.nextToken();
			}

			try {
				group = Integer.parseInt(token);
			} catch (Exception e) {
			}

			parse_count++;

			// the fist_prob is computed differently
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

		odata = null;
		tdata = null;
		label = null;

		isInited = false;

		parseData();
		analyzeData();

		if (tdata == null || tdata.length == 0) {
			return null;
		}
		int num = tdata.length;
		int size = tdata[0].length;
		double[][] data = new double[num][];
		for (int i = 0; i < num; i++) {
			data[i] = new double[size];
			for (int j = 0; j < size; j++) {
				data[i][j] = 1.0;
				if (i != 0) {
					for (int k = 0; k < i; k++) {
						data[i][j] -= tdata[k][j];
					}
				}
			}
		}

		// do the label
		label = new int[size];
		for (int i = 0; i < size; i++) {
			label[i] = i + 1;
		}

		// do the popid
		if (tdata[0].length == idvector.size()) {
			popid = new int[idvector.size()];
			for (int i = 0; i < popid.length; i++) {
				try {
					popid[i] = Integer
							.parseInt((idvector.elementAt(i)));
				} catch (Exception e) {
				}
			}
		}
		return data;
	}

	private void findPrimary(int[] group, int index, int num) {
		double max = 0;
		int prim = 0;
		for (int i = 0; i < num; i++) {
			if (tdata[i][index] > max) {
				max = tdata[i][index];
				prim = i;
			}
		}
		group[index] = prim;
		return;
	}

	private void sort(int index, int target, Vector<Integer> v) {

		double data = tdata[target][index];

		if (v.size() == 0) {
			v.addElement(new Integer(index));
			return;
		}

		for (int i = 0; i < v.size(); i++) {
			Integer temp = v.elementAt(i);
			int oindex = temp.intValue();
			double compare = tdata[target][oindex];
			if (data > compare) {
				v.insertElementAt(new Integer(index), i);
				return;
			}
		}

		v.addElement(new Integer(index));
		return;

	}

	public int[] getLabel() {
		return label;
	}

	public int[] getPopId() {
		return popid;
	}

	/**********************************************************/
	/* Sort Data by Q value */
	/**********************************************************/

	public double[][] sortData() {

		if (tdata == null || tdata.length == 0) {
			return null;
		}
		int num = tdata.length;
		int size = tdata[0].length;

		boolean compPopId = false;
		if (idvector.size() == tdata[0].length) {
			popid = new int[tdata[0].length];
			compPopId = true;
		}

		int[] group = new int[size];

		for (int i = 0; i < size; i++) {
			findPrimary(group, i, num);
		}

		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>(num);
		for (int i = 0; i < num; i++) {
			v.addElement(new Vector<Integer>());
		}

		for (int i = 0; i < num; i++) {
			for (int j = 0; j < size; j++) {
				if (group[j] == i) {
					sort(j, i, v.elementAt(i));
				}
			}
		}

		double[][] data = new double[num][];
		for (int i = 0; i < num; i++) {
			data[i] = new double[size];
		}

		int count = 0;
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < v.elementAt(i).size(); j++) {
				Integer temp = v.elementAt(i).elementAt(j);
				int target = temp.intValue();
				for (int k = 0; k < num; k++) {
					data[k][count] = tdata[k][target];
				}
				label[count] = target + 1;
				if (compPopId) {
					try {
						popid[count] = Integer.parseInt((idvector
								.elementAt(target)));
					} catch (Exception e) {
					}
				}
				count++;
			}
		}

		double[][] sortdata = new double[num][];
		for (int i = 0; i < num; i++) {
			sortdata[i] = new double[size];
			for (int j = 0; j < size; j++) {
				sortdata[i][j] = 1.0;
				if (i != 0) {
					for (int k = 0; k < i; k++) {
						sortdata[i][j] -= data[k][j];
					}
				}
			}
		}

		return sortdata;
	}

	/**********************************************************/
	/* group data by pop ID */
	/**********************************************************/

	public double[][] groupData() {

		if (tdata == null || tdata.length == 0) {
			return null;
		}
		int num = tdata.length;
		int size = tdata[0].length;

		boolean compPopId = false;

		// if no or no consistent pop id available
		// return the raw data

		if (idvector.size() != tdata[0].length) {
			return getData();
		}

		// build a hash table based on the idvector info,
		// associate the seq no. with popid

		Hashtable<Integer, Vector<Integer>> poptable = new Hashtable<Integer, Vector<Integer>>();
		Vector<Integer> key_vec = new Vector<Integer>();

		for (int i = 0; i < idvector.size(); i++) {
			int pop_group = -1;

			try {
				pop_group = Integer.parseInt((idvector.elementAt(i)));
			} catch (Exception e) {
			}

			Integer key = new Integer(pop_group);
			Integer val = new Integer(i);
			if (poptable.containsKey(key)) {
				Vector<Integer> v = poptable.get(key);
				v.add(val);
			} else {
				Vector<Integer> v = new Vector<Integer>();
				v.add(val);
				poptable.put(key, v);
				key_vec.add(key);
			}
		}

		int count = 0;

		// read hash table , build the desired seq and popid array

		popid = new int[size];
		label = new int[size];
		int[] seq = new int[size];

		// correct the order of hash table
		Collections.sort(key_vec);

		for (int i = 0; i < key_vec.size(); i++) {
			Integer currpop = key_vec.elementAt(i);
			Vector<Integer> v = poptable.get(currpop);
			for (int k = 0; k < v.size(); k++) {
				seq[count] = (v.elementAt(k)).intValue();
				// get the label
				label[count] = seq[count] + 1;

				popid[count] = currpop.intValue();

				count++;
			}
		}

		// build the data according to the sequence in array seq
		double[][] gdata = new double[num][];

		for (int i = 0; i < num; i++) {
			gdata[i] = new double[size];
			for (int j = 0; j < size; j++) {
				gdata[i][j] = 1.0;
				if (i != 0) {
					for (int k = 0; k < i; k++) {
						gdata[i][j] -= tdata[k][seq[j]];
					}
				}
			}
		}

		return gdata;
	}
}
