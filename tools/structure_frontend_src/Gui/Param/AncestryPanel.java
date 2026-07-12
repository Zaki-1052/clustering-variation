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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import Obj.NProjObj;
import Obj.NSimObj;

public class AncestryPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// controls
	private JRadioButton admixBox;
	private JRadioButton noadmixBox;
	private JRadioButton linkageBox;
	private JRadioButton popinfoBox;

	private ButtonGroup bp;

	private JButton admixButton;
	private JButton noadmixButton;
	private JButton linkageButton;
	private JButton popinfoButton;

	private JButton defaultButton;

	private RecombineDlg recombineDlg;
	private PopinfoDlg popinfoDlg;
	private AdmixDlg admixDlg;

	// v2.3 new features
	private JCheckBox noadmixPriorBox;
	private JCheckBox admixPriorBox;

	// data members
	private boolean noadmix = false;
	private boolean admix = true;
	private boolean popinfo = false;
	private boolean recomb = false;
	private boolean locationprior = false;

	// data model
	private NSimObj sim;

	public AncestryPanel(NSimObj sim) {

		this.sim = sim;

		bp = new ButtonGroup();
		admixBox = new JRadioButton("Use Admixture Model");
		bp.add(admixBox);
		admixBox.setActionCommand("admixbox");
		admixBox.addActionListener(this);

		noadmixBox = new JRadioButton("Use No Admixture Model");
		bp.add(noadmixBox);
		noadmixBox.setActionCommand("noadmixbox");
		noadmixBox.addActionListener(this);

		noadmixPriorBox = new JCheckBox(
				"Use sampling locations as prior (LOCPRIOR)");
		noadmixPriorBox.setActionCommand("locationprior");
		noadmixPriorBox.addActionListener(this);
		admixPriorBox = new JCheckBox(
				"Use sampling locations as prior (LOCPRIOR)");
		admixPriorBox.setActionCommand("locationprior");
		admixPriorBox.addActionListener(this);

		JPanel boxpane1 = new JPanel();
		boxpane1.add(new JLabel());
		boxpane1.add(noadmixPriorBox);

		JPanel boxpane2 = new JPanel();
		boxpane2.add(new JLabel());
		boxpane2.add(admixPriorBox);

		linkageBox = new JRadioButton("Use Linkage Model");
		bp.add(linkageBox);
		linkageBox.setActionCommand("linkagebox");
		linkageBox.addActionListener(this);

		popinfoBox = new JRadioButton(
				"Use Population Information to test for migrants");
		bp.add(popinfoBox);
		popinfoBox.setActionCommand("popinfobox");
		popinfoBox.addActionListener(this);

		admixButton = new JButton("Advanced ...");
		admixButton.setActionCommand("admixbutton");
		admixButton.addActionListener(this);

		noadmixButton = new JButton("Advanced ...");
		noadmixButton.setVisible(false);

		linkageButton = new JButton("Advanced ...");
		linkageButton.setActionCommand("linkagebutton");
		linkageButton.addActionListener(this);

		popinfoButton = new JButton("Advanced ...");
		popinfoButton.setActionCommand("popinfobutton");
		popinfoButton.addActionListener(this);

		JPanel pt1 = new JPanel();
		pt1.add(noadmixButton);

		JPanel pt2 = new JPanel();
		pt2.add(admixButton);

		JPanel pt3 = new JPanel();
		pt3.add(linkageButton);

		JPanel pt4 = new JPanel();
		pt4.add(popinfoButton);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		p1.add(noadmixBox);
		p1.add(boxpane1);
		p1.add(admixBox);
		p1.add(boxpane2);
		p1.add(linkageBox);
		p1.add(popinfoBox);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		p2.add(pt1);
		p2.add(new JLabel());
		p2.add(pt2);
		p2.add(new JLabel());
		p2.add(pt3);
		p2.add(pt4);

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

		gridbag.setConstraints(topPane, c);
		mainPane.add(topPane);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.insets = new Insets(10, 70, 10, 0);
		gridbag.setConstraints(p1, c);
		mainPane.add(p1);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.insets = new Insets(10, 0, 10, 70);
		gridbag.setConstraints(p2, c);
		mainPane.add(p2);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.insets = new Insets(10, 175, 10, 0);
		gridbag.setConstraints(bottomPane, c);
		mainPane.add(bottomPane);

		mainPane.setPreferredSize(new Dimension(500, 225));

		this.setLayout(new BorderLayout());

		add(mainPane, BorderLayout.CENTER);

		if (!sim.isNew()) {
			if (sim.USEPOPINFO) {
				popinfo = true;
				noadmix = false;
				admix = false;
				recomb = false;
				popinfoDlg = new PopinfoDlg(this, sim, false);
			} else if (sim.RECOMBINE) {
				popinfo = false;
				noadmix = false;
				admix = false;
				recomb = true;
				recombineDlg = new RecombineDlg(this, sim, false);
			} else if (!sim.NOADMIX) {
				popinfo = false;
				noadmix = false;
				admix = true;
				recomb = false;
				admixDlg = new AdmixDlg(this, sim, false);
			} else {
				popinfo = false;
				noadmix = true;
				admix = false;
				recomb = false;
			}
			locationprior = sim.LOCPRIOR;
		}

		NProjObj proj = sim.getProjObj();
		if (!proj.getMapDistance()) {
			linkageBox.setEnabled(false);
			recomb = false;
		} else if (proj.getPloidy() > 2 && !proj.getPhased()) {
			linkageBox.setEnabled(false);
			recomb = false;
		}

		if (!proj.getLocData() && !proj.getPopId()) {
			admixPriorBox.setEnabled(false);
			noadmixPriorBox.setEnabled(false);
			locationprior = false;
		}

		updatePanelUI();

	}

	private void updatePanelUI() {

		noadmixBox.setSelected(noadmix);
		noadmixPriorBox.setEnabled(noadmix);
		noadmixPriorBox.setSelected(noadmix && locationprior && !recomb);
		admixBox.setSelected(admix);
		admixPriorBox.setEnabled(admix);
		admixPriorBox.setSelected(admix && locationprior && !recomb);
		admixButton.setEnabled(admix);
		linkageBox.setSelected(recomb);
		linkageButton.setEnabled(recomb);
		popinfoBox.setSelected(popinfo);
		popinfoButton.setEnabled(popinfo);

	}

	private void reset() {
		noadmix = false;
		admix = true;
		recomb = false;
		locationprior = false;
		popinfo = false;
		popinfoDlg = null;
		admixDlg = null;
		recombineDlg = null;
		updatePanelUI();
	}

	public void updateData() {

		if (noadmix) {
			sim.NOADMIX = true;
			sim.RECOMBINE = false;
			sim.USEPOPINFO = false;
			// bug fixed by William 05012009
			// return;
		}

		if (admix) {
			sim.NOADMIX = false;
			sim.USEPOPINFO = false;
			sim.RECOMBINE = false;

			if (admixDlg == null) {
				sim.INFERALPHA = true;
				sim.ALPHA = 1.0f;
				sim.POPALPHAS = false;
				sim.UNIFPRIORALPHA = true;
				sim.ALPHAMAX = 10.0f;
				sim.ALPHAPROPSD = 0.025f;
			} else {
				admixDlg.updateData();
			}
		}

		sim.LOCPRIOR = locationprior;

		if (popinfo) {

			sim.NOADMIX = true;
			sim.USEPOPINFO = true;
			sim.RECOMBINE = false;

			if (popinfoDlg == null) {
				sim.GENSBACK = 2;
				sim.MIGRPRIOR = 0.05f;

				sim.POPNOADMIX = false;
				sim.INFERALPHA = true;
				sim.ALPHA = 1.0f;
				sim.POPALPHAS = false;
				sim.UNIFPRIORALPHA = true;
				sim.ALPHAMAX = 10.0f;
				sim.ALPHAPROPSD = 0.025f;
				sim.MARKOVPHASE = false;
			} else {
				popinfoDlg.updateData();
			}
		}

		if (recomb) {
			sim.NOADMIX = true;
			sim.USEPOPINFO = false;
			sim.RECOMBINE = true;

			if (recombineDlg == null) {
				sim.USEDEFAULTADMBURNIN = true;
				sim.SITEBYSITE = false;
				sim.RSTART = -2f;
				sim.RMAX = 2f;
				sim.RSTD = 0.1f;
				sim.RMIN = -4f;
			} else {
				recombineDlg.updateData();
			}
		}

	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("noadmixbox")) {
			noadmix = true;
			admix = false;
			recomb = false;
			popinfo = false;
			popinfoDlg = null;
			admixDlg = null;
			recombineDlg = null;
			updatePanelUI();
			return;
		}

		if (cmd.equals("admixbox")) {
			noadmix = false;
			admix = true;
			recomb = false;
			popinfo = false;
			popinfoDlg = null;
			recombineDlg = null;
			updatePanelUI();
			return;

		}

		if (cmd.equals("linkagebox")) {
			noadmix = false;
			admix = false;
			recomb = true;
			popinfo = false;
			popinfoDlg = null;
			admixDlg = null;
			updatePanelUI();
			return;

		}

		if (cmd.equals("popinfobox")) {
			noadmix = false;
			admix = false;
			recomb = false;
			popinfo = true;
			admixDlg = null;
			recombineDlg = null;
			updatePanelUI();
			return;
		}

		if (cmd.equals("admixbutton")) {
			if (admixDlg == null) {
				admixDlg = new AdmixDlg(this, sim, true);
			}
			admixDlg.showDialog();

			return;
		}

		if (cmd.equals("locationprior")) {
			locationprior = admixPriorBox.isSelected()
					|| noadmixPriorBox.isSelected();
		}

		if (cmd.equals("linkagebutton")) {
			if (recombineDlg == null) {
				recombineDlg = new RecombineDlg(this, sim, true);
			}

			recombineDlg.showDialog();
			return;
		}

		if (cmd.equals("popinfobutton")) {
			if (popinfoDlg == null) {
				popinfoDlg = new PopinfoDlg(this, sim, true);
			}

			popinfoDlg.showDialog();
			return;
		}

		if (cmd.equals("default")) {
			reset();
			return;
		}

	}

}
