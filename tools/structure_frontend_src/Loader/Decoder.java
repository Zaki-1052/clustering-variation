package Loader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;

public class Decoder {

	// input files
	private String rstFile;
	private String runtimeFile;

	// positioning Vector
	private Vector<Vector<String>> posVec;

	// data storage
	private Vector<Vector<String>> fstVec;
	private Vector<Vector<String>> alphaVec;
	// private Vector lambdVec;

	private Vector<String> recVec;
	private Vector<String> llhdVec;
	private Vector<String> lnpdVec;

	private int countFst = 0;
	private int countAlpha = 0;
	@SuppressWarnings("unused")
	private int countLambd = 0;
	@SuppressWarnings("unused")
	private int countKLD = 0;
	private int countRec = 0;
	private int countLike = 0;
	private int countLnpd = 0;

	private int burnin = 0;

	double[][] fst_array;
	double[][] alpha_array;
	double[][] llhd_array;
	double[][] rec_array;
	double[][] lnpd_array;

	// controlling bits
	boolean record = false;

	public Decoder(String rstFile, String runtimeFile) {
		this.rstFile = rstFile;
		this.runtimeFile = runtimeFile;
	}

	public String getRstContent() {

		StringBuffer buffer = new StringBuffer();
		String content = "";

		try {
			FileInputStream fis = new FileInputStream(rstFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();
			content = buffer.toString();
		} catch (IOException e) {
			return null;
		}

		return content;
	}

	public void readRuntimeData() {

		StringBuffer buffer = new StringBuffer();
		String content = "";

		try {
			FileInputStream fis = new FileInputStream(runtimeFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}
			in.close();
			content = buffer.toString();
		} catch (IOException e) {
			return;
		}

		String str = new String();
		StringTokenizer file_st = new StringTokenizer(content, "\n");
		while (file_st.hasMoreTokens()) {

			str = file_st.nextToken();

			if (str.startsWith("BURNIN completed")) {
				if (countLike != 0) {
					burnin = llhdVec.size() / 2;
				} else if (countFst != 0) {
					Vector<String> v = fstVec.elementAt(0);
					burnin = v.size() / 2;
				} else if (countAlpha != 0) {
					Vector<String> v = alphaVec.elementAt(0);
					burnin = v.size() / 2;
				} else if (countRec != 0) {
					burnin = recVec.size() / 2;
				}
			}
			if (str.indexOf("MCMC completed") >= 0) {
				break;
			}
			if (str.indexOf("Rep#") >= 0) {
				parseTitle(str);
				record = true;
				continue;
			}
			if (record) {
				parseData(str);
			}

		}
		// System.out.println(countRec+" "+countLambd+" "+countAlpha+" "+countFst+" "+countKLD+" "+countLike+" "+countLnpd);
		/*
		 * Vector v = (Vector)alphaVec.elementAt(0); for(int i=0;i<v.size();i++)
		 * System.out.println(v.elementAt(i));
		 */
		genPlotData();

	}

	private void parseTitle(String str) {
		StringTokenizer st = new StringTokenizer(str);
		String key = new String();
		int fst = 0;
		int alpha = 0;
		int lnpd = 0;
		int like = 0;
		int kld = 0;
		int rec = 0;
		int lambd = 0;
		while (st.hasMoreTokens()) {
			key = st.nextToken();
			if (key.equals("r")) {
				rec++;
				continue;
			}

			if (key.startsWith("Lambda")) {
				lambd++;
				continue;
			}
			if (key.startsWith("Alpha")) {
				alpha++;
				continue;
			}
			if (key.startsWith("F")) {
				fst++;
				continue;
			}
			if (key.startsWith("D")) {
				kld++;
				continue;
			}
			if (key.equals("Like")) {
				like++;
				continue;
			}
			if (key.equals("P(D)")) {
				lnpd++;
				continue;
			}
		}

		// System.out.println("recomb:"+rec+" lambda:"+lambd+" alpha:"+alpha+" fst:"+fst+" KLD:"+kld+" Like:"+like+" LnPD:"+lnpd);
		// update the format counter then
		countRec = rec;
		if (rec > 0 && recVec == null) {
			recVec = new Vector<String>();
		}

		countFst = fst;
		if (fst > 0 && fstVec == null) {
			fstVec = new Vector<Vector<String>>();
			for (int i = 0; i < fst; i++) {
				fstVec.addElement(new Vector<String>());
			}
		}
		countAlpha = alpha;
		if (alpha > 0 && alphaVec == null) {
			alphaVec = new Vector<Vector<String>>();
			for (int i = 0; i < alpha; i++) {
				alphaVec.addElement(new Vector<String>());
			}
		}

		// recording and initialization

		countLambd = lambd;
		countKLD = kld;

		countLike = like;
		if (like > 0 && llhdVec == null) {
			llhdVec = new Vector<String>();
		}
		countLnpd = lnpd;
		if (lnpd > 0 && lnpdVec == null) {
			lnpdVec = new Vector<String>();
		}

		// arrange everthing in position
		posVec = null;
		posVec = new Vector<Vector<String>>();
		if (rec != 0) {
			posVec.addElement(recVec);
		}
		for (int i = 0; i < lambd; i++) {
			posVec.addElement(null);
		}
		for (int i = 0; i < alpha; i++) {
			posVec.addElement(alphaVec.elementAt(i));
		}
		for (int i = 0; i < fst; i++) {
			posVec.addElement(fstVec.elementAt(i));
		}
		for (int i = 0; i < kld; i++) {
			posVec.addElement(null);
		}
		if (like != 0) {
			posVec.addElement(llhdVec);
		}
		if (lnpd != 0) {
			posVec.addElement(lnpdVec);
		}

	}

	private void parseData(String str) {
		if (str == null) {
			return;
		}
		StringTokenizer st = new StringTokenizer(str);
		String key = new String();
		String round = new String();
		int token_count = 0;
		@SuppressWarnings("unused")
		int curr_rep = 0;
		while (st.hasMoreTokens()) {
			key = st.nextToken();
			if (token_count == 0) {
				// test if it is a valid data entry
				int index = key.indexOf(":");
				if (index < 0) {
					record = false;
					return;
				}
				round = key.substring(0, index);
				try {
					curr_rep = Integer.parseInt(round);
				} catch (Exception e) {
					record = false;
					return;
				}
			} else {
				Vector<String> targetVec = posVec.elementAt(token_count - 1);
				if (targetVec != null) {
					targetVec.addElement(round);
					targetVec.addElement(key);
				}
			}
			token_count++;
		}
	}

	private void genPlotData() {

		if (countFst != 0) {
			fst_array = new double[countFst][];
			for (int i = 0; i < countFst; i++) {
				fst_array[i] = vec2array(fstVec.elementAt(i));
			}
		}

		if (countAlpha != 0) {
			alpha_array = new double[countAlpha][];
			for (int i = 0; i < countAlpha; i++) {
				alpha_array[i] = vec2logarray(alphaVec.elementAt(i));
			}

		}

		if (countLike != 0) {
			llhd_array = new double[1][];
			llhd_array[0] = vec2array(llhdVec);
		}
		if (countRec != 0) {
			rec_array = new double[1][];
			rec_array[0] = vec2array(recVec);
		}

		if (countLnpd != 0) {
			lnpd_array = new double[1][];
			lnpd_array[0] = vec2array(lnpdVec);
		}

		recVec = null;
		llhdVec = null;
		lnpdVec = null;
		alphaVec = null;
		fstVec = null;

		/*
		 * if(fstHist!=null){ PlotData pd = new PlotData(fstHist);
		 * pd.writeDataFile(saveDir, sim.getRstName()+"_fsthist"); }
		 * if(alphaHist!=null){ PlotData pd = new PlotData(alphaHist);
		 * pd.writeDataFile(saveDir, sim.getRstName()+"_alphahist"); }
		 * 
		 * if(lnpdHist != null){ double[][] lnpd_hist = new double[1][];
		 * lnpd_hist[0] = lnpdHist; PlotData pd = new PlotData(lnpd_hist);
		 * pd.writeDataFile(saveDir, sim.getRstName()+"_lnpdhist"); }
		 * 
		 * if(recHist != null){ double[][] rec_hist = new double[1][];
		 * rec_hist[0] = recHist; PlotData pd = new PlotData(rec_hist);
		 * pd.writeDataFile(saveDir, sim.getRstName()+"_recmbhist"); }
		 * 
		 * if(likHist != null){
		 * 
		 * double[][] lik_hist = new double[1][]; lik_hist[0] = likHist;
		 * PlotData pd = new PlotData(lik_hist); pd.writeDataFile(saveDir,
		 * sim.getRstName()+"_lhdhist"); }
		 */
		// System.out.println("Burnin = "+burnin);

	}

	private double[] vec2logarray(Vector<String> v) {

		if (v == null) {
			return null;
		}
		double[] result = new double[v.size()];
		for (int i = 0; i < v.size(); i++) {
			try {
				result[i] = Double.parseDouble(v.elementAt(i));
				if (i % 2 == 1) {
					result[i] = Math.log(result[i]) / Math.log(10);
				}
			} catch (Exception e) {
				System.err.println(e);
			}
		}

		return result;
	}

	private double[] vec2array(Vector<String> v) {
		if (v == null) {
			return null;
		}
		double[] result = new double[v.size()];
		for (int i = 0; i < v.size(); i++) {
			try {
				result[i] = Double.parseDouble(v.elementAt(i));
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
		}

		return result;
	}

	public double[][] getHistArray(int cat) {

		double[][] hist = null;

		// for rec
		if (cat == 1) {
			if (countRec == 0) {
				return null;
			}
			hist = new double[1][];
			hist[0] = new double[rec_array[0].length / 2 - burnin];
			int count = 0;
			for (int i = 2 * burnin + 1; i < rec_array[0].length; i += 2) {
				hist[0][count] = rec_array[0][i];
				count++;
			}
		}
		if (cat == 2) {
			if (countAlpha == 0) {
				return null;
			}
			hist = new double[countAlpha][];
			for (int k = 0; k < countAlpha; k++) {
				hist[k] = new double[alpha_array[k].length / 2 - burnin];
				int count = 0;
				for (int i = 2 * burnin + 1; i < alpha_array[k].length; i += 2) {
					hist[k][count] = alpha_array[k][i];
					count++;
				}
			}
		}
		if (cat == 3) {
			if (countFst == 0) {
				return null;
			}
			hist = new double[countFst][];
			for (int k = 0; k < countFst; k++) {
				hist[k] = new double[fst_array[k].length / 2 - burnin];
				int count = 0;
				for (int i = 2 * burnin + 1; i < fst_array[k].length; i += 2) {
					hist[k][count] = fst_array[k][i];
					count++;
				}
			}
		}
		if (cat == 4) {
			if (countLike == 0) {
				return null;
			}
			hist = new double[1][];
			hist[0] = new double[llhd_array[0].length / 2 - burnin];
			int count = 0;
			for (int i = 2 * burnin + 1; i < llhd_array[0].length; i += 2) {
				hist[0][count] = llhd_array[0][i];
				count++;
			}
		}
		if (cat == 5) {
			if (countLnpd == 0) {
				return null;
			}
			hist = new double[1][];
			hist[0] = new double[lnpd_array[0].length / 2];
			int count = 0;
			for (int i = 1; i < lnpd_array[0].length; i += 2) {
				hist[0][count] = lnpd_array[0][i];
				count++;
			}
		}

		return hist;
	}

}
