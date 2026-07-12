package Gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import Controller.StructureApp;
import Util.Lexer;

public class MainFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// the controller
	private StructureApp app;

	// Gui Components

	// the overall pane
	private JSplitPane splitPane;

	// Three sub panels
	private JScrollPane screenPane; // Output Area
	private JScrollPane listScrollPane; // Tree Panel
	private JDesktopPane rightPane; // Right Doc Panel

	// the output are contained by screenPane
	private TextArea console;

	// the internal frame set contained by rightPane
	private JInternalFrame dataframe;
	private JInternalFrame projframe;
	private JInternalFrame simframe;
	private JInternalFrame resultframe;
	private JInternalFrame rstplotframe;
	private JInternalFrame summaryframe;

	private JMenuBar rst_mbar; // menu bar for result frame updated
								// automatically by StructureApp

	// The listener to the internal panel windows event
	private FrameSetListener framelistener = new FrameSetListener();
	

	// Menus

	// top-level menu bar
	private JMenuBar menuBar;

	// Tool Bar
	private JToolBar toolBar;

	// File Menu
	private JMenuItem load_rst_menu;
	private JMenuItem data_open_menu;
	private JMenuItem data_close_menu;
	private JMenuItem new_proj_menu;
	private JMenuItem open_proj_menu;
	private JMenuItem close_proj_menu;
	private JMenuItem save_proj_menu;
	private JMenuItem exit_menu;
	private JMenuItem print_menu;

	// Edit Menu
	private JMenuItem copy_menu;
	private JMenuItem cut_menu;
	private JMenuItem paste_menu;

	// Project Menu
	private JMenuItem proj_job_menu;
	private JMenuItem proj_joblog_menu;
	private JMenuItem proj_killjob_menu;
	private JMenuItem proj_pgen_menu;

	// Simulation Menu
	private JMenuItem sim_param_menu;
	private JMenuItem sim_run_menu;
	private JMenuItem sim_stop_menu;
	private JMenuItem sim_new_menu;
	private JMenuItem sim_del_menu;

	// Plotting Menu
	private JMenu plot_data_menu;
	private JMenuItem data_fst_menu;
	private JMenuItem data_alpha_menu;
	private JMenuItem data_like_menu;
	private JMenuItem data_r_menu;
	private JMenuItem data_lnpd_menu;
	private JMenuItem data_rec_menu;

	private JMenuItem triangle_menu;

	// View Menu
	private JMenuItem view_proj_menu;
	private JMenuItem view_sim_menu;
	private JMenuItem view_result_menu;
	private JMenuItem view_clear_menu;

	// Help Menu
	private JMenuItem help_topic_menu;
	private JMenuItem help_about_menu;

	// File Button
	private JButton open_proj_button;
	private JButton save_proj_button;
	private JButton new_proj_button;

	// Sim Button
	private JButton sim_run_button;
	private JButton sim_stop_button;
	private JButton sim_new_button;
	private JButton sim_del_button;

	// Help Button
	private JButton help_topic_button;

	// Start point of File Chooser
	private File fc_dir;

	/**********************************************************/
	/************** MainFrame Class Methods *******************/

	// Constructor
	public MainFrame(StructureApp app) {
		this.app = app;
		createMenuItem();

	}

	private void createMenuItem() {

		Dimension d = new Dimension(23, 23);
		data_open_menu = new JMenuItem("Open Data File ...");
		load_rst_menu = new JMenuItem("Load structure results ...");
		data_close_menu = new JMenuItem("Close File");
		new_proj_menu = new JMenuItem("New Project ...");
		open_proj_menu = new JMenuItem("Open Project ...");
		close_proj_menu = new JMenuItem("Close Project");
		save_proj_menu = new JMenuItem("Save Project");
		exit_menu = new JMenuItem("Exit");
		print_menu = new JMenuItem("Print ...");

		open_proj_button = new JButton(new ImageIcon("images/New_Job.gif"));
		open_proj_button.setPreferredSize(d);
		open_proj_button.setToolTipText("Open Project");
		save_proj_button = new JButton(new ImageIcon("images/Save_All.gif"));
		save_proj_button.setPreferredSize(d);
		save_proj_button.setToolTipText("Save Project");
		new_proj_button = new JButton(new ImageIcon(
				"images/New_Program_Wizard.gif"));
		new_proj_button.setToolTipText("New Project");
		new_proj_button.setPreferredSize(d);

		copy_menu = new JMenuItem("Copy");
		cut_menu = new JMenuItem("Cut");
		paste_menu = new JMenuItem("Paste");

		proj_job_menu = new JMenuItem("Start a Job");
		proj_joblog_menu = new JMenuItem("View Job Log");
		proj_killjob_menu = new JMenuItem("Kill Running Job");
		proj_pgen_menu = new JMenuItem("Generate parameter files...");
		sim_param_menu = new JMenuItem("Modify current set ...");
		sim_run_menu = new JMenuItem("Run");
		sim_stop_menu = new JMenuItem("Stop");
		sim_new_menu = new JMenuItem("New ... ");
		sim_del_menu = new JMenuItem("Remove Parameter Set ... ");

		sim_new_button = new JButton(new ImageIcon("images/new2.gif"));
		sim_new_button.setPreferredSize(d);
		sim_new_button.setToolTipText("New Parameter Set");
		sim_del_button = new JButton(new ImageIcon("images/delete.gif"));
		sim_del_button.setPreferredSize(d);
		sim_del_button.setToolTipText("Remove Parameter Set");
		sim_run_button = new JButton(new ImageIcon("images/Run.gif"));
		sim_run_button.setPreferredSize(d);
		sim_run_button.setToolTipText("Run");
		sim_stop_button = new JButton(new ImageIcon("images/Stop.gif"));
		sim_stop_button.setPreferredSize(d);
		sim_stop_button.setToolTipText("Stop");

		plot_data_menu = new JMenu("Run-time");
		data_fst_menu = new JMenuItem("Fst");
		data_alpha_menu = new JMenuItem("Alpha");
		data_r_menu = new JMenuItem("r (LOCPRIOR model)");
		data_like_menu = new JMenuItem("Likelihood");
		data_lnpd_menu = new JMenuItem("Ln P(D) ");
		data_rec_menu = new JMenuItem("Recombination Rate");

		triangle_menu = new JMenuItem("Triangle Plot");

		view_proj_menu = new JMenuItem("Project Info");
		view_sim_menu = new JMenuItem("Active Parameter Set Info");
		view_result_menu = new JMenuItem("Simulation Summary");
		view_clear_menu = new JMenuItem("Clear Output Console");

		help_topic_menu = new JMenuItem("Help Document");
		help_about_menu = new JMenuItem("About Structure");

		help_topic_button = new JButton(new ImageIcon("images/Home.gif"));
		help_topic_button.setPreferredSize(d);
		help_topic_button.setToolTipText("About Structure");
	}

	public JSplitPane createPane(int screenWidth, int screenHeight) {

		// prepare the component panes

		listScrollPane = new JScrollPane();
		listScrollPane.getViewport().add(app.getProjTree());

		rightPane = new JDesktopPane();

		// create console area
		console = new TextArea(5, 5);
		console.setBackground(Color.black);
		console.setForeground(Color.white);
		console.setEditable(false);

		screenPane = new JScrollPane(console);

		// Create a split pane with the two scroll panes in it
		JSplitPane upPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				listScrollPane, rightPane);
		upPane.setOneTouchExpandable(true);
		upPane.setDividerLocation(screenWidth / 4);

		// Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(screenWidth / 8, screenHeight / 6);
		listScrollPane.setMinimumSize(minimumSize);
		rightPane.setMinimumSize(minimumSize);
		screenPane.setMinimumSize(minimumSize);
		// Provide a preferred size for the split pane
		upPane.setPreferredSize(new Dimension(6 * screenWidth / 7,
				5 * screenHeight / 7));
		screenPane.setMaximumSize(minimumSize);
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upPane,
				screenPane);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(4 * screenHeight / 7);

		return splitPane;
	}

	// public function that creates the menu bar
	public JMenuBar createMenuBar() {
		menuBar = new JMenuBar();
		toolBar = new JToolBar();

		// Build the first menu.
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu projMenu = new JMenu("Project");
		JMenu simMenu = new JMenu("Parameter Set");
		JMenu viewMenu = new JMenu("View");
		JMenu helpMenu = new JMenu("Help");
		JMenu plotMenu = new JMenu("Plotting");

		load_rst_menu.setActionCommand("loadrst");
		load_rst_menu.addActionListener(this);

		data_open_menu.setActionCommand("datafile");
		data_open_menu.addActionListener(this);

		data_close_menu.setActionCommand("closefile");
		data_close_menu.addActionListener(this);

		new_proj_menu.setActionCommand("newproject");
		new_proj_menu.addActionListener(this);
		new_proj_button.setActionCommand("newproject");
		new_proj_button.addActionListener(this);

		open_proj_menu.setActionCommand("openproj");
		open_proj_menu.addActionListener(this);
		open_proj_button.setActionCommand("openproj");
		open_proj_button.addActionListener(this);

		close_proj_menu.setActionCommand("closeproj");
		close_proj_menu.addActionListener(this);

		save_proj_menu.setActionCommand("saveproj");
		save_proj_menu.addActionListener(this);
		save_proj_button.setActionCommand("saveproj");
		save_proj_button.addActionListener(this);

		print_menu.setActionCommand("print");
		print_menu.addActionListener(this);

		exit_menu.setActionCommand("exit");
		exit_menu.addActionListener(this);

		proj_job_menu.setActionCommand("startjob");
		proj_job_menu.addActionListener(this);

		proj_joblog_menu.setActionCommand("joblog");
		proj_joblog_menu.addActionListener(this);

		proj_killjob_menu.setActionCommand("killjob");
		proj_killjob_menu.addActionListener(this);

		proj_pgen_menu.setActionCommand("genparam");
		proj_pgen_menu.addActionListener(this);

		sim_new_menu.setActionCommand("simnew");
		sim_new_menu.addActionListener(this);
		sim_new_button.setActionCommand("simnew");
		sim_new_button.addActionListener(this);

		sim_del_menu.setActionCommand("simdel");
		sim_del_menu.addActionListener(this);
		sim_del_button.setActionCommand("simdel");
		sim_del_button.addActionListener(this);

		sim_param_menu.setActionCommand("simsetting");
		sim_param_menu.addActionListener(this);

		sim_run_menu.setActionCommand("simrun");
		sim_run_menu.addActionListener(this);
		sim_run_button.setActionCommand("simrun");
		sim_run_button.addActionListener(this);

		sim_stop_menu.setActionCommand("simstop");
		sim_stop_menu.addActionListener(this);
		sim_stop_button.setActionCommand("simstop");
		sim_stop_button.addActionListener(this);

		view_proj_menu.setActionCommand("viewproj");
		view_proj_menu.addActionListener(this);

		view_sim_menu.setActionCommand("viewsim");
		view_sim_menu.addActionListener(this);

		view_result_menu.setActionCommand("viewsummary");
		view_result_menu.addActionListener(this);

		data_fst_menu.setActionCommand("viewfst");
		data_fst_menu.addActionListener(this);

		data_alpha_menu.setActionCommand("viewalpha");
		data_alpha_menu.addActionListener(this);

		data_r_menu.setActionCommand("viewr");
		data_r_menu.addActionListener(this);

		data_like_menu.setActionCommand("viewlike");
		data_like_menu.addActionListener(this);

		data_lnpd_menu.setActionCommand("viewlnpd");
		data_lnpd_menu.addActionListener(this);

		data_rec_menu.setActionCommand("viewrec");
		data_rec_menu.addActionListener(this);

		triangle_menu.setActionCommand("triangle");
		triangle_menu.addActionListener(this);

		view_clear_menu.setActionCommand("clearconsole");
		view_clear_menu.addActionListener(this);

		help_topic_menu.setActionCommand("helpdoc");
		help_topic_menu.addActionListener(this);

		help_about_menu.setActionCommand("about");
		help_about_menu.addActionListener(this);

		help_topic_button.setActionCommand("about");
		help_topic_button.addActionListener(this);

		// build file menu
		fileMenu.add(data_open_menu);
		fileMenu.add(data_close_menu);
		fileMenu.addSeparator();
		fileMenu.add(new_proj_menu);
		fileMenu.add(open_proj_menu);
		fileMenu.add(close_proj_menu);
		fileMenu.add(save_proj_menu);
		// fileMenu.addSeparator();
		// fileMenu.add(print_menu);
		fileMenu.addSeparator();
		fileMenu.add(load_rst_menu);
		fileMenu.addSeparator();
		fileMenu.add(app.getHistoryMenu());
		fileMenu.addSeparator();

		fileMenu.add(exit_menu);

		// build edit menu
		editMenu.add(copy_menu);
		editMenu.add(cut_menu);
		editMenu.add(paste_menu);

		// buld sim menu
		simMenu.add(app.getSimMenu());
		simMenu.addSeparator();
		simMenu.add(sim_param_menu);
		simMenu.add(sim_new_menu);
		simMenu.add(sim_del_menu);
		simMenu.addSeparator();
		simMenu.add(sim_run_menu);
		simMenu.add(sim_stop_menu);

		// build proj menu
		projMenu.add(proj_job_menu);
		projMenu.add(proj_joblog_menu);
		projMenu.add(proj_killjob_menu);
		projMenu.addSeparator();
		projMenu.add(proj_pgen_menu);

		// build view menu
		viewMenu.add(view_proj_menu);
		viewMenu.add(view_sim_menu);
		viewMenu.add(view_result_menu);
		plot_data_menu.add(data_fst_menu);
		plot_data_menu.add(data_alpha_menu);
		plot_data_menu.add(data_rec_menu);
		plot_data_menu.add(data_r_menu);
		plot_data_menu.add(data_like_menu);
		plot_data_menu.add(data_lnpd_menu);

		plotMenu.add(plot_data_menu);
		plotMenu.addSeparator();
		plotMenu.add(triangle_menu);

		viewMenu.add(view_clear_menu);
		// build help menu
		helpMenu.add(help_topic_menu);
		helpMenu.add(help_about_menu);

		// Finally compose the menuBar
		menuBar.add(fileMenu);
		// menuBar.add(editMenu);
		menuBar.add(projMenu);
		menuBar.add(simMenu);
		menuBar.add(plotMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);

		toolBar.addSeparator();
		toolBar.add(open_proj_button);
		toolBar.add(save_proj_button);
		toolBar.add(new_proj_button);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(sim_new_button);
		toolBar.add(sim_del_button);
		toolBar.addSeparator();
		toolBar.add(sim_run_button);
		toolBar.add(sim_stop_button);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(help_topic_button);
		return menuBar;
	}

	/***********************************************************/
	/*********** UI (mostly menus) Update Mehtods **************/

	// this method update UIs according to the program state
	// state 0 : program started, no project loaded
	// state 1 : project loaded , no active sim selected
	// state 2 : Active simulation selected but not running
	// state 3 : Active simulation is running
	// state 4 : Active job is running

	public void updateUI(int state) {
		if (state == 0) {
			new_proj_menu.setEnabled(true);
			new_proj_button.setEnabled(true);
			open_proj_menu.setEnabled(true);
			open_proj_button.setEnabled(true);
			close_proj_menu.setEnabled(false);
			save_proj_menu.setEnabled(false);
			save_proj_button.setEnabled(false);

			proj_job_menu.setEnabled(false);
			proj_joblog_menu.setEnabled(false);
			proj_killjob_menu.setEnabled(false);
			proj_pgen_menu.setEnabled(false);

			app.getSimMenu().setEnabled(false);
			sim_param_menu.setEnabled(false);
			sim_run_menu.setEnabled(false);
			sim_run_button.setEnabled(false);
			sim_stop_menu.setEnabled(false);
			sim_stop_button.setEnabled(false);
			sim_new_menu.setEnabled(false);
			sim_new_button.setEnabled(false);
			sim_del_menu.setEnabled(false);
			sim_del_button.setEnabled(false);

			view_proj_menu.setEnabled(false);
			view_sim_menu.setEnabled(false);
			view_result_menu.setEnabled(false);
			view_clear_menu.setEnabled(false);
			plot_data_menu.setEnabled(false);
			triangle_menu.setEnabled(false);
			console.setText("");
			if (dataframe != null) {
				dataframe.dispose();
			}
			if (projframe != null) {
				projframe.dispose();
				projframe = null;
			}
			if (simframe != null) {
				simframe.dispose();
			}
			if (resultframe != null) {
				resultframe.dispose();
				rst_mbar = null;
			}
			if (rstplotframe != null) {
				rstplotframe.dispose();
			}
			if (summaryframe != null) {
				summaryframe.dispose();
			}
			return;
		}

		if (state == 1) {
			new_proj_menu.setEnabled(false);
			open_proj_menu.setEnabled(false);
			close_proj_menu.setEnabled(true);
			save_proj_menu.setEnabled(true);
			new_proj_button.setEnabled(false);
			open_proj_button.setEnabled(false);
			save_proj_button.setEnabled(true);

			proj_job_menu.setEnabled(true);
			proj_joblog_menu.setEnabled(false);
			proj_killjob_menu.setEnabled(false);
			proj_pgen_menu.setEnabled(true);

			app.getSimMenu().setEnabled(true);
			sim_param_menu.setEnabled(false);
			sim_run_menu.setEnabled(false);
			sim_stop_menu.setEnabled(false);
			sim_new_menu.setEnabled(true);
			sim_run_button.setEnabled(false);
			sim_stop_button.setEnabled(false);
			sim_new_button.setEnabled(true);
			sim_del_menu.setEnabled(true);
			sim_del_button.setEnabled(true);

			view_proj_menu.setEnabled(true);
			view_sim_menu.setEnabled(false);
			view_result_menu.setEnabled(true);
			view_clear_menu.setEnabled(true);
			plot_data_menu.setEnabled(false);
			triangle_menu.setEnabled(true);
			return;
		}

		if (state == 2) {
			new_proj_menu.setEnabled(false);
			open_proj_menu.setEnabled(false);
			close_proj_menu.setEnabled(true);
			save_proj_menu.setEnabled(true);
			new_proj_button.setEnabled(false);
			open_proj_button.setEnabled(false);
			save_proj_button.setEnabled(true);

			proj_job_menu.setEnabled(true);
			proj_joblog_menu.setEnabled(false);
			proj_killjob_menu.setEnabled(false);
			proj_pgen_menu.setEnabled(true);

			app.getSimMenu().setEnabled(true);
			sim_param_menu.setEnabled(true);
			sim_run_menu.setEnabled(true);
			sim_stop_menu.setEnabled(false);
			sim_new_menu.setEnabled(true);
			sim_run_button.setEnabled(true);
			sim_stop_button.setEnabled(false);
			sim_new_button.setEnabled(true);
			sim_del_menu.setEnabled(true);
			sim_del_button.setEnabled(true);

			view_proj_menu.setEnabled(true);
			view_sim_menu.setEnabled(true);
			view_result_menu.setEnabled(true);
			view_clear_menu.setEnabled(true);
			plot_data_menu.setEnabled(false);
			triangle_menu.setEnabled(true);
			return;
		}

		if (state == 3) {
			new_proj_menu.setEnabled(false);
			open_proj_menu.setEnabled(false);
			close_proj_menu.setEnabled(false);
			save_proj_menu.setEnabled(true);
			new_proj_button.setEnabled(false);
			open_proj_button.setEnabled(false);
			save_proj_button.setEnabled(true);

			proj_job_menu.setEnabled(false);
			proj_joblog_menu.setEnabled(false);
			proj_killjob_menu.setEnabled(false);
			proj_pgen_menu.setEnabled(true);

			app.getSimMenu().setEnabled(false);
			sim_param_menu.setEnabled(true);
			sim_run_menu.setEnabled(false);
			sim_stop_menu.setEnabled(true);
			sim_new_menu.setEnabled(true);
			sim_run_button.setEnabled(false);
			sim_stop_button.setEnabled(true);
			sim_new_button.setEnabled(true);
			sim_del_menu.setEnabled(true);
			sim_del_button.setEnabled(true);

			view_proj_menu.setEnabled(true);
			view_sim_menu.setEnabled(true);
			view_result_menu.setEnabled(true);
			view_clear_menu.setEnabled(false);
			plot_data_menu.setEnabled(true);
			triangle_menu.setEnabled(true);
			return;
		}

		if (state == 4) {
			new_proj_menu.setEnabled(false);
			open_proj_menu.setEnabled(false);
			close_proj_menu.setEnabled(false);
			save_proj_menu.setEnabled(true);
			new_proj_button.setEnabled(false);
			open_proj_button.setEnabled(false);
			save_proj_button.setEnabled(true);

			proj_job_menu.setEnabled(false);
			proj_joblog_menu.setEnabled(true);
			proj_killjob_menu.setEnabled(true);
			proj_pgen_menu.setEnabled(true);

			app.getSimMenu().setEnabled(false);
			sim_param_menu.setEnabled(false);
			sim_run_menu.setEnabled(false);
			sim_stop_menu.setEnabled(false);
			sim_new_menu.setEnabled(true);
			sim_run_button.setEnabled(false);
			sim_stop_button.setEnabled(false);
			sim_new_button.setEnabled(true);
			sim_del_menu.setEnabled(false);
			sim_del_button.setEnabled(false);

			view_proj_menu.setEnabled(true);
			view_sim_menu.setEnabled(true);
			view_result_menu.setEnabled(true);
			view_clear_menu.setEnabled(false);
			plot_data_menu.setEnabled(true);
			triangle_menu.setEnabled(true);
			return;
		}

	}

	public void setRstMenu(JMenuBar newbar) {
		rst_mbar = newbar;
	}

	/*****************************************************/
	/*********** The Action Handler **************/

	// The Action handler forward most of the data requst
	// action to the controller except the project-independent
	// data loading, and update the Guis - tree and simListMenu
	// will be notified by contoller and cooperate the update

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		// TODO: let app handle this, some
		// clean-up work is required

		if (action.compareTo("exit") == 0) {
			app.exit_structure();
		}

		if (action.compareTo("newproject") == 0) {
			app.newProject();
			return;
		}

		if (action.compareTo("closeproj") == 0) {
			app.closeProj();
		}

		if (action.equals("closefile")) {
			JInternalFrame frame = rightPane.getSelectedFrame();
			if (frame != dataframe && frame != projframe && frame != simframe
					&& frame != resultframe && frame != rstplotframe
					&& frame != summaryframe) {
				frame.dispose();
			}
			return;
		}

		if (action.compareTo("saveproj") == 0) {
			app.saveProj();
		}

		if (action.compareTo("openproj") == 0) {

			app.openProj();
			return;
		}

		if (action.compareTo("simnew") == 0) {
			app.newSim();
			return;
		}

		if (action.compareTo("simdel") == 0) {
			app.removeSimObj();
			return;
		}

		if (action.compareTo("simsetting") == 0) {
			app.simSetting();
			return;
		}

		if (action.compareTo("simrun") == 0) {
			app.runProc();
			return;
		}

		if (action.compareTo("simstop") == 0) {
			app.stopProc();
			return;
		}

		if (action.compareTo("datafile") == 0) {
			loadData();
			return;
		}

		if (action.equals("loadrst")) {
			app.loadOutsideRst();
		}

		if (action.equals("startjob")) {
			app.startJob();
			return;
		}

		if (action.equals("joblog")) {
			app.showJobLog();
			return;
		}

		if (action.equals("killjob")) {
			app.killJob();
			return;
		}

		if (action.equals("genparam")) {
			app.genParamFiles();
		}

		/*********************************************************/
		// plotting handler //
		/*********************************************************/

		// run-time plotting handler
		if (action.compareTo("viewfst") == 0) {
			app.plotDynamic("Fst");
			return;
		}
		if (action.equals("viewalpha")) {
			app.plotDynamic("Alpha");
			return;
		}
		if (action.equals("viewrec")) {
			app.plotDynamic("Rec");
			return;
		}
		if (action.equals("viewlike")) {
			app.plotDynamic("Like");
			return;
		}
		if (action.equals("viewlnpd")) {
			app.plotDynamic("LnPD");
			return;
		}

		if (action.equals("viewr")) {
			app.plotDynamic("r");
			return;
		}

		// off-time data plotting handler
		if (action.compareTo("sviewfst") == 0) {
			app.plotStatic("Fst");
			return;
		}
		if (action.equals("sviewalpha")) {
			app.plotStatic("Alpha");
			return;
		}
		if (action.equals("sviewrec")) {
			app.plotStatic("Rec");
			return;
		}
		if (action.equals("sviewr")) {
			app.plotStatic("r");
			return;
		}
		if (action.equals("sviewlnpd")) {
			app.plotStatic("LnPD");
			return;
		}
		if (action.equals("sviewlike")) {
			app.plotStatic("Like");
			return;
		}

		// hist plotters
		if (action.equals("histfst")) {
			app.plotStatic("FstHist");
			return;
		}

		if (action.equals("histalpha")) {
			app.plotStatic("AlphaHist");
			return;
		}

		if (action.equals("histrec")) {
			app.plotStatic("RecHist");
			return;
		}
		if (action.equals("histr")) {
			app.plotStatic("rHist");
			return;
		}

		if (action.equals("histlike")) {
			app.plotStatic("LikHist");
			return;
		}
		if (action.equals("histlnpd")) {
			app.plotStatic("LnpdHist");
			return;
		}
		if (action.equals("triplot")) {
			app.triPlot(1);
		}

		if (action.equals("treeplot")) {
			app.treePlot();
		}

		if (action.equals("treePlotLicense")) {
			app.treePlotLicense();
		}

		// triangle plotter
		if (action.equals("triangle")) {
			app.triPlot(0);
			return;
		}

		/**************************************************/
		/* content panel loaders */
		/**************************************************/

		if (action.compareTo("viewproj") == 0) {
			app.loadDataFrame("proj", null);
			return;
		}

		if (action.equals("viewsim")) {
			app.loadDataFrame("sim", null);
			return;
		}

		if (action.equals("viewsummary")) {
			app.loadSummary();
			return;
		}

		if (action.equals("clearconsole")) {
			console.setText("");
			return;
		}

		if (action.equals("print")) {
			processPrinting();
			return;
		}

		if (action.equals("barplot")) {
			app.loadBarPlot();
		}

		if (action.equals("helpdoc")) {
			JOptionPane.showMessageDialog(this,
					"Help document (readme.pdf) is availble in \"Help Files\" directory\n"
							+ "It is also available online in HTML format\n\n",
					"Structure Document", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		if (action.equals("about")) {
			JOptionPane.showMessageDialog(this,
					"Structure 2.3.4 (July 2012)\n",
					"About Structure 2.3.4", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

	}

	/***********************************************************/
	/******************* Uitility Method ***********************/

	// Render Console to output process manager
	public TextArea getConsole() {
		return console;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	/************************************************************/
	/********** Loading Project-Independent Data ************/

	public void loadData() {

		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(fc_dir);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			fc_dir = file.getParentFile();
			InputStream in;
			Lexer lexer;
			String filename = file.getAbsolutePath();
			/******************************************/
			/***** open input and output files... *****/

			in = OpenInputFile(filename);
			if (in == null) {
				return;
			}

			/****************************************************/
			/***** create Lexer and start reading tokens... *****/
			lexer = new Lexer(in);
			// first parse get file info
			while (lexer.getNextString() != null) {
				;
			}

			int col = lexer.getTokenCol();
			int row = lexer.getLine();
			try { // to close files...
				in.close();

			} catch (IOException e) { /* just ignore */
			}// try-catch

			// reopen the file
			in = OpenInputFile(filename);
			if (in == null) {
				return;
			}

			lexer = new Lexer(in);
			// second parse , assign vals for the table

			String[][] data = new String[row][col];

			BufferedReader infile = null;
			try {
				infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Can not Read Data Source",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String token = "";
			String str = "";
			int row_count = 0;

			Hashtable<Integer,Integer> stat = new Hashtable<Integer, Integer>(); // file counter

			while (true) {
				try {
					str = infile.readLine();
				} catch (Exception re) {
				}

				if (str == null) {
					break;
				}

				StringTokenizer st = new StringTokenizer(str);
				if (!st.hasMoreTokens()) {
					continue;
				}
				int col_count = 0;

				int word_count = 0;
				while (st.hasMoreTokens()) {
					token = st.nextToken();
					try {
						data[row_count][col_count++] = token;
						word_count++;
					} catch (ArrayIndexOutOfBoundsException e) {
					}
				}

				// write to stat hashtable
				Integer index = new Integer(word_count);
				if (stat.containsKey(index)) {
					Integer obj = stat.get(index);
					int objVal = obj.intValue();
					stat.put(index, new Integer(++objVal));
				} else {
					stat.put(new Integer(word_count), new Integer(1));
				}

				row_count++;
			}

			String[] name = new String[col];
			for (int i = 0; i < col; i++) {
				name[i] = new String("" + (i + 1));
			}
			JTable table = new JTable(data, name);

			// garbage collection
			data = null;

			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setPreferredScrollableViewportSize(new Dimension(650, 420));
			JScrollPane scrollPane = new JScrollPane(table);
			JInternalFrame frame = new JInternalFrame(new String("Data - "
					+ filename), true, true, true, true);
			frame.getContentPane().add(scrollPane);
			frame.setSize(650, 450);
			frame.setVisible(true);
			rightPane.add(frame);
			try {
				frame.setMaximum(true);
				frame.setSelected(true);
			} catch (java.beans.PropertyVetoException e) {
			}

			// show message dialog box
			String info = filename + "\n\n";
			Enumeration<Integer> keys = stat.keys();
			while (keys.hasMoreElements()) {
				Integer index = keys.nextElement();
				Integer obj = stat.get(index);
				info += new String(obj + " Lines with " + index + " Columns\n");
			}

			JOptionPane.showMessageDialog(this, info,
					"Structure: Open Data File",
					JOptionPane.INFORMATION_MESSAGE);

			return;

		}
		return;
	}

	/***********************************************************/
	/* Print Content in JInternalFrame */

	private void processPrinting() {
	}

	/**********************************************************/
	/***** opens a file for reading, returning a file ref *****/

	// Utility for loading independent data
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

	/*************************************************************/
	/******** Mehtods to handle right-hand side frames ***********/

	// load project data to right panel
	public void showData(final String[][] data, final String[] title,
			boolean genename, boolean mapdistance, boolean recessiveallele,
			boolean phaseinfo, boolean onerow, int ploidy, int numloci) {

		String[] colName = new String[data[0].length];
		final int colNum = data[0].length;
		final int rowNum = data.length;
		for (int i = 0; i < colNum; i++) {
			colName[i] = " ";
		}
		final JTable table = new JTable(new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			int rows = rowNum, cols = colNum;

			public String getColumnName(int col) {
				return title[col];
			}

			public int getRowCount() {
				return rows;
			}

			public int getColumnCount() {
				return cols;
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
		});

		// format the table

		int lociCol = numloci;
		if (onerow) {
			lociCol *= ploidy;
		}

		TableColumnModel cm = table.getColumnModel();

		for (int i = 0; i < colNum; i++) {
			if (i >= colNum - lociCol) {
				TableColumn column = cm.getColumn(i);
				column
						.setCellRenderer(new DataRowRenderer(rowNum, genename,
								mapdistance, phaseinfo, onerow, ploidy,
								recessiveallele));
			}
		}

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(new Dimension(650, 420));
		JScrollPane scrollPane = new JScrollPane(table);
		dataframe = new JInternalFrame("Project Data", true, true, true, true);
		dataframe.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		dataframe.addInternalFrameListener(framelistener);

		dataframe.getContentPane().add(scrollPane);
		dataframe.setSize(650, 450);
		dataframe.setVisible(true);
		rightPane.add(dataframe);
		try {

			dataframe.setSelected(true);
			dataframe.setMaximum(true);
		} catch (java.beans.PropertyVetoException e) {
		}

	}

	public void loadSummaryFrame(JInternalFrame sp, String title) {

		hideAllFrames();
		if (summaryframe != null) {
			summaryframe.dispose();
		}

		summaryframe = sp;
		summaryframe.setTitle(title);

		summaryframe.setSize(650, 450);
		summaryframe.setVisible(true);

		rightPane.add(summaryframe);
		try {
			summaryframe.setSelected(true);
			summaryframe.setMaximum(true);

		} catch (java.beans.PropertyVetoException e) {
		}

		return;
	}

	public int getCurrState() {
		return app.getCurrState();
	}

	public void loadDataFrame(String type, String title, String content) {
		if (type.equals("proj")) {
			hideAllFrames();
			if (projframe == null) {
				projframe = new JInternalFrame(title, true, true, true, true);
				projframe.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				projframe.addInternalFrameListener(framelistener);
				JTextArea ta = new JTextArea(content);
				ta.setFont(new Font("TimesRoman", Font.PLAIN, 15));
				ta.setLineWrap(true);
				ta.setWrapStyleWord(true);

				JScrollPane scrollPane = new JScrollPane(ta);
				ta.setCaretPosition(0);
				ta.setEditable(false);

				projframe.getContentPane().add(scrollPane);
				projframe.setSize(650, 450);
				projframe.setVisible(true);
				rightPane.add(projframe);
			}
			if (!projframe.isVisible()) {
				projframe.setVisible(true);
			}
			try {
				projframe.setSelected(true);
				projframe.setMaximum(true);

			} catch (java.beans.PropertyVetoException e) {
			}
			return;
		}

		if (type.equals("data")) {

			hideAllFrames();
			if (!dataframe.isVisible()) {
				dataframe.setVisible(true);
			}
			try {
				dataframe.setSelected(true);
				dataframe.setMaximum(true);
			} catch (java.beans.PropertyVetoException e) {
				System.err.println(e);
			}

			return;
		}

		if (type.equals("sim")) {
			hideAllFrames();
			if (simframe != null) {
				simframe.dispose();
			}

			simframe = new JInternalFrame(title, true, true, true, true);
			JTextArea ta = new JTextArea(content);
			ta.setFont(new Font("TimesRoman", Font.PLAIN, 15));
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);

			JScrollPane sp = new JScrollPane(ta);
			ta.setCaretPosition(0);
			ta.setEditable(false);

			simframe.getContentPane().add(sp);
			simframe.setSize(650, 450);
			simframe.pack();
			simframe.setVisible(true);

			rightPane.add(simframe);
			try {
				simframe.setSelected(true);
				simframe.setMaximum(true);
			} catch (java.beans.PropertyVetoException e) {
			}

			return;
		}

		if (type.equals("result")) {

			hideAllFrames();
			if (resultframe != null) {
				resultframe.dispose();
			}

			resultframe = new JInternalFrame(title, true, true, true, true);
			JTextArea ta = new JTextArea(content);
			content = null;
			ta.setFont(new Font("TimesRoman", Font.PLAIN, 15));
			ta.setMargin(new Insets(0, 25, 0, 10));
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);

			JScrollPane sp = new JScrollPane(ta);
			ta.setCaretPosition(0);
			ta.setEditable(false);
			resultframe.setJMenuBar(rst_mbar);
			resultframe.getContentPane().setLayout(new BorderLayout());
			resultframe.getContentPane().add(sp, BorderLayout.CENTER);

			resultframe.pack();
			resultframe.setVisible(true);

			rightPane.add(resultframe);
			try {
				resultframe.setSelected(true);
				resultframe.setMaximum(true);

			} catch (java.beans.PropertyVetoException e) {
			}

			return;
		}

	}

	void hideAllFrames() {

		if (dataframe != null) {
			dataframe.setVisible(false);
		}

		if (projframe != null) {
			projframe.setVisible(false);
		}

		if (simframe != null) {
			simframe.setVisible(false);
		}

		if (resultframe != null) {
			resultframe.setVisible(false);
		}

		if (rstplotframe != null) {
			rstplotframe.setVisible(false);
		}

		if (summaryframe != null) {
			summaryframe.setVisible(false);
		}
	}

	/***********************************************************/
	/********** Internal Frame Window Event Listener ***********/

	class FrameSetListener implements InternalFrameListener {

		public void internalFrameClosing(InternalFrameEvent e) {
			JInternalFrame frame = (JInternalFrame) e.getSource();
			frame.setVisible(false);
		}

		public void internalFrameClosed(InternalFrameEvent e) {
			/*
			 * JInternalFrame frame = e.getInternalFrame();
			 * frame.setVisible(false);
			 */
		}

		public void internalFrameOpened(InternalFrameEvent e) {
		}

		public void internalFrameIconified(InternalFrameEvent e) {
		}

		public void internalFrameDeiconified(InternalFrameEvent e) {

		}

		public void internalFrameActivated(InternalFrameEvent e) {

		}

		public void internalFrameDeactivated(InternalFrameEvent e) {
		}

	}

}

class DataRowRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	Color[] rowColor;

	public DataRowRenderer(int rowNum, boolean genename, boolean mapdist,
			boolean phaseinfo, boolean onerow, int ploidy,
			boolean recessiveallel) {
		super();
		int data_start_row = 0;
		if (genename) {
			data_start_row++;
		}
		if (mapdist) {
			data_start_row++;
		}
		if (recessiveallel) {
			data_start_row++;
		}
		int interval = 0;
		if (phaseinfo) {
			if (onerow) {
				interval = 1;
			} else {
				interval = ploidy;
			}
		}

		rowColor = new Color[rowNum];
		for (int i = 0; i < rowNum; i++) {
			if (i < data_start_row) {
				rowColor[i] = Color.cyan;
				continue;
			}

			if (phaseinfo && (i - data_start_row + 1) % (interval + 1) == 0) {
				rowColor[i] = Color.white;
				continue;
			}

			rowColor[i] = Color.gray;
		}

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setBackground(rowColor[row]);
		return super.getTableCellRendererComponent(table, value, isSelected,
				hasFocus, row, column);
	}

}
