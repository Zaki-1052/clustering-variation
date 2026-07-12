package Proc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.SwingUtilities;

import Gui.MainFrame;
import Gui.ProjTree;
import Obj.NSimObj;
import Obj.PlotData;

public class AppProc extends Thread {
	private String appName;
	private String str;
	private Process proc; // the running process

	private ProjTree projTree;
	private MainFrame target; // the front end
	private NSimObj sim;

	private boolean record;
	private boolean end;
	private boolean isKilled;
	private boolean isStarted;
	private boolean isFast;

	private boolean recStart;
	private boolean lnpdStart;
	private boolean alpha_decide;

	private int maxdata; // the max number of data per line
	private int freq; // decide the number of points used by plotting
	private int alphacount;
	private int fstcount;
	private int reccount;
	private int likcount;
	private int lnpdcount;
	private int rcount;

	private Vector<Vector<String>> fst;
	private double[][] fstHist;

	private Vector<Vector<String>> alpha;
	private double[][] alphaHist;

	private double[] lnpdHist;
	private double[] likHist;
	private double[] recHist;
	private double[] srHist;

	private Vector<String> recmb;
	private Vector<String> sr; // if use sampling prior
	private Vector<String> llhd; // likelihood
	private Vector<String> lnpd;
	private boolean controlGui;
	

	public AppProc(String name, MainFrame target, ProjTree projTree,
			NSimObj sim, boolean controlGui) {

		appName = name;
		this.controlGui = controlGui;
		this.target = target;
		this.sim = sim;
		this.projTree = projTree;
		

		int num = 0;

		maxdata = 1;
		if (!sim.FREQSCORR) {
			fst = null;
			fstHist = null;
		} else {
			if (sim.ONEFST) {
				num = 1;
			} else {
				num = sim.getMAXPOPS();
			}

			maxdata += num;
			fst = new Vector<Vector<String>>(num);
			fstHist = new double[num][];

			for (int i = 0; i < num; i++) {
				fst.addElement(new Vector<String>());
				fstHist[i] = new double[sim.NUMREPS];
			}
			num = 0;

		}

		if (sim.NOADMIX || sim.POPNOADMIX) {
			alpha = null;
			alphaHist = null;
		} else {
			if (sim.POPALPHAS) {
				num = sim.getMAXPOPS();
			} else {
				num = 1;
			}

			if (!sim.INFERALPHA) {
				num = 1;
			}

			maxdata += num;
			alpha = new Vector<Vector<String>>(num);
			alphaHist = new double[num][];

			for (int i = 0; i < num; i++) {
				alpha.addElement(new Vector<String>());
				alphaHist[i] = new double[sim.NUMREPS];
			}
			num = 0;
		}

		if (sim.RECOMBINE || sim.POPRECOMBINE) {
			recmb = new Vector<String>();
			recHist = new double[sim.NUMREPS];
			maxdata += 1;

			alpha = new Vector<Vector<String>>(1);

			alphaHist = new double[1][];
			alphaHist[0] = new double[sim.NUMREPS];
		} else {
			recmb = null;
			recHist = null;
		}

		if (sim.LOCPRIOR) {
			sr = new Vector<String>();
			srHist = new double[sim.NUMREPS];
			maxdata += 1;
		} else {
			sr = null;
			srHist = null;
		}

		if (sim.COMPUTEPROB) {
			llhd = new Vector<String>();
			likHist = new double[sim.NUMREPS];
			lnpd = new Vector<String>();
			lnpdHist = new double[sim.NUMREPS];
			maxdata += 2;
		} else {
			llhd = null;
			likHist = null;
			lnpd = null;
			lnpdHist = null;
		}

		freq = (sim.BURNIN + sim.NUMREPS) / 2000;
		if (freq < 1) {
			freq = 1;
		}

	}

	public void run() {
		try {
			String str;
			proc = Runtime.getRuntime().exec(appName);
			BufferedReader proc_in = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			
			alpha_decide = recStart = lnpdStart = isFast = isStarted = isKilled = end = record = false;
			alphacount = fstcount = 0;
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					target.getConsole().setText("");	
					if (controlGui) {
						target.updateUI(3);
					}
				}
			});
			

			try {
				while ((str = proc_in.readLine()) != null) {
					if (isKilled) {
						return;
					}
					parseData(str);
					// System.out.println(str);
				}
				if (proc != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							projTree.appendRstNode(sim.getSimName(), sim.getRstName(),
									sim.getMAXPOPS());						
						}
					});
					savePlotData();
				}
				
				if (controlGui) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							target.updateUI(2);
						}
					});
				}

			} catch (IOException e) {
				System.exit(0);
				return;
			}

		} catch (IOException e1) {
			System.err.println(e1);
		}

	}

	private void parseData(String str) {

		StringTokenizer st = new StringTokenizer(str);
		String token;
		if (st.hasMoreTokens()) {
			token = st.nextToken();
		} else {
			if (!record) {
				SwingUtilities.invokeLater(new ConsoleMessage(str + "\n"));
			}
			return;
		}

		if (token.equals("Rep#:")) {

			if (!record) {
				SwingUtilities.invokeLater(new ConsoleMessage(str + "\n"));
				if (!alpha_decide && sim.USEPOPINFO && alpha != null) {
					StringTokenizer st2 = new StringTokenizer(str);
					st2.nextToken();

					boolean useAlpha = false;
					while (st2.hasMoreTokens()) {
						String expectAlpha = st2.nextToken();
						if (expectAlpha.length() >= 5
								&& (expectAlpha.substring(0, 5))
										.equals("Alpha")) {
							useAlpha = true;
							break;
						}
					}
					if (!useAlpha) {
						alpha = null;
						alphaHist = null;
					}

					st2 = null;
					alpha_decide = true;
				}

				if (!recStart && sim.RECOMBINE) {
					StringTokenizer st2 = new StringTokenizer(str);
					st2.nextToken();
					String expectRec = st2.nextToken();
					if (expectRec != null && expectRec.equals("Rec")) {
						recStart = true;
					}
				}
			}

			record = true;
			isStarted = true;
			return;

		}

		if (token.equals("MCMC")) {
			record = false;
			end = true;
			SwingUtilities.invokeLater(new ConsoleMessage("\n\n" + str + "\n"));
			return;
		}
		if (!record) {
			SwingUtilities.invokeLater(new ConsoleMessage(str + "\n"));
			return;
		}

		// "No Recombi ... finished"
		if (token.equals("No")) {
			recStart = true;
			SwingUtilities.invokeLater(new ConsoleMessage("\n" + str + "\n\n"));
			record = false; // force to reprint "Rep#" line
			return;
		}

		if (token.equals("Admixture")) {
			SwingUtilities.invokeLater(new ConsoleMessage("\n" + str + "\n"));
			record = false;
			return;
		}

		if (token.equals("BURNIN")) {
			SwingUtilities.invokeLater(new ConsoleMessage("\n" + str + "\n\n"));
			record = false;
			lnpdStart = true;
			return;
		}

		if (token.endsWith(":")) {
			token = token.substring(0, token.length() - 1);
		}

		int round = 0;
		try {
			round = Integer.parseInt(token);
		} catch (Exception e) {
		}

		if (round % freq != 0 && !lnpdStart) {
			return;
		}

		// else

		Vector<String> v = new Vector<String>();
		v.addElement(token);
		while (st.hasMoreTokens()) {
			v.addElement(st.nextToken());
		}
		if (round % freq == 0) {
			storeData(v);
		}
		if (lnpdStart) {
			parseHistData(v);
		}

		if (round % 100 == 0) {
			SwingUtilities.invokeLater(new ConsoleMessage(str + "\n"));
		}
		
		
		return;
	}

	private class ConsoleMessage implements Runnable {
		private String s;
		ConsoleMessage(String s) {
			this.s = s;
		}
		
		public void run() {
			target.getConsole().append(s);
		}	
	
	}
	
	private void parseHistData(Vector<String> v) {
		if (v == null || v.size() == 0) {
			return;
		}

		int size = v.size();
		int curr = 1;

		if (curr >= size) {
			return;
		}

		if (recStart) {
			if (recHist != null) {
				try {
					double data = Double
							.parseDouble(v.elementAt(curr));
					if (reccount < recHist.length) {
						recHist[reccount++] = data;
					}
				} catch (NumberFormatException e) {
					System.err.println(e);
				}
			}
			curr++;
		}

		if (curr >= size) {
			return;
		}

		if (alphaHist != null) {
			for (int i = 0; i < alphaHist.length; i++) {
				try {
					double data = Double.parseDouble(v
							.elementAt(curr++));
					if (alphacount < alphaHist[i].length) {
						alphaHist[i][alphacount] = data;
					}
				} catch (NumberFormatException e) {
					System.err.println(e);
				}
			}
			alphacount++;
		}

		if (fstHist != null) {
			for (int i = 0; i < fstHist.length; i++) {
				try {
					double data = Double.parseDouble(v
							.elementAt(curr++));
					if (fstcount < fstHist[i].length) {
						fstHist[i][fstcount] = data;
					}
				} catch (NumberFormatException e) {
					System.err.println(e);
				}
			}
			fstcount++;
		}

		if (srHist != null) {
			try {
				double data = Double.parseDouble(v.elementAt(curr++));
				if (rcount < srHist.length) {
					srHist[rcount++] = data;
				}
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
			rcount++;
		}

		if (likHist != null) {
			try {
				double data = Double.parseDouble(v.elementAt(curr++));
				if (likcount < likHist.length) {
					likHist[likcount++] = data;
				}
			} catch (NumberFormatException e) {
				System.err.println(e);
			}

		}

		if (curr >= size && sim.COMPUTEPROB) {
			// resize the lnpd histogram
			lnpdHist = new double[lnpdHist.length - 1];
			return;
		}

		if (lnpdHist != null) {
			try {
				double data = Double.parseDouble(v.elementAt(curr));
				if (lnpdcount < lnpdHist.length) {
					lnpdHist[lnpdcount++] = data;
				}
			} catch (NumberFormatException e) {
				System.err.println(e);
			}

		}

		return;
	}

	private void storeData(Vector<String> v) {
		if (v == null || v.size() == 0) {
			return;
		}

		String round = v.elementAt(0);
		int size = v.size();
		int curr = 1;

		if (curr >= size) {
			return;
		}

		if (recStart) {
			recmb.addElement(round);
			recmb.addElement(v.elementAt(curr++));

		}

		if (curr >= size) {
			return;
		}

		if (alpha != null) {
			for (int i = 0; i < alpha.size(); i++) {
				alpha.elementAt(i).addElement(round);
				alpha.elementAt(i).addElement(v.elementAt(curr++));
			}
		}

		if (curr >= size) {
			return;
		}

		if (fst != null) {
			for (int i = 0; i < fst.size(); i++) {
				fst.elementAt(i).addElement(round);
				fst.elementAt(i).addElement(v.elementAt(curr++));
			}
		}

		if (curr >= size) {
			return;
		}

		if (sr != null) {
			sr.addElement(round);
			sr.addElement(v.elementAt(curr++));
		}

		if (curr >= size) {
			return;
		}

		if (llhd != null) {
			llhd.addElement(round);
			llhd.addElement(v.elementAt(curr++));
		}

		if (curr >= size) {
			return;
		}

		if (lnpdStart && lnpd != null) {
			lnpd.addElement(round);
			lnpd.addElement(v.elementAt(curr));
		}

		return;

	}

	public double[] getNewData(String target, int seq, int datacount) {

		Vector<String> v = null;

		if (target.equals("Fst")) {
			if (fst == null) {
				return null;
			}
			if (seq >= fst.size()) {
				return null;
			}
			v = fst.elementAt(seq);
		}

		if (target.equals("Alpha")) {
			if (alpha == null) {
				return null;
			}
			if (seq >= alpha.size()) {
				return null;
			}
			v = alpha.elementAt(seq);
		}

		if (target.equals("Rec")) {
			if (recmb == null) {
				return null;
			}
			v = recmb;
		}

		if (target.equals("r")) {
			if (sr == null) {
				return null;
			}
			v = sr;
		}

		if (target.equals("Like")) {
			v = llhd;
		}

		if (target.equals("LnPD")) {
			v = lnpd;
		}

		if (v==null || v.size() < datacount + 2) {
			return null;
		}

		if (v.size() >= datacount + 4) {
			isFast = true;
		} else {
			isFast = false;
		}

		double[] data = new double[2];
		try {
			data[0] = Double.parseDouble(v.elementAt(datacount));
			data[1] = Double.parseDouble(v.elementAt(datacount + 1));
		} catch (NumberFormatException e) {
			System.err.println(e);
		}

		return data;
	}

	/**
	 * 
	 * use for retrieve the whole data set called by plotting class for post-run
	 * plotting
	 * 
	 **/

	private double[] getHistData(String target, int seq) {

		if (target.equals("Fst")) {
			if (fstHist == null) {
				return null;
			}
			if (seq >= fstHist.length) {
				return null;
			}

			return fstHist[seq];
		}

		if (target.equals("Alpha")) {
			if (alphaHist == null) {
				return null;
			}
			if (seq >= alphaHist.length) {
				return null;
			}
			return alphaHist[seq];
		}

		if (target.equals("Rec")) {
			return recHist;
		}
		if (target.equals("r")) {
			return srHist;
		}
		if (target.equals("Like")) {
			return likHist;
		}
		if (target.equals("Lnpd")) {
			return lnpdHist;
		}

		return null;
	}

	private Vector<String> getDataSet(String target, int seq) {

		Vector<String> v = null;

		if (target.equals("Fst")) {
			if (fst == null) {
				return null;
			}
			if (seq >= fst.size()) {
				return null;
			}
			v = fst.elementAt(seq);
		}

		if (target.equals("Alpha")) {
			if (alpha == null) {
				return null;
			}
			if (seq >= alpha.size()) {
				return null;
			}
			v = alpha.elementAt(seq);
		}

		if (target.equals("Rec")) {
			if (recmb == null) {
				return null;
			}
			v = recmb;
		}

		if (target.equals("r")) {
			if (sr == null) {
				return null;
			}
			v = sr;
		}

		if (target.equals("Like")) {
			v = llhd;
		}

		if (target.equals("LnPD")) {
			v = lnpd;
		}

		return v;
	}

	public boolean isEnd() {
		return end;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isRecStarted() {
		return recStart;
	}

	public boolean isKilled() {
		return isKilled;
	}

	public boolean isFast() {
		return isFast;
	}

	public boolean isBurinFinished() {
		return lnpdStart;
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

	private void savePlotData() {

		String simDir = sim.getSimPath();
		File f = new File(simDir, "PlotData");
		String saveDir = f.getAbsolutePath();

		if (fst != null) {

			double[][] fst_double = new double[fst.size()][];
			for (int i = 0; i < fst_double.length; i++) {
				fst_double[i] = vec2array(fst.elementAt(i));
			}
			PlotData pd = new PlotData(fst_double);
			pd.writeDataFile(saveDir, sim.getRstName() + "_fst");
		}

		if (alpha != null) {
			double[][] alpha_double = new double[alpha.size()][];
			for (int i = 0; i < alpha_double.length; i++) {
				alpha_double[i] = vec2logarray(alpha.elementAt(i));
			}
			PlotData pd = new PlotData(alpha_double);
			pd.writeDataFile(saveDir, sim.getRstName() + "_alpha");
		}

		if (llhd != null) {

			double[][] llhd_double = new double[1][];
			llhd_double[0] = vec2array(llhd);
			PlotData pd = new PlotData(llhd_double);
			pd.writeDataFile(saveDir, sim.getRstName() + "_lhd");
		}

		if (recmb != null) {
			double[][] recmb_double = new double[1][];
			recmb_double[0] = vec2array(recmb);
			PlotData pd = new PlotData(recmb_double);
			pd.writeDataFile(saveDir, sim.getRstName() + "_recmb");
		}

		if (sr != null) {
			double[][] sr_double = new double[1][];
			sr_double[0] = vec2array(sr);
			PlotData pd = new PlotData(sr_double);
			pd.writeDataFile(saveDir, sim.getRstName() + "_sr");
		}

		if (lnpd != null) {
			double[][] lnpd_double = new double[1][];
			lnpd_double[0] = vec2array(lnpd);
			PlotData pd = new PlotData(lnpd_double);
			pd.writeDataFile(saveDir, sim.getRstName() + "_lnpd");
		}

		if (fstHist != null) {
			PlotData pd = new PlotData(fstHist);
			pd.writeDataFile(saveDir, sim.getRstName() + "_fsthist");
		}
		if (alphaHist != null) {
			PlotData pd = new PlotData(alphaHist);
			pd.writeDataFile(saveDir, sim.getRstName() + "_alphahist");
		}

		if (lnpdHist != null) {
			double[][] lnpd_hist = new double[1][];
			lnpd_hist[0] = lnpdHist;
			PlotData pd = new PlotData(lnpd_hist);
			pd.writeDataFile(saveDir, sim.getRstName() + "_lnpdhist");
		}

		if (recHist != null) {
			double[][] rec_hist = new double[1][];
			rec_hist[0] = recHist;
			PlotData pd = new PlotData(rec_hist);
			pd.writeDataFile(saveDir, sim.getRstName() + "_recmbhist");
		}

		if (srHist != null) {
			double[][] sr_hist = new double[1][];
			sr_hist[0] = srHist;
			PlotData pd = new PlotData(sr_hist);
			pd.writeDataFile(saveDir, sim.getRstName() + "_srhist");
		}

		if (likHist != null) {

			double[][] lik_hist = new double[1][];
			lik_hist[0] = likHist;
			PlotData pd = new PlotData(lik_hist);
			pd.writeDataFile(saveDir, sim.getRstName() + "_lhdhist");
		}
	}

	public void killProc() {
		if (proc != null) {
			proc.destroy();
		}

		proc = null;
		if (controlGui) {
			target.updateUI(2);
		}
		isKilled = true;
	}

}
