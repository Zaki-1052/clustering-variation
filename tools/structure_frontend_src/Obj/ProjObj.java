// 
//The project object  -- Data Model 
//

package Obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import Util.CopyFile;
import Util.DelDir;

public class ProjObj implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String workingPath; // the working dir;

	private String projName;
	private String projPath; // parent directory of project

	private Vector<String> simList;

	private boolean useIndLabel;
	private boolean usePopId;
	private boolean usePopFlag;
	private boolean useGeneName;
	private boolean useLocusName;
	private boolean usePhaseInfo;
	private boolean usePhenoType;
	private boolean oneRow;
	private boolean phased;
	private int extraCol = 0;
	private int numInds;
	private int ploidy;
	private int numloci;
	private String missingVal;
	private String dataFile;

	public ProjObj(String projName, String projPath, boolean useIndLabel,
			boolean usePopId, int extraCol, int numInds, int ploidy,
			int numloci, String missingVal, String dataFile,
			boolean usePopFlag, boolean useGeneName, boolean useLocusName,
			boolean usePhaseInfo, boolean oneRow, boolean phased,
			boolean usePhenoType) {

		this.projName = projName;
		this.projPath = projPath;
		this.useIndLabel = useIndLabel;
		this.usePopId = usePopId;
		this.extraCol = extraCol;
		this.numInds = numInds;
		this.ploidy = ploidy;
		this.numloci = numloci;
		this.missingVal = missingVal;
		this.dataFile = dataFile;
		this.usePopFlag = usePopFlag;
		this.useGeneName = useGeneName;
		this.useLocusName = useLocusName;
		this.usePhaseInfo = usePhaseInfo;
		this.oneRow = oneRow;
		this.phased = phased;
		this.usePhenoType = usePhenoType;
		simList = new Vector<String>();
	}

	public ProjObj() {
		simList = new Vector<String>();
	}

	//
	// The Bean Methods Set/Get Properties
	//

	public void setName(String s) {
		this.projName = s;
	}

	public String getName() {
		return projName;
	}

	public void setPath(String s) {
		this.projPath = s;
	}

	public String getPath() {
		return projPath;
	}

	public void setIndLabel(boolean b) {
		this.useIndLabel = b;
	}

	public boolean isIndLabel() {
		return useIndLabel;
	}

	public void setPopId(boolean b) {
		this.useIndLabel = b;
	}

	public boolean isPopId() {
		return useIndLabel;
	}

	public void setExtraCol(int col) {
		this.extraCol = col;
	}

	public int getExtraCol() {
		return extraCol;
	}

	public void setNumInds(int num) {
		this.numInds = num;
	}

	public int getNumInds() {
		return numInds;
	}

	public void setPloidy(int num) {
		this.ploidy = num;
	}

	public int getPloidy() {
		return ploidy;
	}

	public void setNumloci(int num) {
		this.numloci = num;
	}

	public int getNumloci() {
		return numloci;
	}

	public void setMissingVal(String s) {
		this.missingVal = s;
	}

	public String getMissingVal() {
		return missingVal;
	}

	public void setDataFile(String file) {
		this.dataFile = file;
	}

	public String getDataFile() {
		return dataFile;
	}

	public boolean getMapDistance() {
		return useLocusName;
	}

	public boolean getGeneName() {
		return useGeneName;
	}

	public boolean getOneRow() {
		return oneRow;
	}

	public boolean getPhaseInfo() {
		return usePhaseInfo;
	}

	public boolean getPhased() {
		return phased;
	}

	//
	// Ending of Bean Methods
	//

	//
	// Project Management Methods
	// 

	public void writeProjectFile() {
		String projFileName = projName.concat(".spj");

		// structure project file
		File f = new File(workingPath, projFileName);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
		}

	}

	public void createProjectSpace() {
		File f = new File(projPath, projName);
		f.mkdirs();
		workingPath = f.getAbsolutePath();
		// copy data file
		File df = new File(workingPath, "project_data");
		CopyFile.copyFile(dataFile, df.getAbsolutePath());
		dataFile = df.getAbsolutePath();
		writeProjectFile();
	}

	// every time project is opened , workingpath is reset
	// give user flexibility to move project data
	private void setWorkingPath(String path) {
		workingPath = path;
	}

	public String getWorkingPath() {
		return workingPath;
	}

	// executing when proj is loaded, detect the change
	// (removal of sim ) in inactive period
	private void setSimVector() {

		simList = new Vector<String>();
		File dir = new File(workingPath);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File simFile = new File(files[i], ".sim");
				if (simFile.exists()) {
					simList.addElement(files[i].getName());
				}
			}
		}

		return;
	}

	static public ProjObj loadProjObj(File f) {

		try {
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(f));

			ProjObj proj = (ProjObj) ois.readObject();
			ois.close();
			File dir = new File(f.getParent());
			proj.setWorkingPath(dir.getAbsolutePath());
			File df = new File(f.getParent(), "project_data");
			proj.setDataFile(df.getAbsolutePath());
			proj.setSimVector();
			return proj;

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Can not Read Project File",
					"Error", JOptionPane.ERROR_MESSAGE);
			System.err.println(e);
			return null;
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(null, "Fail to Open Project File",
					"Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

	}

	public String getConfig() {

		String str = "";

		str += new String("#define INFILE " + dataFile + "\n");
		str += new String("#define NUMINDS " + numInds + "\n");
		str += new String("#define NUMLOCI " + numloci + "\n");
		if (useIndLabel) {
			str += new String("#define LABEL 1 \n");
		} else {
			str += new String("#define LABEL 0 \n");
		}

		if (usePopId) {
			str += new String("#define POPDATA 1 \n");
		} else {
			str += new String("#define POPDATA 0 \n");
		}

		if (usePopFlag) {
			str += new String("#define POPFLAG 1 \n");
		} else {
			str += new String("#define POPFLAG 0 \n");
		}
		if (usePhenoType) {
			str += new String("#define PHENOTYPE 1 \n");
		} else {
			str += new String("#define PHENOTYPE 0 \n");
		}
		if (useGeneName) {
			str += new String("#define MARKERNAMES 1 \n");
		} else {
			str += new String("#define MARKERNAMES 0 \n");
		}

		if (useLocusName) {
			str += new String("#define MAPDISTANCES 1 \n");
		} else {
			str += new String("#define MAPDISTANCES 0 \n");
		}

		if (oneRow) {
			str += new String("#define ONEROWPERIND 1 \n");
		} else {
			str += new String("#define ONEROWPERIND 0 \n");
		}

		if (usePhaseInfo) {
			str += new String("#define PHASEINFO 1 \n");
		} else {
			str += new String("#define PHASEINFO 0 \n");
		}

		if (phased) {
			str += new String("#define PHASED 1 \n");
		} else {
			str += new String("#define PHASED 0 \n");
		}

		str += new String("#define EXTRACOLS " + extraCol + "\n");

		str += new String("#define MISSING " + missingVal + "\n");

		str += new String("#define PLOIDY " + ploidy + "\n");

		return str;
	}

	// well formatted project information for display
	public String printProjInfo() {

		String str = new String("\n");

		str += new String(
				"         =============== Project Information ============== \n\n");
		str += new String("                  Project Name: " + projName + "\n");
		str += new String("                  Project Path: " + workingPath
				+ "\n");
		str += new String("                  Data File: " + dataFile + "\n\n\n");
		str += new String("                         Data Information\n\n");
		str += new String("                  Number of Individuals: " + numInds
				+ "\n");
		str += new String("                  Number of Loci: " + numloci + "\n");
		str += new String("                  Ploidy: " + ploidy + "\n");
		str += new String("                  Missing Data is Represented by: "
				+ missingVal + "\n\n\n");
		str += new String("                         Data Format\n\n");
		if (useIndLabel) {
			str += new String(
					"                  Data Contains Individual Labels \n");
		}
		if (usePopId) {
			str += new String(
					"                  Data Contains Population Identifiers \n");
		}
		if (usePopFlag) {
			str += new String(
					"                  Data Contains USEPOPINFO Selection Flags \n");
		}
		if (useGeneName) {
			str += new String(
					"                  Data Contains Row of Marker Names \n");
		}
		if (useLocusName) {
			str += new String(
					"                  Data Contains Map Distances between Loci \n");
		}
		if (usePhaseInfo) {
			str += new String(
					"                  Data Contains Phase Information \n");
		}
		if (usePhenoType) {
			str += new String("                  Data Contains Phenotype \n");
		}
		if (oneRow) {
			str += new String(
					"                  Data File Strores Data for Individuals in a Single Line \n");
		}
		if (phased) {
			str += new String("                  Data are phased \n");
		}
		if (extraCol > 0) {
			str += new String(
					"                  Total Number of Extra Columns: "
							+ extraCol + "\n");
		}

		return str;
	}

	//
	// try to load data from the specified source, check
	// the data format
	//

	public int getColNum() {
		return numloci + extraCol;
	}

	public String[] getDataTitle() {

		int dataCol = numloci;
		if (oneRow) {
			dataCol = numloci * ploidy;
		}

		int sideCol = extraCol;

		if (useIndLabel) {
			sideCol++;
		}
		if (usePopId) {
			sideCol++;
		}
		if (usePopFlag) {
			sideCol++;
		}
		if (usePhenoType) {
			sideCol++;
		}

		String[] title = new String[dataCol + sideCol];

		int count = 0;

		if (useIndLabel) {
			title[count++] = "Label";
		}
		if (usePopId) {
			title[count++] = "Pop ID";
		}
		if (usePopFlag) {
			title[count++] = "Flag";
		}
		if (usePhenoType) {
			title[count++] = "Phenotype";
		}

		for (int i = 0; i < extraCol; i++) {
			title[count + i] = "Extra " + (i + 1);
		}

		count += extraCol;
		int skip = 1;
		if (oneRow) {
			skip = ploidy;
		}

		int loc_count = 1;
		int loc = count;

		for (int i = count; i < title.length; i++) {
			title[i] = "";
		}

		for (int i = 0; i < numloci; i++) {
			title[loc] = "Locus " + loc_count;
			loc += skip;
			loc_count++;
		}

		return title;
	}

	public String[][] loadData() {

		// find the file row number
		int fileRow = 0;
		int rowcount = ploidy;
		if (oneRow) {
			rowcount = 1;
		}
		if (usePhaseInfo) {
			rowcount += 1;
		}
		fileRow = rowcount * numInds;
		int extraRow = 0;
		if (useGeneName) {
			extraRow++;
		}
		if (useLocusName) {
			extraRow++;
		}

		fileRow += extraRow;

		// find the file col number
		int fileCol = numloci;
		if (oneRow) {
			fileCol = numloci * ploidy;
		}

		int sideCol = extraCol;

		if (useIndLabel) {
			sideCol++;
		}
		if (usePopId) {
			sideCol++;
		}
		if (usePopFlag) {
			sideCol++;
		}
		if (usePhenoType) {
			sideCol++;
		}

		fileCol += sideCol;

		String[][] data = new String[fileRow][fileCol];

		// init the data
		for (int i = 0; i < fileRow; i++) {
			for (int j = 0; j < fileCol; j++) {
				data[i][j] = "  ";
			}
		}

		StringBuffer buffer = new StringBuffer();
		String content = "";

		try {
			FileInputStream fis = new FileInputStream(dataFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");

			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);

			}
			in.close();
			content = buffer.toString();
		} catch (IOException e) {
			System.out.println(buffer);
			JOptionPane.showMessageDialog(null, "Can not Read Data Source",
					"Error", JOptionPane.ERROR_MESSAGE);

			return null;
		}

		StringTokenizer file_st = new StringTokenizer(content, "\n");

		String str = new String();
		int count = 0;
		int pcount = 0;
		int phasedata_counter = ploidy;

		int interval = 0;
		if (oneRow) {
			phasedata_counter = 1;
			interval = ploidy - 1;
		}

		while (file_st.hasMoreTokens()) {
			str = file_st.nextToken();

			StringTokenizer st = new StringTokenizer(str);
			if (!st.hasMoreTokens()) {
				continue;
			}

			if (count < extraRow) {
				if (!parseData(data, count, str, sideCol, 0, interval)) {
					return null;
				}
				count++;
				continue;
			}

			if (usePhaseInfo && pcount == phasedata_counter) {
				if (!parseData(data, count, str, sideCol, 0, interval)) {
					return null;
				}
				pcount = 0;
				count++;
				continue;
			}

			if (!parseData(data, count, str, sideCol, 1, 0)) {
				return null;
			}
			count++;
			pcount++;
		}
		if (count < data.length) {
			JOptionPane.showMessageDialog(null, "Bad Format in Data Source: \n"
					+ "Expect " + data.length + " rows, currently have "
					+ count + " rows", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		return data;
	}

	private boolean parseData(String[][] data, int count, String str,
			int sideCol, int type, int interval) {

		String msg = "";
		StringTokenizer st = new StringTokenizer(str);
		int scount = 0;
		if (type == 0) {
			scount = sideCol;
		}

		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			try {
				data[count][scount] = s;
				scount += interval + 1;
			} catch (ArrayIndexOutOfBoundsException e) {
				// more data than expected

				if (count >= data.length) {
					msg = "Number of rows are more than expected ("
							+ data.length + ")";
				} else {
					msg = "Data entries are more than expected ("
							+ data[count].length + ") at line " + (count + 1);
				}

				JOptionPane.showMessageDialog(null,
						"Bad Format in Data Source: \n" + msg, "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		if (scount != data[0].length) {
			msg = "Expect " + data[0].length + " data entries at line "
					+ (count + 1);
			JOptionPane.showMessageDialog(null, "Bad Format in Data Source: \n"
					+ msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;

	}

	public boolean checkSimName(String simName) {

		if (simName == null) {
			return false;
		}
		if (simList.size() == 0) {
			return true;

		}
		for (int i = 0; i < simList.size(); i++) {
			String name = simList.elementAt(i);
			// duplicated sim name found
			if (name.equals(simName)) {

				return false;
			}
		}
		return true;
	}

	public boolean addSimObj(String simName) {

		if (checkSimName(simName)) {
			simList.add(simName);
			return true;
		}
		return false;
	}

	public boolean removeSimObj(String simName) {
		for (int i = 0; i < simList.size(); i++) {
			String name = simList.elementAt(i);
			// target name found
			if (name.compareTo(simName) == 0) {
				// Get it! then remove it
				simList.remove(i);
				i--;
				// do the filesystem clean-up
				File simdir = new File(workingPath, simName);
				DelDir.removeDir(simdir);
				return true;
			}
		}
		return false;
	}

	//
	//
	// load simulation object by specifying the name
	// if success, return the SimObj
	// else return null
	//
	//

	public SimObj getSimObj(String simName) {

		for (int i = 0; i < simList.size(); i++) {
			String name = simList.elementAt(i);
			// target name found
			if (name.compareTo(simName) == 0) {
				File simDir = new File(workingPath, simName);
				String simPath = simDir.getAbsolutePath();
				File simFile = new File(simPath, ".sim");
				SimObj sim = SimObj.loadSimObj(simFile);
				if (sim != null) {
					sim.setProjObj(this);
				}
				return sim;
			}
		}

		return null;
	}

	public String[] getSimList() {
		String[] list = new String[simList.size()];
		for (int i = 0; i < simList.size(); i++) {
			list[i] = simList.elementAt(i);
		}
		return list;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {

		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		in.defaultReadObject();
	}

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
