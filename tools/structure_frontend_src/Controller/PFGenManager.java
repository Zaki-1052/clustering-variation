package Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import Gui.PFileGenDlg;
import Obj.NProjObj;
import Obj.NSimObj;
import Obj.ObjIO;

public class PFGenManager {

	private NProjObj proj;
	private PFileGenDlg dlg;

	public PFGenManager(StructureApp app, NProjObj proj, String[] simList) {

		this.proj = proj;

		dlg = new PFileGenDlg(simList);
		dlg.setManager(this);
		dlg.showDialog();

	}

	public void generateFile(String simName, int fromK, int toK, String path,
			boolean userSeed, int start_seed) {

		NSimObj sim = ObjIO.loadSimObj(proj, simName);
		if (sim == null) {
			return;
		}

		int seed = 0;
		if (userSeed) {
			seed = start_seed;
		}

		int counter = 0;

		for (int kcount = fromK; kcount <= toK; kcount++) {
			sim.setMAXPOPS(kcount);

			if (userSeed) {
				sim.setRNDSEED(seed + counter);
				counter++;
			}

			sim.setOutFile("");
			String filename = new String("mainparams." + simName + ".k"
					+ kcount);
			File paramFile = new File(path, filename);
			PrintStream out = OpenOutputFile(paramFile);
			out.println(sim.getConfig());
			out.close();
		}

		String postmessage = null;
		if (toK != fromK) {
			postmessage = "" + (toK - fromK + 1) + " parameter files generated";
		} else {
			postmessage = "1 parameter file generated";
		}

		JOptionPane.showMessageDialog(dlg, postmessage, null,
				JOptionPane.INFORMATION_MESSAGE);

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

}
