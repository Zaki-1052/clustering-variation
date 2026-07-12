package Controller;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import Gui.JobLogDlg;
import Gui.MainFrame;
import Gui.ProjTree;
import Obj.NProjObj;
import Obj.NSimObj;
import Obj.ObjIO;
import Proc.AppProc;

public class JobManager extends Thread {

	private NProjObj proj;
	private String[] simList;
	private MainFrame mainFrame;
	private ProjTree projTree;
	private int iteration, fromK, toK, seed;
	private boolean requireSeed;
	private JTextArea log;
	private AppProc runningThread;
	private StructureApp app;
	private boolean receivedKillCommand;

	public JobManager(StructureApp app, NProjObj proj, String[] simList,
			int iteration, int fromK, int toK, MainFrame mainFrame,
			ProjTree projTree, boolean requireSeed, int seed) {

		this.app = app;
		this.proj = proj;
		this.simList = simList;
		this.iteration = iteration;
		this.mainFrame = mainFrame;
		this.projTree = projTree;
		this.fromK = fromK;
		this.toK = toK;
		this.requireSeed = requireSeed;
		this.seed = seed;
		this.receivedKillCommand = false;
		log = new JTextArea();
	}

	public void displayLog(final String str) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				log.append(str);
			}
		});
	}
	
	public void run() {
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				mainFrame.updateUI(4);
				log.setFont(new Font("TimesRoman", Font.PLAIN, 12));
				log.setLineWrap(true);
				log.setWrapStyleWord(true);
				log.setMargin(new Insets(10, 10, 10, 10));
				log.setText("");
			}
		});
		
		//mainFrame.updateUI(4);
		
		if (proj == null) {
			displayLog("No Project Information: Fatal Error, Stop the Job\n");
			return;
		}

		int counter = 0;

		displayLog("Start Job ... \n\n");
		for (int i = 0; i < simList.length; i++) {
			if (this.receivedKillCommand) {
				return;
			}
			displayLog("Loading Simulation " + simList[i] + " ...\n\n");
			NSimObj sim = ObjIO.loadSimObj(proj, simList[i]);

			if (sim == null) {
				continue;
			}

			/*
			if (sim == null) {
				displayLog("Can Not Load the Simulation, Skip ... \n");
				continue;
			}
			*/

			int count = 1;
			for (int kcount = fromK; kcount <= toK; kcount++) {
				for (int j = 0; j < iteration; j++) {
					if (this.receivedKillCommand) {
						return;
					}
					// displayLog("Start Running Simulation "+simList[i]
					// +" ("+(j+1)+"/"+iteration+")\n");
					// set the result name;

					String rstName = simList[i] + "_run_" + count;
					while (!sim.setOutFile(rstName)) {
						count++;
						rstName = simList[i] + "_run_" + count;
					}
					count++;

					displayLog("Set K = " + kcount + "\n"
								+ "Set Output Name as: " + rstName + "\n");
					sim.setMAXPOPS(kcount);
					if (requireSeed) {
						sim.setRNDSEED(seed + counter);
						displayLog("Set Random Seed: " + (seed + counter) + "\n");
						counter++;
					}
					sim.writeParamFile();

					// figure out the plat form and coresponding structure
					// executable

					String execmd = "bin/structure -m ";

					String cmdStr = new String(execmd + "\""
							+ sim.getParamFile() + "\"" + " -e " + "\""
							+ sim.getExtraFile() + "\"");

					runningThread = new AppProc(cmdStr, mainFrame, projTree,
							sim, false);
					app.setRunningInstance(runningThread, sim);
					runningThread.start();

					while (runningThread.isAlive()) {
						if (this.receivedKillCommand) {
							return;
						}
						try {
							Thread.sleep(5000);
						} catch (Exception e) {
						}
					}

					displayLog("Simulation " + simList[i] + " finished\n\n");
				}
			}
		}

		displayLog("*******************************\n\nJob is Completed!\n");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "Job is Completed !", "Structure",
						JOptionPane.INFORMATION_MESSAGE);
				app.returnControl();
				mainFrame.updateUI(mainFrame.getCurrState());
			}
		});
	}

	public void killJob() {
		if (runningThread == null || !runningThread.isAlive()) {
			return;
		}
		runningThread.killProc();
		this.receivedKillCommand = true;
		log.append("\n\nJob is Killed by user\n");
		app.returnControl();
		mainFrame.updateUI(mainFrame.getCurrState());
		app.setRunningInstance(null, null);
		return;
	}

	public void showLog() {
		JobLogDlg jdlg = new JobLogDlg(log);
		jdlg.showDialog();
	}

}
