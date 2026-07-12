package Gui.Wizard;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Obj.NProjObj;
import Util.FileCounter;

public class WizardController extends WindowAdapter implements ActionListener {
	// The all 4 wizard windows

	private Wizard_1 w1;
	private Wizard_2 w2;
	private Wizard_3 w3;
	private Wizard_4 w4;
	private JFrame main;
	private Point pos;

	private File dataFile;

	// window control varible
	@SuppressWarnings("unused")
	private JDialog currDlg;
	private boolean isDone;

	// date member controlled
	private NProjObj projObj;
	private String[][] data; // the data from source file

	// use to set up connection objects
	public WizardController(JFrame main) {
		this.main = main;
		isDone = false;
	}

	public boolean isDone() {
		return isDone;
	}

	public void initWizard() {
		w1 = new Wizard_1(main, this);
		w2 = new Wizard_2(main, this);
		w3 = new Wizard_3(main, this);
		w4 = new Wizard_4(main, this);

		currDlg = w1;
		w1.setLocationRelativeTo(main);
		w1.showDialog();
		return;

	}

	public void windowClosing(WindowEvent e) {
		projObj = null;
		destroyAll();
		super.windowClosing(e);
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.compareTo("w1_next") == 0) {
			pos = w1.getLocation();

			String filename = w1.getDataFile();

			dataFile = new File(filename);
			if (!dataFile.exists()) {
				dataFile = null;
				JOptionPane.showMessageDialog(null, "Invalid data file",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			w1.dispose();
			w2.setLocation(pos);
			w2.showDialog();
			currDlg = w2;
			return;
		}

		if (action.compareTo("w2_next") == 0) {
			pos = w2.getLocation();
			w2.dispose();
			w3.setLocation(pos);
			w3.showDialog(w2.getPloidy());
			currDlg = w3;
			return;
		}

		if (action.compareTo("w3_next") == 0) {
			pos = w3.getLocation();
			w3.dispose();
			w4.setLocation(pos);
			w4.showDialog();
			currDlg = w4;
			return;
		}

		if (action.compareTo("w4_back") == 0) {
			pos = w4.getLocation();
			w4.dispose();
			w3.setLocation(pos);
			w3.showDialog(w2.getPloidy());
			currDlg = w3;
			return;
		}

		if (action.compareTo("w3_back") == 0) {
			pos = w3.getLocation();
			w3.dispose();
			w2.setLocation(pos);
			w2.showDialog();
			currDlg = w2;
			return;
		}

		if (action.compareTo("w2_back") == 0) {
			pos = w2.getLocation();
			w2.dispose();
			w1.setLocation(pos);
			w1.showDialog();
			currDlg = w1;
			return;
		}

		if (action.compareTo("w1_cancel") == 0) {
			// destroy everything then return
			w1.dispose();
			destroyAll();
			return;
		}

		if (action.compareTo("w2_cancel") == 0) {
			// destroy everything then return
			w2.dispose();
			destroyAll();
			return;
		}

		if (action.compareTo("w3_cancel") == 0) {
			// destroy everything then return
			w3.dispose();
			destroyAll();
			return;
		}

		if (action.compareTo("w4_cancel") == 0) {
			// destroy everything then return
			w4.dispose();
			destroyAll();
			return;
		}

		if (action.compareTo("finish") == 0) {
			buildProjObj();
			if (projObj != null) {
				w4.dispose();
				destroyAll();
			}
			return;
		}

		if (action.equals("datainfo")) {
			if (dataFile == null) {
				JOptionPane.showMessageDialog(null,
						"No valid data file is specified", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			FileCounter fc = new FileCounter(dataFile);
			fc.report();
			return;
		}

	}

	private void destroyAll() {
		w1 = null;
		w2 = null;
		w3 = null;
		w4 = null;
		isDone = true;
	}

	private void buildProjObj() {

		//
		// check all user input when user trying to build the project
		// if errors found return the string containing error messages
		// and build nothing
		//

		String errString = "";

		String name = w1.getProjName();
		String path = w1.getProjDir();
		File projParent = new File(path);
		if (!projParent.exists()) {
			errString += new String(
					"\n* The directory to save project does not exist");
		}

		boolean indlabel = w4.getIndLabel();
		boolean popId = w4.getPopId();
		boolean popflag = w4.getPopFlag();
		boolean genename = w3.getGeneName();
		boolean locusname = w3.getLocusName();
		boolean phaseinfo = w3.getPhaseInfo();
		boolean onerow = w3.getOneRow();
		boolean phased = false;
		boolean phenotype = w4.getPheno();
		boolean recessiveAllele = w3.getRecessiveAllele();
		boolean locData = w4.getLocData();
		String extraStr = w4.getExtraCols();
		String notambiguous = "";

		int extraCol = 0;
		if (extraStr == null) {
			extraCol = 0;
		} else {
			try {
				extraCol = Integer.parseInt(extraStr);
			} catch (NumberFormatException e) {
				errString += new String(
						"\n* Input in \"Extra Column\" Field is not an Integer");
			}
		}

		String numIndStr = w2.getNumInds();
		int numInds = 0;
		try {
			numInds = Integer.parseInt(numIndStr);
		} catch (NumberFormatException e) {
			errString += new String(
					"\n* Input in \"Number of Individuals\" Field is not an Integer");
		}

		String ploidyStr = w2.getPloidy();
		int ploidy = 0;
		try {
			ploidy = Integer.parseInt(ploidyStr);
		} catch (NumberFormatException e) {
			errString += new String(
					"\n* Input in \"Number of Ploidy\" Field is not an Integer");
		}

		String lociStr = w2.getNumLoci();
		int numLoci = 0;
		try {
			numLoci = Integer.parseInt(lociStr);
		} catch (NumberFormatException e) {
			errString += new String(
					"\n* Input in \"Number of Loci\" Field is not an Integer");
		}

		String missingVal = w2.getMissingVal();

		String dataFile = w1.getDataFile();
		if (dataFile != null) {
			File f = new File(dataFile);
			if (!f.exists()) {
				errString += new String("\n* The Data Source does not exist");
			}
		}
		// End of data validation

		// if some errors detected
		if (errString.compareTo("") != 0) {
			String prefix = "Errors detected when building project:\n\n";
			errString = prefix.concat(errString);
			errString += new String("\n\n");
			JOptionPane.showMessageDialog(w4, errString, "Input errors",
					JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			// show a confirm message board
			String msg = "You are about to build a Structure project with following settings:\n\n";
			msg += new String("Project Name:  " + name);
			msg += new String("\nProject Path:  " + path);
			if (dataFile != null) {
				msg += new String("\nData Source:  " + dataFile + "\n\n");
			} else {
				msg += new String("\nData Source: Manually Input\n\n");
			}
			msg += new String("\n========================================\n");
			msg += new String("\nNumber of Individuals:  " + numInds);
			msg += new String("\nNumber of Loci:  " + numLoci);
			msg += new String("\nNumber of Ploidy:  " + ploidy);
			msg += new String("\nMissing Value represented as:  " + missingVal
					+ "\n");
			msg += new String("\n========================================\n");

			if (genename) {
				msg += new String("\nData File Contains Row of Marker Names");
			}
			if (locusname) {
				msg += new String(
						"\nData File Contains Row of Map Distances between Loci");
			}
			if (phaseinfo) {
				msg += new String("\nData File Contains Phase Information");
			}
			if (onerow) {
				msg += new String(
						"\nData File Stores Data for Individuals in a Single Line");
			}
			if (indlabel) {
				msg += new String("\nData File Contains Individual Labels");
			}
			if (popId) {
				msg += new String("\nData File Contains Population Identifiers");
			}
			if (locData) {
				msg += new String(
						"\nData File Contains Sampling Location Information");
			}
			if (popflag) {
				msg += new String(
						"\nData File Contains USEPOPINFO Selection Flagn");
			}
			if (phenotype) {
				msg += new String("\nData File Contains Phenotpye Information");
			}
			msg += new String("\nNumber of Other Extra Columns:  " + extraCol
					+ "\n");

			msg += new String("\n========================================\n");

			Object[] options = { "Proceed", "Go back" };
			int n = JOptionPane.showOptionDialog(w4, msg, "Comfirmation",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[0]);
			if (n == JOptionPane.NO_OPTION) {
				return;
			}

		}

		// one last qustion

		if (ploidy == 1) {
			phased = true;
		}

		if ((ploidy >= 3 && locusname)
				|| (ploidy == 2 && !phaseinfo && locusname)) {
			Object[] options = { " Yes ", "  No " };
			int n = JOptionPane.showOptionDialog(null, "Are the data phased ?",
					"One more question", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (n == JOptionPane.YES_OPTION) {
				phased = true;
			} else {
				phased = false;
			}
		}

		if (ploidy >= 3 && recessiveAllele) {

			String sC = null;
			while (sC == null) {
				sC = JOptionPane
						.showInputDialog("Indicator code for locus without genotypic ambiguity");
				if (sC.equals(missingVal)) {
					JOptionPane
							.showMessageDialog(
									null,
									"Indicator code must be different from missing value",
									"Error", JOptionPane.ERROR_MESSAGE);
					sC = null;
				}
			}
			notambiguous = sC;
		}

		// if reaches here build the project
		projObj = new NProjObj(name, path, indlabel, popId, extraCol, numInds,
				ploidy, numLoci, missingVal, dataFile, popflag, genename,
				locusname, phaseinfo, onerow, phased, phenotype,
				recessiveAllele, locData);

		if (ploidy >= 3 && recessiveAllele) {
			projObj.setNAMBCode(notambiguous);
		}

		data = projObj.loadData();
		if (data == null) {
			// bad data format, discard this projObj
			String expection = new String(
					"The data file is expected to have following format:\n\n");
			if (genename) {
				expection += "1 row with " + numLoci
						+ " entries (marker name) \n";
			}
			if (recessiveAllele) {
				expection += "1 row with " + numLoci
						+ " entries (recessive allele) \n";
			}
			if (locusname) {
				expection += "1 row with " + numLoci
						+ " entries (map distance) \n";
			}
			if (phaseinfo) {
				expection += "" + numInds + " rows with " + numLoci
						+ " entries (phase information)\n";
			}
			int rowcount = ploidy;
			if (onerow) {
				rowcount = 1;
			}
			int datarow = rowcount * numInds;
			int colcount = numLoci;
			if (onerow) {
				colcount = numLoci * ploidy;
			}
			if (indlabel) {
				colcount++;
			}
			if (popId) {
				colcount++;
			}
			if (locData) {
				colcount++;
			}
			if (popflag) {
				colcount++;
			}
			colcount += extraCol;
			expection += "" + datarow + " rows with " + colcount
					+ " entries (data)\n\n";
			JOptionPane.showMessageDialog(null, expection, "Structure Wizard",
					JOptionPane.INFORMATION_MESSAGE);

			projObj = null;
		}
	}

	public File getDataFile() {
		return dataFile;
	}

	public String[][] getData() {
		return data;
	}

	public NProjObj getProjObj() {
		return projObj;
	}

	// Utility Method //

	/**********************************************************/
	/***** opens a file for reading, returning a file ref *****/
	@SuppressWarnings("unused")
	private static InputStream OpenInputFile(String filename) {

		InputStream in;

		try { // try to open input file so we can input data...
			in = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			System.err.println("**Error: unable to open input file.");
			in = null;
		}// try-catch

		return in;
	}

}
