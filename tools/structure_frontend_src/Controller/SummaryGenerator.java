package Controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import Obj.NProjObj;
import Obj.NSimObj;
import Obj.ObjIO;

public class SummaryGenerator {

	private NProjObj proj;
	private String workingPath;
	private String[] simList;
	private String[][] tableData;
	private String[] header;
	private JInternalFrame currFrame;
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				saveSummary();
			} catch (Exception exception) {
			}
		}
	};

	public SummaryGenerator(NProjObj proj) {

		this.proj = proj;
		workingPath = proj.getWorkingPath();
		simList = proj.getSimList();

		Vector<String> testVec = new Vector<String>();
		for (int i = 0; i < simList.length; i++) {
			if (ObjIO.loadSimObj(proj, simList[i]) != null) {
				testVec.addElement(simList[i]);
			}
		}
		simList = new String[testVec.size()];
		for (int i = 0; i < testVec.size(); i++) {
			simList[i] = testVec.elementAt(i);
		}

	}

	public JInternalFrame getSummary() {

		if (simList == null || simList.length == 0) {
			return null;
		}

		SimData[] sims = new SimData[simList.length];

		int alphaCol = 0;
		int fstCol = 0;

		int rowNum = 0;

		for (int i = 0; i < simList.length; i++) {
			sims[i] = getSimSummary(ObjIO.loadSimObj(proj, simList[i]));
			sims[i].simName = simList[i];
			sims[i].sortData();
			rowNum += sims[i].data.length;
			if (sims[i].alphacol > alphaCol) {
				alphaCol = sims[i].alphacol;
			}
			if (sims[i].fstcol > fstCol) {
				fstCol = sims[i].fstcol;
			}

		}

		rowNum += simList.length;

		tableData = new String[rowNum][];
		int count = 0;
		for (int i = 0; i < simList.length; i++) {
			for (int j = 0; j < sims[i].data.length; j++) {
				tableData[count++] = sims[i].data[j].getStringArray(alphaCol,
						fstCol);
			}
			String[] es = new String[alphaCol + fstCol + 5];
			for (int k = 0; k < es.length; k++) {
				es[i] = new String();
			}
			tableData[count++] = es;
		}

		header = new String[5 + alphaCol + fstCol];
		header[0] = "Parameter Set";
		header[1] = "Run Name";
		header[2] = "K";
		header[3] = "Ln P(D)";
		header[4] = "Var[LnP(D)]";
		for (int i = 0; i < alphaCol; i++) {
			header[i + 5] = " \u03b1" + (i + 1);
		}
		for (int i = 0; i < fstCol; i++) {
			header[i + 5 + alphaCol] = "Fst_" + (i + 1);
		}

		final Object[][] data = tableData;
		final Object[] column = header;

		AbstractTableModel fixedModel = new AbstractTableModel() {
			/**
		 * 
		 */
			private static final long serialVersionUID = 1L;

			public int getColumnCount() {
				return 5;
			}

			public int getRowCount() {
				return data.length;
			}

			public String getColumnName(int col) {
				return (String) column[col];
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
		};

		AbstractTableModel model = new AbstractTableModel() {
			/**
		 * 
		 */
			private static final long serialVersionUID = 1L;

			public int getColumnCount() {
				return column.length - 5;
			}

			public int getRowCount() {
				return data.length;
			}

			public String getColumnName(int col) {
				return (String) column[col + 5];
			}

			public Object getValueAt(int row, int col) {
				return data[row][col + 5];
			}

			public void setValueAt(Object obj, int row, int col) {
				data[row][col + 5] = obj;
			}

			@SuppressWarnings("unused")
			public boolean CellEditable(int row, int col) {
				return true;
			}
		};

		JTable fixedTable = new JTable(fixedModel);
		JTable table = new JTable(model);
		fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumnModel colModel = fixedTable.getColumnModel();
		colModel.getColumn(1).setPreferredWidth(150);
		colModel.getColumn(2).setPreferredWidth(30);

		JScrollPane scroll = new JScrollPane(table);
		JViewport viewport = new JViewport();
		viewport.setView(fixedTable);
		viewport.setPreferredSize(fixedTable.getPreferredSize());
		scroll.setRowHeaderView(viewport);
		scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, fixedTable
				.getTableHeader());

		JLabel title = new JLabel("  Summary of Simulations");

		JPanel titlePane = new JPanel();

		titlePane.add(title, BorderLayout.CENTER);
		JMenuBar mbar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem mi = new JMenuItem("save as text file ...");
		mi.addActionListener(listener);
		menu.add(mi);
		mbar.add(menu);
		titlePane.setPreferredSize(new Dimension(400, 30));
		JInternalFrame frame = new JInternalFrame("", true, true, true, true);
		frame.setJMenuBar(mbar);
		currFrame = frame;
		frame.getContentPane().add(titlePane, BorderLayout.NORTH);
		frame.getContentPane().add(scroll, BorderLayout.CENTER);

		return frame;

	}

	private void saveSummary() {
		if (tableData == null || header == null) {
			return;
		}

		String filename = "";
		// ask for file name/dir to save image
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showDialog(currFrame, "Save");

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			// check if file exists
			if (file.exists()) {
				Object[] options = { "Yes", "No " };

				int n = JOptionPane
						.showOptionDialog(currFrame, "Target file "
								+ file.getName()
								+ " already exists, Overwrite it?",
								"Save image", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[1]);
				if (n == JOptionPane.NO_OPTION) {
					return;
				}
			}
			File fc_dir = file.getParentFile();
			if (!fc_dir.canWrite()) {
				JOptionPane.showMessageDialog(currFrame,
						"Can not write image file: permission denied", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			filename = file.getAbsolutePath();

		}

		File sumFile = new File(filename);
		PrintStream out = OpenOutputFile(sumFile);

		for (int i = 0; i < header.length; i++) {
			out.print("" + header[i] + "   ");
		}
		out.println("\n");

		for (int i = 0; i < tableData.length; i++) {
			if (tableData[i][tableData[i].length - 1] == null) {
				out.print("\n");
				continue;
			}

			for (int j = 0; j < tableData[i].length; j++) {
				out.print("" + tableData[i][j] + "  ");
			}
			out.print("\n");
		}

	}

	private static PrintStream OpenOutputFile(File file) {

		PrintStream out;
		try { // try to open output file as well...
			out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("**Error: unable to open output file.");
			out = null;
		}// try-catch
		catch (SecurityException e) {
			System.err.println("**Error: no permission to write output file.");
			out = null;
		}// try-catch

		return out;
	}

	private SimData getSimSummary(NSimObj sim) {

		@SuppressWarnings("unused")
		String simName = sim.getSimName();
		@SuppressWarnings("unused")
		int burnin = sim.BURNIN;
		@SuppressWarnings("unused")
		int numreps = sim.NUMREPS;
		@SuppressWarnings("unused")
		String amodel = new String();
		@SuppressWarnings("unused")
		String fmodel = new String();

		if (sim.USEPOPINFO) {
			amodel = "USEPOPINFO";
		} else if (sim.RECOMBINE) {
			amodel = "RECOMBINE";
		} else if (!sim.NOADMIX) {
			amodel = "ADMIXTURE";
		} else {
			amodel = "NO ADMIXTURE";
		}

		if (sim.FREQSCORR) {
			fmodel = "FREQ CORRELATE";
		} else if (sim.INFERLAMBDA) {
			fmodel = "INFERLAMBDA";
		} else {
			fmodel = "FREQ INDEPENDENT";
		}

		// System.out.println(amodel + "     "+fmodel);

		// Extract from Result Files
		File simDir = new File(workingPath, sim.getSimName());
		File rstDir = new File(simDir.getAbsolutePath(), "Results");

		File[] allFiles = rstDir.listFiles();
		Vector<File> fileVec = new Vector<File>();
		for (int i = 0; i < allFiles.length; i++) {
			String name = allFiles[i].getName();
			if (name.endsWith("_f")) {
				fileVec.addElement(allFiles[i]);
			}
		}

		RunData[] runData = new RunData[fileVec.size()];

		int max_alpha_col = 0;
		int max_fst_col = 0;

		for (int i = 0; i < fileVec.size(); i++) {

			runData[i] = readFile(fileVec.elementAt(i));
			runData[i].modelname = sim.getSimName();
			int alphaCol = runData[i].alpha.size();
			int fstCol = runData[i].fst.size();

			if (alphaCol > max_alpha_col) {
				max_alpha_col = runData[i].alpha.size();
			}

			if (fstCol > max_fst_col) {
				max_fst_col = fstCol;
			}
		}

		SimData simData = new SimData();
		simData.alphacol = max_alpha_col;
		simData.fstcol = max_fst_col;
		simData.data = runData;

		return simData;
	}

	private RunData readFile(File target) {

		RunData data = new RunData();
		String filename = target.getName();
		data.name = filename.substring(0, filename.length() - 2);

		BufferedReader infile = null;
		try {
			infile = new BufferedReader(new InputStreamReader(new FileInputStream(target.getAbsolutePath())));
		} catch (FileNotFoundException e) {
		}
		String str = "";
		StringTokenizer st = null;

		while (true) {
			try {
				str = infile.readLine();
			} catch (Exception re) {
				//TODO: bad ,bad, bad, fix in future
			}
			if (str == null) {
				break;
			}

			String rst = null;
			if (str.startsWith("Estimated Ln Prob of Data")) {
				st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					rst = st.nextToken();
				}
				data.lnpd = rst;
				continue;
			}

			if (str.startsWith("Variance of ln likelihood")) {
				st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					rst = st.nextToken();
				}
				data.varld = rst;
				continue;
			}

			if (str.startsWith("Mean value of alpha")) {
				st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					rst = st.nextToken();
				}
				data.alpha.addElement(rst);
				continue;
			}

			if (str.startsWith("Mean value of Fst")) {
				st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					rst = st.nextToken();
				}
				data.fst.addElement(rst);
				continue;
			}

			if (str.endsWith("populations assumed")) {
				st = new StringTokenizer(str);
				String sK = st.nextToken();
				try {
					data.K = Integer.parseInt(sK);
				} catch (Exception e) {
				}
				continue;
			}

		}

		return data;
	}

}

class SimData {
	String simName;
	int alphacol;
	int fstcol;
	RunData[] data;

	// sort data by K
	public void sortData() {

		if (data == null || data.length == 0) {
			return;
		}

		// insertion sort
		for (int i = 1; i < data.length; i++) {
			int currK = data[i].K;
			RunData currData = data[i];
			int j = 0;
			for (j = i - 1; j >= 0; j--) {
				if (currK > data[j].K) {
					break;
				}
				data[j + 1] = data[j];
			}

			data[j + 1] = currData;
		}

	}
}

class RunData {

	String modelname = " - ";
	String name = " - ";
	int K;
	String lnpd = " - ";
	String varld = " - ";
	Vector<String> fst = new Vector<String>();
	Vector<String> alpha = new Vector<String>();

	public String[] getStringArray(int alphaCol, int fstCol) {

		String[] sa = new String[5 + alphaCol + fstCol];
		sa[0] = modelname;
		sa[1] = name;
		sa[2] = "" + K;
		sa[3] = lnpd;
		sa[4] = varld;
		int count = 0;
		boolean ends = false;
		for (int i = 0; i < alphaCol; i++) {
			if (alpha.size() <= count) {
				ends = true;
			}
			if (!ends) {
				sa[i + 5] = alpha.elementAt(count++);

			} else {
				sa[i + 5] = " - ";
			}
		}

		count = 0;
		ends = false;
		for (int i = 0; i < fstCol; i++) {
			if (fst.size() <= count) {
				ends = true;
			}
			if (!ends) {
				sa[i + 5 + alphaCol] = fst.elementAt(count++);
			} else {
				sa[i + 5 + alphaCol] = " - ";
			}
		}
		return sa;
	}

}
