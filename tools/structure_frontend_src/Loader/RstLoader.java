package Loader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import Gui.RstDlg;
import Plot.AlphaPlotManager;
import Plot.FstPlotManager;
import Plot.LikePlotManager;
import Plot.LnpdPlotManager;
import Plot.RecmbPlotManager;
import Plot.RstPlotManager;
import Triangle.TrianglePlotManager;

public class RstLoader implements ActionListener {

	private JFrame frame;
	private Decoder decoder;
	private String rstFile;
	private String runtimeFile;

	// plotting tools
	private FstPlotManager fstpm;
	private AlphaPlotManager alphapm;
	private LikePlotManager likepm;
	private LnpdPlotManager lnpdpm;
	private RstPlotManager rpm;
	private RecmbPlotManager recpm;
	private TrianglePlotManager tripm;

	private RstDlg fileDlg;

	public RstLoader() {

		fstpm = new FstPlotManager();
		alphapm = new AlphaPlotManager();
		likepm = new LikePlotManager();
		lnpdpm = new LnpdPlotManager();
		recpm = new RecmbPlotManager();

		fileDlg = new RstDlg(null);
		int feedback = fileDlg.showDialog();

		if (feedback == JOptionPane.OK_OPTION) {
			showResult(fileDlg.getRstFile(), fileDlg.getRtmFile());
		}
		fileDlg.dispose();
	}

	private void showResult(String rstFile, String runtimeFile) {

		this.rstFile = rstFile;
		this.runtimeFile = runtimeFile;

		if (decoder != null) {
			decoder = null;
		}

		decoder = new Decoder(rstFile, runtimeFile);

		if (runtimeFile != null) {
			decoder.readRuntimeData();
		}
		if (frame != null) {
			frame.dispose();
		}

		frame = new JFrame("Show results");

		String content = decoder.getRstContent();
		JTextArea ta = new JTextArea(content);
		ta.setMargin(new Insets(20, 50, 20, 40));
		ta.setFont(new Font("TimesRoman", Font.PLAIN, 15));
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);

		JScrollPane sp = new JScrollPane(ta);
		ta.setCaretPosition(0);
		ta.setEditable(false);
		sp.setPreferredSize(new Dimension(850, 600));
		JMenuBar mbar = new JMenuBar();
		JMenu fmenu = new JMenu("File");
		JMenuItem fo = new JMenuItem("Load results ...");
		fo.setActionCommand("open");
		fo.addActionListener(this);
		fmenu.add(fo);
		JMenu bmenu = new JMenu("Bar plot");
		JMenuItem mi = new JMenuItem("Show");
		mi.setActionCommand("barplot");
		mi.addActionListener(this);
		bmenu.add(mi);

		JMenu dmenu = new JMenu("Data plot");
		JMenuItem mfst = new JMenuItem("Fst");

		// if( (new File(plotDir,filename+"_fst")).exists()){
		if (decoder.fst_array != null) {
			mfst.setActionCommand("sviewfst");
			mfst.addActionListener(this);
		} else {
			mfst.setEnabled(false);
		}
		JMenuItem malpha = new JMenuItem("Alpha");
		// if( (new File(plotDir,filename+"_alpha")).exists()){
		if (decoder.alpha_array != null) {
			malpha.setActionCommand("sviewalpha");
			malpha.addActionListener(this);
		} else {
			malpha.setEnabled(false);
		}
		JMenuItem mrecomb = new JMenuItem("Recombination rate");
		// if( (new File(plotDir,filename+"_recmb")).exists()){
		if (decoder.rec_array != null) {
			mrecomb.setActionCommand("sviewrec");
			mrecomb.addActionListener(this);
		} else {
			mrecomb.setEnabled(false);
		}
		JMenuItem mlike = new JMenuItem("Likelihood");
		// if( (new File(plotDir,filename+"_lhd")).exists()){
		if (decoder.llhd_array != null) {
			mlike.setActionCommand("sviewlike");
			mlike.addActionListener(this);
		} else {
			mlike.setEnabled(false);
		}
		JMenuItem mlnpd = new JMenuItem("Ln P(D) ");
		// if( (new File(plotDir,filename+"_lnpd")).exists()){
		if (decoder.lnpd_array != null) {
			mlnpd.setActionCommand("sviewlnpd");
			mlnpd.addActionListener(this);
		} else {
			mlnpd.setEnabled(false);
		}
		dmenu.add(mfst);

		dmenu.add(malpha);
		dmenu.add(mrecomb);
		dmenu.add(mlike);
		dmenu.add(mlnpd);

		JMenu hmenu = new JMenu("Histogram");
		JMenuItem mhfst = new JMenuItem("Fst");

		if (decoder.fst_array != null) {
			mhfst.setActionCommand("histfst");
			mhfst.addActionListener(this);
		} else {
			mhfst.setEnabled(false);
		}

		JMenuItem mhalpha = new JMenuItem("Alpha");
		if (decoder.alpha_array != null) {
			mhalpha.setActionCommand("histalpha");
			mhalpha.addActionListener(this);
		} else {
			mhalpha.setEnabled(false);
		}

		JMenuItem mhrecomb = new JMenuItem("Recombination rate");
		if (decoder.rec_array != null) {
			mhrecomb.setActionCommand("histrec");
			mhrecomb.addActionListener(this);
		} else {
			mhrecomb.setEnabled(false);
		}

		JMenuItem mhlike = new JMenuItem("Likelihood");
		if (decoder.llhd_array != null) {
			mhlike.setActionCommand("histlike");
			mhlike.addActionListener(this);
		} else {
			mhlike.setEnabled(false);
		}

		JMenuItem mhlnpd = new JMenuItem("Ln P(D) ");
		if (decoder.llhd_array != null) {
			mhlnpd.setActionCommand("histlnpd");
			mhlnpd.addActionListener(this);
		} else {
			mhlnpd.setEnabled(false);
		}
		JMenu tmenu = new JMenu("Triangle plot");
		JMenuItem trimenu = new JMenuItem("Show");
		trimenu.setActionCommand("triplot");
		trimenu.addActionListener(this);
		tmenu.add(trimenu);

		hmenu.add(mhfst);

		hmenu.add(mhalpha);
		hmenu.add(mhrecomb);
		hmenu.add(mhlike);
		hmenu.add(mhlnpd);

		mbar.add(fmenu);
		mbar.add(bmenu);
		mbar.add(dmenu);
		mbar.add(hmenu);
		mbar.add(tmenu);

		frame.setJMenuBar(mbar);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(sp, BorderLayout.CENTER);
		frame.setLocation(300, 200);
		frame.pack();
		frame.setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.compareTo("sviewfst") == 0) {
			fstpm.plot(decoder.fst_array, runtimeFile);
			/*
			 * for(int i=0;i<decoder.fst_array[0].length;i++)
			 * System.out.println(decoder.fst_array[0][i]);
			 */

			return;
		}
		if (action.equals("sviewalpha")) {
			alphapm.plot(decoder.alpha_array, runtimeFile);
			return;
		}
		if (action.equals("sviewrec")) {
			recpm.plot(decoder.rec_array, runtimeFile);
			return;
		}
		if (action.equals("sviewlnpd")) {
			lnpdpm.plot(decoder.lnpd_array, runtimeFile);
			return;
		}
		if (action.equals("sviewlike")) {
			likepm.plot(decoder.llhd_array, runtimeFile);
			return;
		}

		if (action.equals("barplot")) {
			if (rpm != null) {
				rpm.dispose();
				rpm = null;
			}
			rpm = new RstPlotManager(frame, rstFile, rstFile);
		}

		// hist plotters

		if (action.equals("histrec")) {
			recpm.plotHist(decoder.getHistArray(1), runtimeFile);
			return;
		}

		if (action.equals("histfst")) {
			fstpm.plotHist(decoder.getHistArray(3), runtimeFile);
			return;
		}

		if (action.equals("histalpha")) {
			alphapm.plotHist(decoder.getHistArray(2), runtimeFile);
			return;
		}

		if (action.equals("histlike")) {
			likepm.plotHist(decoder.getHistArray(4), runtimeFile);
			return;
		}
		if (action.equals("histlnpd")) {
			likepm.plotHist(decoder.getHistArray(5), runtimeFile);
			return;
		}

		if (action.equals("open")) {
			fileDlg = new RstDlg(frame);
			int feedback = fileDlg.showDialog();

			if (feedback == JOptionPane.OK_OPTION) {
				showResult(fileDlg.getRstFile(), fileDlg.getRtmFile());
			}
			fileDlg.dispose();
		}

		if (action.equals("triplot")) {
			tripm = new TrianglePlotManager();
			tripm.showFrame(null, rstFile);
		}

	}

}
