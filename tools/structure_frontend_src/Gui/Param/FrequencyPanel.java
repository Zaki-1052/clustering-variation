package Gui.Param;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import Obj.NSimObj;

public class FrequencyPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// controls
	private JRadioButton corrfreqBox;
	private JRadioButton indifreqBox;
	private JRadioButton lambdBox;

	private ButtonGroup bp;

	private JButton corrfreqButton;
	private JButton indifreqButton;
	private JButton lambdButton;

	private JButton defaultButton;

	private FreqDlg freqDlg;
	private LambdDlg lambdDlg;
	private IndiFreqDlg indiDlg;

	// data members
	private boolean corrfreq = true;
	private boolean indifreq = false;
	private boolean inferlambd = false;

	// data model
	private NSimObj sim;

	public FrequencyPanel(NSimObj sim) {

		this.sim = sim;

		bp = new ButtonGroup();

		corrfreqBox = new JRadioButton("Allele Frequencies Correlated");
		bp.add(corrfreqBox);
		corrfreqBox.setActionCommand("corrfreqbox");
		corrfreqBox.addActionListener(this);

		indifreqBox = new JRadioButton("Allele Frequencies Independent");
		bp.add(indifreqBox);
		indifreqBox.setActionCommand("indifreqbox");
		indifreqBox.addActionListener(this);

		lambdBox = new JRadioButton("Infer Lambda                  ");
		bp.add(lambdBox);
		lambdBox.setActionCommand("lambdbox");
		lambdBox.addActionListener(this);

		corrfreqButton = new JButton("Advanced ...");
		corrfreqButton.setActionCommand("corrfreqbutton");
		corrfreqButton.addActionListener(this);

		indifreqButton = new JButton("Advanced ...");
		indifreqButton.setActionCommand("indifreqbutton");
		indifreqButton.addActionListener(this);

		lambdButton = new JButton("Advanced ...");
		lambdButton.setActionCommand("lambdbutton");
		lambdButton.addActionListener(this);

		JPanel pt1 = new JPanel();
		pt1.add(corrfreqButton);

		JPanel pt2 = new JPanel();
		pt2.add(indifreqButton);

		JPanel pt3 = new JPanel();
		pt3.add(lambdButton);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		p1.add(corrfreqBox);
		p1.add(indifreqBox);
		p1.add(lambdBox);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		p2.add(pt1);
		p2.add(pt2);
		p2.add(pt3);

		JPanel topPane = new JPanel();
		JLabel title = new JLabel("Select ONE from the following:");
		topPane.add(title, BorderLayout.CENTER);
		topPane.setPreferredSize(new Dimension(500, 30));

		JPanel bottomPane = new JPanel();
		defaultButton = new JButton("Default Setting");
		defaultButton.setActionCommand("default");
		defaultButton.addActionListener(this);

		bottomPane.add(defaultButton);
		bottomPane.setPreferredSize(new Dimension(500, 30));

		JPanel mainPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		mainPane.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.insets = new Insets(50, 0, 0, 0);
		gridbag.setConstraints(topPane, c);
		mainPane.add(topPane);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.insets = new Insets(40, 50, 10, 0);
		gridbag.setConstraints(p1, c);
		mainPane.add(p1);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.insets = new Insets(40, 0, 10, 50);
		gridbag.setConstraints(p2, c);
		mainPane.add(p2);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.insets = new Insets(30, 175, 10, 0);
		gridbag.setConstraints(bottomPane, c);
		mainPane.add(bottomPane);

		mainPane.setPreferredSize(new Dimension(500, 300));
		this.setLayout(new BorderLayout());
		add(mainPane, BorderLayout.CENTER);

		if (!sim.isNew()) {
			if (sim.FREQSCORR) {
				corrfreq = true;
				indifreq = false;
				inferlambd = false;
				freqDlg = new FreqDlg(this, sim, false);
			} else if (sim.INFERLAMBDA) {
				corrfreq = false;
				indifreq = false;
				inferlambd = true;
				lambdDlg = new LambdDlg(this, sim, false);
			} else {
				corrfreq = false;
				indifreq = true;
				inferlambd = false;
				indiDlg = new IndiFreqDlg(this, sim, false);
			}
		}

		updatePanelUI();

	}

	private void updatePanelUI() {
		corrfreqBox.setSelected(corrfreq);
		corrfreqButton.setEnabled(corrfreq);
		indifreqBox.setSelected(indifreq);
		indifreqButton.setEnabled(indifreq);
		lambdBox.setSelected(inferlambd);
		lambdButton.setEnabled(inferlambd);
	}

	public void updateData() {
		if (corrfreq) {
			sim.FREQSCORR = true;
			sim.INFERLAMBDA = false;

			if (freqDlg == null) {
				// default value
				sim.ONEFST = false;
				sim.FPRIORMEAN = 0.01f;
				sim.FPRIORSD = 0.05f;
				sim.FQSETLAMBDA = true;
				sim.LAMBDA = 1.0f;
				sim.FQINFERLAMBDA = false;

			} else {
				freqDlg.updateData();
			}
			return;
		}

		if (indifreq) {
			sim.FREQSCORR = false;
			sim.INFERLAMBDA = false;

			if (indiDlg == null) {
				sim.INDIFQSETLAMBDA = true;
				sim.LAMBDA = 1.0f;
				sim.INDIFQINFERLAMBDA = false;
			} else {
				indiDlg.updateData();
			}
			return;
		}

		if (inferlambd) {
			sim.FREQSCORR = false;
			sim.INFERLAMBDA = true;
			if (lambdDlg == null) {
				// default values
				sim.LAMBDA = 1.0f;
				sim.POPSPECIFICLAMBDA = false;
			} else {
				lambdDlg.updateData();
			}
			return;
		}

	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("corrfreqbox")) {
			corrfreq = true;
			indifreq = false;
			inferlambd = false;

			lambdDlg = null;
			updatePanelUI();

			return;
		}

		if (cmd.equals("indifreqbox")) {
			corrfreq = false;
			indifreq = true;
			inferlambd = false;

			lambdDlg = null;
			freqDlg = null;
			updatePanelUI();

			return;
		}

		if (cmd.equals("lambdbox")) {
			corrfreq = false;
			indifreq = false;
			inferlambd = true;

			freqDlg = null;

			updatePanelUI();
			return;
		}

		if (cmd.equals("lambdbutton")) {
			if (lambdDlg == null) {
				lambdDlg = new LambdDlg(this, sim, true);
			}
			lambdDlg.showDialog();
			return;
		}

		if (cmd.equals("corrfreqbutton")) {
			if (freqDlg == null) {
				freqDlg = new FreqDlg(this, sim, true);
			}
			freqDlg.showDialog();
			return;
		}

		if (cmd.equals("indifreqbutton")) {
			if (indiDlg == null) {
				indiDlg = new IndiFreqDlg(this, sim, true);
			}
			indiDlg.showDialog();
			return;
		}
		if (cmd.equals("default")) {
			corrfreq = true;
			indifreq = false;
			inferlambd = false;

			freqDlg = null;
			lambdDlg = null;

			updatePanelUI();
			return;
		}

	}

}
