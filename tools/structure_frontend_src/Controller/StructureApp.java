package Controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import Dialog.ta_scroll;
import Gui.MainFrame;
import Gui.ProjTree;
import Gui.RemoveSimDlg;
import Gui.SchedulerDlg;
import Gui.SimListMenu;
import Gui.StartRunDlg;
import Gui.Param.ParamDlg;
import Gui.Wizard.WizardController;
import Loader.RstLoader;
import Obj.NProjObj;
import Obj.NSimObj;
import Obj.ObjIO;
import Obj.PlotData;
import Plot.AlphaPlotManager;
import Plot.FstPlotManager;
import Plot.LikePlotManager;
import Plot.LnpdPlotManager;
import Plot.RecmbPlotManager;
import Plot.RstPlotManager;
import Plot.SrPlotManager;
import Proc.AppProc;
import Tree.TreePlotManager;
import Triangle.TrianglePlotManager;
import Util.GeneralFileFilter;

public class StructureApp {

	// Controlled View Compentent
	private MainFrame mainFrame;
	private WizardController wizardController;
	private ParamDlg paramDlg;
	private ProjTree projTree;
	private SimListMenu simMenu;

	// Plot managers
	private FstPlotManager fstpm;
	private AlphaPlotManager alphapm;
	private LikePlotManager likepm;
	private LnpdPlotManager lnpdpm;
	private RstPlotManager rpm;
	private RecmbPlotManager recpm;
	private SrPlotManager srpm;
	private JobManager jobManager;

	private String plottingFilePath;
	private String plotString;
	private String plottingSimName;
	

	// The Data Model
	private NProjObj currProj;
	private NSimObj currSim;

	// the running instance of Simulation Object, in most time same as currSim,
	// when jobManager is in control
	// they are different object
	private NSimObj runningSim;
	private boolean outOfControl = false;

	private AppProc currThread;

	// handled by method setCurrentSelection
	private File currSimDir;
	private String currSimRst;

	// the history recorder
	private History history;

	// the filechooser's start directory
	private File fc_dir;
	

	/****************************************************/
	/******* Method to construct the Views *************/

	public void initStructure() {

		fstpm = new FstPlotManager();
		alphapm = new AlphaPlotManager();
		likepm = new LikePlotManager();
		lnpdpm = new LnpdPlotManager();
		recpm = new RecmbPlotManager();
		srpm = new SrPlotManager();
		history = new History(this);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		// Get size
		Dimension dimension = toolkit.getScreenSize();
		int screenWidth = dimension.width;
		int screenHeight = dimension.height;

		JFrame frame = new JFrame("Structure");

		// TODO: some cleanup code needed here
		// i.e. kill the running structure process
		// before quit

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit_structure();
			}
		});

		// init the projTree
		projTree = new ProjTree(this);
		projTree.initProjTree();

		// init the sim list menu
		simMenu = new SimListMenu(this);
		simMenu.initMenu();

		// init the mainFrame
		mainFrame = new MainFrame(this);
		frame.getContentPane().add("Center",
				mainFrame.createPane(screenWidth, screenHeight));
		frame.setJMenuBar(mainFrame.createMenuBar());
		frame.getContentPane().add("North", mainFrame.getToolBar());
		mainFrame.updateUI(0);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
				"images/icon.jpeg"));
		// show it!!
		frame.pack();
		frame.setSize(screenWidth, screenHeight);
		frame.setVisible(true);
	}

	/**********************************************/
	/************ Utility Methods ***************/

	// render the controlled Gui to potential containers
	public JTree getProjTree() {
		return projTree.getTree();
	}

	public JMenu getSimMenu() {
		return simMenu.getMenu();
	}

	public JMenu getHistoryMenu() {
		return history.getHistoryMenu();
	}

	/******************************************************/
	/******* Core Part: The Action Handlers ***************/

	// Actions are triggered by user command
	// all these functions are boolean functions
	// returning true by successfully execution

	public void exit_structure() {
		if (currThread != null && currThread.isAlive()) {
			currThread.killProc();
		}
		if (currProj != null) {
			File spjFile = new File(currProj.getWorkingPath(), currProj
					.getName());
			history.updateHistory(spjFile.getAbsolutePath(), false);
		}
		history.writeHistoryFile();
		System.exit(0);
	}

	public boolean newProject() {

		if (currProj == null) {

			wizardController = new WizardController(mainFrame);
			wizardController.initWizard();
			while (!wizardController.isDone()) {
				;
			}

			currProj = wizardController.getProjObj();

			// unsuccessful return
			if (currProj == null) {
				return false;
			}
			currProj.createProjectSpace();
			// loading data source

			String[][] data = wizardController.getData();
			if (data == null) {
				// loading error
				currProj = null;
				return false;
				// at this point wizardController is not null
			}

			mainFrame.showData(data, currProj.getDataTitle(), currProj
					.getGeneName(), currProj.getMapDistance(), currProj
					.getRecessiveAllele(), currProj.getPhaseInfo(), currProj
					.getOneRow(), currProj.getPloidy(), currProj.getNumloci());

			String[] simList = currProj.getSimList();
			// update the Guis
			projTree.loadProjTree(currProj.getName(), simList);
			simMenu.loadMenu(simList);

			wizardController = null;

			// garbage collect the possible huge memory
			data = null;
			mainFrame.updateUI(getCurrState());
			return true;
		}

		return false;
	}

	public boolean openProj() {

		GeneralFileFilter spjFilter = new GeneralFileFilter("spj",
				"Structure Project Files");
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(spjFilter);
		fc.setCurrentDirectory(fc_dir);
		int returnVal = fc.showOpenDialog(mainFrame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			fc_dir = file.getParentFile();
			NProjObj tempProj = ObjIO.loadProjObj(file);
			if (tempProj == null) {
				return false;
			}
			currProj = tempProj;
			String[][] data = currProj.loadData();
			if (data == null) {
				// loading error
				currProj = null;
				return false;

			}

			mainFrame.showData(data, currProj.getDataTitle(), currProj
					.getGeneName(), currProj.getRecessiveAllele(), currProj
					.getMapDistance(), currProj.getPhaseInfo(), currProj
					.getOneRow(), currProj.getPloidy(), currProj
					.getNumloci());

			// garbage collect the possible huge memory
			data = null;

			String[] list = currProj.getSimList();
			// update the Guis
			projTree.loadProjTree(currProj.getName(), list);
			simMenu.loadMenu(list);

			mainFrame.updateUI(getCurrState());
			setCurrentSelection(null, null);
			return true;

		}
		return false;
	}

	public boolean openProj(String spjPath) {

		if (currProj != null) {
			Object[] options = { "Yes", "No " };

			int n = JOptionPane.showOptionDialog(mainFrame,
					"Close current active project?", "Open project",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[1]);
			if (n == JOptionPane.NO_OPTION) {
				return false;
			}

			closeProj();

		}
		File file = new File(spjPath);
		fc_dir = file.getParentFile();
		NProjObj tempProj = ObjIO.loadProjObj(file);
		if (tempProj == null) {
			return false;
		}
		currProj = tempProj;
		String[][] data = currProj.loadData();
		if (data == null) {
			// loading error
			currProj = null;
			return false;

		}

		mainFrame.showData(data, currProj.getDataTitle(), currProj
				.getGeneName(), currProj.getMapDistance(), currProj
				.getRecessiveAllele(), currProj.getPhaseInfo(), currProj
				.getOneRow(), currProj.getPloidy(), currProj.getNumloci());

		// garbage collect the possible huge memory
		data = null;
		String[] list = currProj.getSimList();
		// update the Guis
		projTree.loadProjTree(currProj.getName(), list);
		simMenu.loadMenu(list);

		mainFrame.updateUI(getCurrState());
		setCurrentSelection(null, null);
		return true;
	}

	public boolean saveProj() {
		// save the current data
		currProj.writeProjectFile();
		return true;
	}

	public boolean closeProj() {

		File spjFile = new File(currProj.getWorkingPath(), currProj.getName());
		history.updateHistory(spjFile.getAbsolutePath(), true);
		currProj.writeProjectFile();
		currSim = null;
		currProj = null;

		// update the Guis
		projTree.emptyProjTree();
		simMenu.emptyMenu();

		mainFrame.updateUI(getCurrState());

		// call gc

		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();

		return true;
	}

	public boolean newSim() {

		NSimObj temp = new NSimObj(currProj);
		paramDlg = new ParamDlg(temp);
		paramDlg.pack();
		int n = paramDlg.showDialog();
		paramDlg = null;
		// get the feedback
		if (n == JOptionPane.OK_OPTION) {

			currProj.addSimObj(temp.getSimName());
			currProj.writeProjectFile();

			// update Guis

			boolean setActive = true;
			int state = getCurrState();
			if (state == 3 || state == 4) {
				setActive = false;
			}

			projTree.appendProjTree(temp.getSimName(), setActive);
			simMenu.appendMenuItem(temp.getSimName(), setActive);
			projTree.resetSimNodeFlag(currProj.getSimList());
			if (setActive) {
				currSim = temp;
				projTree.updateProjTree(currSim.getSimName());
				mainFrame.updateUI(getCurrState());
				mainFrame.loadDataFrame("sim", "Simulation Configuration - "
						+ currSim.getSimName(), currSim.printSimInfo());
			}

			return true;

		} else if (n == JOptionPane.CANCEL_OPTION) {
			return false;
		}

		return false;
	}

	public boolean loadSimObj(String simName) {

		NSimObj temp = ObjIO.loadSimObj(currProj, simName);

		if (temp == null) {
			return false;
		}

		currSim = temp;
		projTree.resetSimNodeFlag(currProj.getSimList());

		// update the Guis
		projTree.updateProjTree(simName);
		simMenu.updateMenu(simName);
		mainFrame.updateUI(getCurrState());
		mainFrame.loadDataFrame("sim", "Simulation Configuration - " + simName,
				currSim.printSimInfo());
		return true;
	}

	public boolean removeSimObj() {
		if (currProj == null) {
			return false;
		}

		String[] list = currProj.getSimList();
		if (list.length == 0) {
			JOptionPane.showMessageDialog(null,
					"No Simulations can to be Removed", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// else , let user pick up the list
		RemoveSimDlg rmdlg = new RemoveSimDlg(list);
		int n = rmdlg.showDialog();
		if (n == JOptionPane.CANCEL_OPTION) {
			rmdlg = null;
			return false;
		}

		// else must be true
		Object[] target = rmdlg.getSelectedList();
		rmdlg = null;
		for (int i = 0; i < target.length; i++) {
			if (currSim != null && currSim.getSimName().equals(target[i])) {

				if (getCurrState() == 3) {
					JOptionPane.showMessageDialog(null,
							"Can not Delete Current Running Simulation",
							"Error", JOptionPane.ERROR_MESSAGE);
					continue;
				}

				projTree.resetSimNodeFlag(currProj.getSimList());
				simMenu.setActiveFlag(currSim.getSimName(), false);
				currSim = null;

			}

			// Do the removal
			if (!currProj.removeSimObj((String) target[i])) {
				JOptionPane.showMessageDialog(null, "Delete Simulation "
						+ target[i] + " Failed", "Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				simMenu.removeMenuItem((String) target[i]);
				projTree.removeSimNode((String) target[i]);
			}

		}

		currProj.writeProjectFile();
		mainFrame.updateUI(getCurrState());
		return true;
	}

	public boolean simSetting() {

		paramDlg = new ParamDlg(currSim);

		int n = paramDlg.showDialog();
		// get the feedback
		if (n == JOptionPane.OK_OPTION) {
			currSim = paramDlg.getSimObj();
			mainFrame.loadDataFrame("sim", "Simulation Configuration - "
					+ currSim.getSimName(), currSim.printSimInfo());
			return true;
		} else if (n == JOptionPane.CANCEL_OPTION) {
			currSim = paramDlg.getSimObj();
			return false;
		} else if (n == JOptionPane.NO_OPTION) {
			NSimObj temp = paramDlg.getSimObj();
			currProj.addSimObj(temp.getSimName());
			currProj.writeProjectFile();
			boolean setActive = true;

			int state = getCurrState();
			if (state == 3 || state == 4) {
				setActive = false;
			}
			// update the Guis
			simMenu.appendMenuItem(currSim.getSimName(), setActive);
			projTree.appendProjTree(currSim.getSimName(), setActive);
			projTree.resetSimNodeFlag(currProj.getSimList());
			if (setActive) {
				currSim = temp;
				projTree.updateProjTree(currSim.getSimName());
				mainFrame.updateUI(getCurrState());
				mainFrame.loadDataFrame("sim", "Simulation Configuration - "
						+ currSim.getSimName(), currSim.printSimInfo());
			}

			return true;
		}
		return false;
	}

	public boolean runProc() {

		// ask for K
		int K = 0;

		StartRunDlg srd = new StartRunDlg();
		int feedback = srd.showDialog();
		/*
		 * while(true){ String sK =
		 * JOptionPane.showInputDialog("Set Number of Population Assumed (K)");
		 * if(sK == null) return false; try { K = Integer.parseInt(sK); }catch
		 * (NumberFormatException e){ JOptionPane.showMessageDialog(null,
		 * "K must be an Integer", "Error", JOptionPane.ERROR_MESSAGE);
		 * continue; }
		 * 
		 * if(K<=0){ JOptionPane.showMessageDialog(null,
		 * "K must be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
		 * continue; }else break; }
		 */

		if (feedback != 1) {
			return false;
		}

		K = srd.K;

		// generate an output file name
		int count = 1;
		String rstName = currSim.getSimName() + "_run_" + count;
		while (!currSim.setOutFile(rstName)) {
			count++;
			rstName = currSim.getSimName() + "_run_" + count;
		}

		// set MAXPOPS
		currSim.setMAXPOPS(K);

		// set RSeed
		if (srd.seed_flag == 1) {
			currSim.setRNDSEED(srd.seed);
		}

		// single point to write the param file
		currSim.writeParamFile();
		runningSim = currSim;

		String execmd = "bin/structure -m ";
		String cmdStr = new String(execmd + "\"" + currSim.getParamFile()
				+ "\"" + " -e " + "\"" + currSim.getExtraFile() + "\"");
		currThread = new AppProc(cmdStr, mainFrame, projTree, currSim, true);
		currThread.start();
		return true;
	}

	public boolean treePlot() {
		if (currProj == null) {
			return false;
		}
		TreePlotManager tpm = new TreePlotManager(this, currProj
				.getSimList(), plottingSimName, plottingFilePath,
				plotString);
		if (tpm.canWrite()) {
			Integer K = tpm.getK(plottingSimName, plottingFilePath);
	
			if (K.intValue() > 2) {
					boolean b = tpm.parseDistanceMatrix(plottingSimName, plottingFilePath);
					if (b) {
						b = tpm.runNeighborAlgorithm(plottingSimName, plottingFilePath);
					}
					if (b) {
						tpm.createTreePlot(plottingSimName, plottingFilePath);
					} else {
						JOptionPane.showMessageDialog(null, 
								"Could not create files neccessary for plotting, check write permissions\n" +
								"in the folder \"library/neighbor/plots\" in the structure directory."
								,
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				
			} else {
				JOptionPane.showMessageDialog(null,
						"Requires at least three populations.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, 
					"Cannot write to folder \"library/neighbor/plots\" in the structure directory, \n" +
					"please modify write permissions on the folder to use the Tree plot.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		
		}
		return true;
	}

	public boolean treePlotLicense() {
		// System.out.println("Tree Plot License");
		if (currProj == null) {
			return false;
		}
		ta_scroll gui = new ta_scroll();
		gui.launchFrame();
		return true;
	}

	public boolean triPlot(int option) {

		if (currProj == null) {
			return false;
		}
		if (option == 0) {
			TrianglePlotManager tpm = new TrianglePlotManager(this, currProj
					.getSimList());
			tpm.showFrame();
		} else if (option == 1) {
			TrianglePlotManager tpm = new TrianglePlotManager();
			tpm.showFrame(plottingSimName, plottingFilePath);
		}

		return true;
	}

	/*
	 * public boolean plot(String cat){
	 * 
	 * if(currSimDir !=null&&currSimRst!=null &&currProj != null&&currThread !=
	 * null && currThread.isAlive()){ // ask a question, what's the desired?
	 * Object[] options = {"View Run-Time Plots", "View Selected Plots",
	 * "       Cancel      "}; int n = JOptionPane.showOptionDialog(mainFrame,
	 * "Please select one from the following ", "Structure Plot",
	 * JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
	 * options, options[2]); if(n==JOptionPane.YES_OPTION) return
	 * plotDynamic(cat); if(n==JOptionPane.NO_OPTION) return plotStatic(cat);
	 * return false; }
	 * 
	 * 
	 * if(currSimDir !=null&&currSimRst!=null &&currProj != null) return
	 * plotStatic(cat);
	 * 
	 * if(currThread != null && currThread.isAlive()) return plotDynamic(cat);
	 * 
	 * else{ JOptionPane.showMessageDialog(null,
	 * "No data available for plotting \n"+ "Please select a result set first",
	 * "Structure Plotting", JOptionPane.ERROR_MESSAGE); return false; }
	 * 
	 * }
	 */

	public boolean plotStatic(String cat) {
		if (currSimDir == null) {
			JOptionPane.showMessageDialog(null, "Select a parameter set.", "No parameters selected", JOptionPane.OK_OPTION);
			return false;
		}
		if (currSimRst == null) {
			JOptionPane.showMessageDialog(null, "Run simulation first.", "No results found", JOptionPane.OK_OPTION);
			return false;
		
		}
		File plotDir = new File(currSimDir.getAbsolutePath(), "PlotData");
		String filename = new String();
		if (cat.equals("Fst")) {
			filename = currSimRst + "_fst";
		}
		if (cat.equals("Alpha")) {
			filename = currSimRst + "_alpha";
		}
		if (cat.equals("r")) {
			filename = currSimRst + "_sr";
		}
		if (cat.equals("Like")) {
			filename = currSimRst + "_lhd";
		}
		if (cat.equals("LnPD")) {
			filename = currSimRst + "_lnpd";
		}
		if (cat.equals("FstHist")) {
			filename = currSimRst + "_fsthist";
		}
		if (cat.equals("rHist")) {
			filename = currSimRst + "_srhist";
		}
		if (cat.equals("AlphaHist")) {
			filename = currSimRst + "_alphahist";
		}

		if (cat.equals("LnpdHist")) {
			filename = currSimRst + "_lnpdhist";
		}
		if (cat.equals("LikHist")) {
			filename = currSimRst + "_lhdhist";
		}
		if (cat.equals("RecHist")) {
			filename = currSimRst + "_recmbhist";
		}

		if (cat.equals("Rec")) {
			filename = currSimRst + "_recmb";
		}

		File dataFile = new File(plotDir.getAbsolutePath(), filename);
		PlotData pd = null;
		if (dataFile.exists()) {
			pd = PlotData.loadData(dataFile.getAbsolutePath());
		}

		if (cat.equals("Fst")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No fst data available, the simulation assumed\n"
								+ "allele frequencies independent",
						"Structure Plotting: Fst",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			fstpm.plot(pd.getDataArray(), currSimRst + " of " + currSimDir.getName());

			return true;
		}

		if (cat.equals("Alpha")) {
			if (pd == null) {
				JOptionPane
						.showMessageDialog(
								null,
								"No alpha data available. The simulation applied no admixture model",
								"Structure Plotting: Log Alpha",
								JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			
			alphapm.plot(pd.getDataArray(), currSimRst + " of "	+ currSimDir.getName());
			
			return true;
		}

		if (cat.equals("Like")) {
			if (pd != null) {
				likepm.plot(pd.getDataArray(), currSimRst + " of "
						+ currSimDir.getName());
			}
			return true;
		}

		if (cat.equals("r")) {
			if (pd != null) {
				srpm.plot(pd.getDataArray(), currSimRst + " of "
						+ currSimDir.getName());
			}
			return true;
		}

		if (cat.equals("LnPD")) {
			if (pd != null) {
				lnpdpm.plot(pd.getDataArray(), currSimRst + " of "
						+ currSimDir.getName());
			}
			return true;
		}

		if (cat.equals("Rec")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No recombination rate data  available. This simulation \n"
								+ "did not apply linkage model",
						"Structure Plotting: Recombination Rate",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			recpm.plot(pd.getDataArray(), currSimRst + " of "
					+ currSimDir.getName());
			return true;
		}

		if (cat.equals("FstHist")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No fst data available. The simulation assumed \n"
								+ "allele frequencies independent",
						"Structure Plotting: Fst",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			
			fstpm.plotHist(pd.getDataArray(), currSimRst + " of " + currSimDir.getName());

			return true;
		}

		if (cat.equals("AlphaHist")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No alpha data available. The simulation did not\n"
								+ "apply admixture model",
						"Structure Plotting: Log Alpha",
						JOptionPane.INFORMATION_MESSAGE);

				return false;
			}
			alphapm.plotHist(pd.getDataArray(), currSimRst + " of "	+ currSimDir.getName());
			return true;
		}

		if (cat.equals("RecHist")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No recombinant rate data available. The simulation did not\n"
								+ "apply linkage model", "Structure Plotting",
						JOptionPane.INFORMATION_MESSAGE);

				return false;
			}

			recpm.plotHist(pd.getDataArray(), currSimRst + " of "
					+ currSimDir.getName());
			return true;
		}
		if (cat.equals("rHist")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null, "No r data available",
						"Structure Plotting", JOptionPane.INFORMATION_MESSAGE);

				return false;
			}

			srpm.plotHist(pd.getDataArray(), currSimRst + " of "
					+ currSimDir.getName());
			return true;
		}

		if (cat.equals("LikHist")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No likihood data available", "Structure Plotting",
						JOptionPane.INFORMATION_MESSAGE);

				return false;
			}

			likepm.plotHist(pd.getDataArray(), currSimRst + " of "
					+ currSimDir.getName());
			return true;
		}

		if (cat.equals("LnpdHist")) {
			if (pd == null) {
				JOptionPane.showMessageDialog(null,
						"No ln P(D) data available", "Structure Plotting",
						JOptionPane.INFORMATION_MESSAGE);

				return false;
			}

			lnpdpm.plotHist(pd.getDataArray(), currSimRst + " of "
					+ currSimDir.getName());
			return true;
		}

		return false;
	}

	public boolean plotDynamic(String cat) {

		if (cat.equals("Fst")) {
			if (!runningSim.FREQSCORR) {
				JOptionPane
						.showMessageDialog(
								null,
								"The simulation assumes allele frequencies independent",
								"Structure Plotting: Fst",
								JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			fstpm.plot(currThread, runningSim.ONEFST, runningSim.getMAXPOPS());

			return true;
		}

		if (cat.equals("Alpha")) {
			if (runningSim.NOADMIX && !runningSim.RECOMBINE
					&& runningSim.POPNOADMIX && runningSim.POPRECOMBINE) {
				JOptionPane.showMessageDialog(null,
						"The simulation is using no admixture model",
						"Structure Plotting: Log Alpha",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			alphapm.plot(currThread, runningSim.POPALPHAS, runningSim
					.getMAXPOPS());
			return true;
		}

		if (cat.equals("r")) {
			if (!runningSim.LOCPRIOR) {
				JOptionPane.showMessageDialog(null,
						"The simulation is not using LOCPRIOR model",
						"Structure Plotting: r",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			srpm.plot(currThread);
			return true;
		}

		if (cat.equals("Like")) {
			if (!runningSim.COMPUTEPROB) {
				JOptionPane.showMessageDialog(null,
						"No likelihood data available",
						"Structure Plotting: Log Alpha",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			likepm.plot(currThread);
			return true;
		}

		if (cat.equals("LnPD")) {
			if (!runningSim.COMPUTEPROB) {
				JOptionPane.showMessageDialog(null,
						"No Ln P(D) data available",
						"Structure Plotting: Log Alpha",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			if (!currThread.isBurinFinished()) {
				JOptionPane
						.showMessageDialog(
								null,
								"Burnin is not finished, no Ln P(D) data available at this moment",
								"Structure Plotting: Ln P(D)",
								JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			lnpdpm.plot(currThread);
			return true;
		}

		if (cat.equals("Rec")) {
			if (!runningSim.RECOMBINE && !runningSim.POPRECOMBINE) {
				JOptionPane.showMessageDialog(null,
						"The simulation is not using linkage model",
						"Structure Plotting: Recombination Rate",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			if (!currThread.isRecStarted()) {
				JOptionPane.showMessageDialog(null,
						"\"NO Recombination Burnin\" is not finished,\n"
								+ "no data available at this moment",
						"Structure Plotting: Recombination Rate",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}

			recpm.plot(currThread);
			return true;
		}

		if (cat.equals("AlphaHist") || cat.equals("FstHist")
				|| cat.equals("LikHist") || cat.equals("LnpdHist")
				|| cat.equals("RecHist")) {
			JOptionPane.showMessageDialog(null,
					"Simulation is not finished, can not plot histogram",
					"Structure Plotting", JOptionPane.INFORMATION_MESSAGE);
		}
		return false;

	}

	public boolean stopProc() {
		if (currThread != null) {
			currThread.killProc();
			currThread = null;
		}
		mainFrame.getConsole().append("\n\nSimulation Interrupted!");
		return true;
	}

	public void startJob() {
		if (currProj == null) {
			return;
		}
		String[] list;
		int iteration = 0;
		int fromK = 0;
		int toK = 0;
		boolean requireSeed;
		int seed = 0;
		String[] simList = currProj.getSimList();
		if (simList == null || simList.length == 0) {
			JOptionPane.showMessageDialog(null, "create new paramer settings before submitting a job","No parameter sets defined", JOptionPane.WARNING_MESSAGE);
			return;
		}

		SchedulerDlg sdlg = new SchedulerDlg(simList);

		int n = sdlg.showDialog();

		if (n == JOptionPane.OK_OPTION) {
			list = sdlg.getSelectedList();
			iteration = sdlg.getIterationNum();
			fromK = sdlg.getFromK();
			toK = sdlg.getToK();
			requireSeed = sdlg.isSeedRequired();
			seed = sdlg.getSeed();
			if (list == null) {
				return;
			}
		} else {
			return;
		}
		sdlg = null;

		jobManager = new JobManager(this, currProj, list, iteration, fromK,
				toK, mainFrame, projTree, requireSeed, seed);
		jobManager.start();
		jobManager.showLog();
		outOfControl = true;
	}

	void returnControl() {
		outOfControl = false;
	}

	void setRunningInstance(AppProc runningThread, NSimObj sim) {
		currThread = runningThread;
		runningSim = sim;
	}

	public void showJobLog() {
		if (jobManager == null) {
			return;
		}
		jobManager.showLog();
	}

	public void killJob() {
		if (jobManager == null || !jobManager.isAlive()) {
			return;
		}
		jobManager.killJob();
	}

	public void genParamFiles() {
		if (currProj == null) {
			return;
		}
		String[] simList = currProj.getSimList();
		if (simList == null) {
			return;
		}
		@SuppressWarnings("unused")
		PFGenManager pfg = new PFGenManager(this, currProj, simList);
	}

	public void loadOutsideRst() {
		@SuppressWarnings("unused")
		RstLoader loader = new RstLoader();
	}

	//
	// Other functional utilities connecting data model and view
	//  

	public File[] getRunFiles(String simName) {

		if (currProj == null) {
			return null;
		}

		File simDir = new File(currProj.getWorkingPath(), simName);

		if (!simDir.exists()) {
			return null;
		}
		File rstDir = new File(simDir.getAbsolutePath(), "Results");

		if (!rstDir.exists()) {
			return null;
		}

		File[] allFiles = rstDir.listFiles();
		if (allFiles == null) {
			return null;
		}

		Vector<File> filevec = new Vector<File>();
		for (int i = 0; i < allFiles.length; i++) {
			String name = allFiles[i].getName();

			if (name.endsWith("_f")) {
				filevec.add(allFiles[i]);
			}

		}
		if (filevec.size() == 0) {
			return null;
		}

		File[] runlist = new File[filevec.size()];

		for (int i = 0; i < filevec.size(); i++) {
			runlist[i] = filevec.elementAt(i);
		}

		return runlist;

	}

	public String[] getRstFiles(String simName) {
		if (currProj == null) {
			return null;
		}
		File simDir = new File(currProj.getWorkingPath(), simName);

		if (!simDir.exists()) {
			return null;
		}
		File rstDir = new File(simDir.getAbsolutePath(), "Results");

		if (!rstDir.exists()) {
			return null;
		}

		File[] allFiles = rstDir.listFiles();
		if (allFiles == null) {
			return null;
		}

		//TODO: really should be a stringbuffer
		Vector<String> filename = new Vector<String>();
		for (int i = 0; i < allFiles.length; i++) {
			String name = allFiles[i].getName();

			// find K
			String sk = "";
			if (name.endsWith("_f")) {
				BufferedReader infile = null;
				try {
					infile = new BufferedReader(new InputStreamReader(new FileInputStream(allFiles[i].getAbsolutePath())));
				} catch (FileNotFoundException e) {
					return null;
				}

				while (true) {
					String str = null;
					try {
						str = infile.readLine();
					} catch (Exception re) {
					}
					if (str == null) {
						break;
					}
					if (str.endsWith("populations assumed")) {
						StringTokenizer st = new StringTokenizer(str);
						sk = st.nextToken();
						break;
					}
				}
				filename.addElement(name.substring(0, name.length() - 2)
						+ " ( K=" + sk + " )");
			}
		}

		if (filename.size() == 0) {
			return null;
		}

		String[] names = new String[filename.size()];
		for (int i = 0; i < filename.size(); i++) {
			names[i] = filename.elementAt(i);
		}

		return names;
	}

	public void appendRstToTree(String simName, String rstname, int K) {

		projTree.appendRstNode(simName, rstname, K);
	}

	public void setCurrentSelection(File simDir, String rstSet) {
		currSimDir = simDir;
		currSimRst = rstSet;
	}

	public int getCurrState() {

		if (outOfControl) {
			return 4;
		}

		if (currProj == null) {
			return 0;
		}
		if (currSim == null) {
			return 1;
		}

		if (currThread == null) {
			return 2;
		}

		if (currThread.isAlive()) {
			return 3;
		}

		return 2;

	}

	public void showResults(String simName, String filename) {

		if (currProj == null) {
			return;
		}
		File simDir = new File(currProj.getWorkingPath(), simName);

		if (!simDir.exists()) {
			return;
		}
		File rstDir = new File(simDir.getAbsolutePath(), "Results");

		if (!rstDir.exists()) {
			return;
		}
		File targetFile = new File(rstDir.getAbsolutePath(), filename + "_f");

		if (!targetFile.exists()) {
			return;
		}

		File plotDir = new File(simDir.getAbsolutePath(), "PlotData");

		setCurrentSelection(simDir, filename);

		StringBuffer buffer = new StringBuffer();
		String content = "";

		try {
			FileInputStream fis = new FileInputStream(targetFile
					.getAbsolutePath());
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

		// reformat the content (too slow)
		/*
		 * StringTokenizer file_st = new StringTokenizer(content,"\n"); String
		 * temps = ""; while(file_st.hasMoreTokens()){
		 * 
		 * String str = file_st.nextToken(); temps += "             "+str+"\n";
		 * }
		 * 
		 * content = temps;
		 */

		// set resultframe menubar
		JMenuBar mbar = new JMenuBar();
		JMenu bmenu = new JMenu("Bar plot");
		JMenuItem mi = new JMenuItem("Show");
		mi.setActionCommand("barplot");
		mi.addActionListener(mainFrame);
		bmenu.add(mi);

		JMenu dmenu = new JMenu("Data plot");
		JMenuItem mfst = new JMenuItem("Fst");
		if ((new File(plotDir, filename + "_fst")).exists()) {
			mfst.setActionCommand("sviewfst");
			mfst.addActionListener(mainFrame);
		} else {
			mfst.setEnabled(false);
		}
		JMenuItem malpha = new JMenuItem("Alpha");
		if ((new File(plotDir, filename + "_alpha")).exists()) {
			malpha.setActionCommand("sviewalpha");
			malpha.addActionListener(mainFrame);
		} else {
			malpha.setEnabled(false);
		}
		JMenuItem mrecomb = new JMenuItem("Recombination rate");
		if ((new File(plotDir, filename + "_recmb")).exists()) {
			mrecomb.setActionCommand("sviewrec");
			mrecomb.addActionListener(mainFrame);
		} else {
			mrecomb.setEnabled(false);
		}

		JMenuItem mr = new JMenuItem("r (LOCPRIOR model)");
		if ((new File(plotDir, filename + "_sr")).exists()) {
			mr.setActionCommand("sviewr");
			mr.addActionListener(mainFrame);
		} else {
			mr.setEnabled(false);
		}

		JMenuItem mlike = new JMenuItem("Likelihood");
		if ((new File(plotDir, filename + "_lhd")).exists()) {
			mlike.setActionCommand("sviewlike");
			mlike.addActionListener(mainFrame);
		} else {
			mlike.setEnabled(false);
		}
		JMenuItem mlnpd = new JMenuItem("Ln P(D) ");
		if ((new File(plotDir, filename + "_lnpd")).exists()) {
			mlnpd.setActionCommand("sviewlnpd");
			mlnpd.addActionListener(mainFrame);
		} else {
			mlnpd.setEnabled(false);
		}
		dmenu.add(mfst);

		dmenu.add(malpha);
		dmenu.add(mrecomb);
		dmenu.add(mr);
		dmenu.add(mlike);
		dmenu.add(mlnpd);

		JMenu hmenu = new JMenu("Histogram");
		JMenuItem mhfst = new JMenuItem("Fst");
		if ((new File(plotDir, filename + "_fsthist")).exists()) {
			mhfst.setActionCommand("histfst");
			mhfst.addActionListener(mainFrame);
		} else {
			mhfst.setEnabled(false);
		}
		JMenuItem mhalpha = new JMenuItem("Alpha");
		if ((new File(plotDir, filename + "_alphahist")).exists()) {
			mhalpha.setActionCommand("histalpha");
			mhalpha.addActionListener(mainFrame);
		} else {
			mhalpha.setEnabled(false);
		}

		JMenuItem mhrecomb = new JMenuItem("Recombination rate");
		if ((new File(plotDir, filename + "_recmbhist")).exists()) {
			mhrecomb.setActionCommand("histrec");
			mhrecomb.addActionListener(mainFrame);
		} else {
			mhrecomb.setEnabled(false);
		}

		JMenuItem mhr = new JMenuItem("r (LOCPRIOR model)");
		if ((new File(plotDir, filename + "_srhist")).exists()) {
			mhr.setActionCommand("histr");
			mhr.addActionListener(mainFrame);
		} else {
			mhr.setEnabled(false);
		}

		JMenuItem mhlike = new JMenuItem("Likelihood");
		if ((new File(plotDir, filename + "_lhdhist")).exists()) {
			mhlike.setActionCommand("histlike");
			mhlike.addActionListener(mainFrame);
		} else {
			mhlike.setEnabled(false);
		}
		JMenuItem mhlnpd = new JMenuItem("Ln P(D) ");
		if ((new File(plotDir, filename + "_lnpdhist")).exists()) {
			mhlnpd.setActionCommand("histlnpd");
			mhlnpd.addActionListener(mainFrame);
		} else {
			mhlnpd.setEnabled(false);
		}

		hmenu.add(mhfst);

		hmenu.add(mhalpha);
		hmenu.add(mhrecomb);
		hmenu.add(mhr);
		hmenu.add(mhlike);
		hmenu.add(mhlnpd);

		JMenu trmenu = new JMenu("Tree plot");
		JMenuItem treeMenuItem1 = new JMenuItem("Show");
		JMenuItem treeMenuItem2 = new JMenuItem("Information and credit");

		treeMenuItem1.setActionCommand("treeplot");
		treeMenuItem2.setActionCommand("treePlotLicense");

		treeMenuItem1.addActionListener(mainFrame);
		treeMenuItem2.addActionListener(mainFrame);

		trmenu.add(treeMenuItem1);
		trmenu.add(treeMenuItem2);

		// trmenu.add(treeMenuItem1);
		// trmenu.add(treeMenuItem2);

		JMenu tmenu = new JMenu("Triangle plot");
		JMenuItem trimenu = new JMenuItem("Show");
		trimenu.setActionCommand("triplot");
		trimenu.addActionListener(mainFrame);
		tmenu.add(trimenu);

		mbar.add(bmenu);
		mbar.add(dmenu);
		mbar.add(hmenu);
		mbar.add(tmenu);

		mbar.add(trmenu);

		mainFrame.setRstMenu(mbar);
		mainFrame.loadDataFrame("result", "Simulation Result: " + simName + "("
				+ filename + ")", content);
		plottingFilePath = targetFile.getAbsolutePath();
		plottingSimName = simName;
		plotString = simName + "(" + filename + ")";

		return;

	}

	public void loadBarPlot() {
		// load the graph

		if (rpm != null) {
			rpm.dispose();
			rpm = null;
		}
		rpm = new RstPlotManager(mainFrame, plottingFilePath, plotString);
	}

	public void loadSummary() {
		if (currProj == null) {
			return;
		}
		SummaryGenerator sg = new SummaryGenerator(currProj);
		JInternalFrame sp = sg.getSummary();
		if (sp == null) {
			JOptionPane.showMessageDialog(null,
					"No Summary Information Available", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		mainFrame.loadSummaryFrame(sp, "Summary of Project "
				+ currProj.getName());
		return;
	}

	public void loadDataFrame(String type, String simName) {
		if (type.equals("proj")) {
			mainFrame.loadDataFrame("proj", "Project Information - "
					+ currProj.getName(), currProj.printProjInfo());
			return;
		}
		if (type.equals("data")) {
			mainFrame.loadDataFrame("data", "", "");
			return;
		}

		if (type.equals("sim")) {
			if (simName != null) {

				NSimObj sim = ObjIO.loadSimObj(currProj, simName);

				if (sim == null) {
					return;
				}
				mainFrame.loadDataFrame("sim",
						"Simulation Configuration - " + simName, sim.printSimInfo());
				
				sim = null;

			} else if (currSim != null) {
				mainFrame.loadDataFrame("sim", "Simulation Configuration - "
						+ currSim.getSimName(), currSim.printSimInfo());
			}

			return;

		}
	}

}
