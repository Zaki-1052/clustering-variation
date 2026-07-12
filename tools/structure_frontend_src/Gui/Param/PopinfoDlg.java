package Gui.Param;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import Obj.NProjObj;
import Obj.NSimObj;

public class PopinfoDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int buttonHit;

	// controls
	JTextField gensField, migrField;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;

	private JRadioButton admixBox;
	private JRadioButton noadmixBox;
	private JRadioButton linkageBox;

	private JButton admixButton;
	private JButton noadmixButton;
	private JButton linkageButton;

	private ButtonGroup bp = new ButtonGroup();

	// Data Memebers
	private int gens = 2;
	private float migr = 0.05f;
	private boolean admix = true;
	private boolean noadmix = false;
	private boolean recomb = false;

	private AdmixDlg admixDlg;
	private RecombineDlg recombineDlg;

	// Data Model
	private NSimObj sim;

	public PopinfoDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		// Create the labels.

		JLabel l1 = new JLabel("GENSBACK:   ");
		JLabel l2 = new JLabel("MIGRPRIOR:  ");

		admixBox = new JRadioButton("Admixture Model");
		admixBox.setActionCommand("admixbox");
		admixBox.addActionListener(this);
		noadmixBox = new JRadioButton("No Admixture Model");
		noadmixBox.setActionCommand("noadmixbox");
		noadmixBox.addActionListener(this);
		linkageBox = new JRadioButton("Linkage Model");
		linkageBox.setActionCommand("recombbox");
		linkageBox.addActionListener(this);

		admixButton = new JButton("Configure...");
		admixButton.setActionCommand("admixbutton");
		admixButton.addActionListener(this);

		noadmixButton = new JButton("Configure...");

		linkageButton = new JButton("Configure...");
		linkageButton.setActionCommand("recombbutton");
		linkageButton.addActionListener(this);

		noadmixButton.setVisible(false);

		bp.add(admixBox);
		bp.add(noadmixBox);
		bp.add(linkageBox);

		JLabel l4 = new JLabel("For any individuals without popinfo data, use");
		JPanel pt = new JPanel();
		pt.setLayout(new GridLayout(0, 1));
		pt.add(l4);
		pt.add(noadmixBox);
		pt.add(admixBox);
		pt.add(linkageBox);

		JPanel ptb = new JPanel();
		ptb.setLayout(new GridLayout(0, 1));

		JPanel bp1 = new JPanel();
		bp1.add(noadmixButton);
		JPanel bp2 = new JPanel();
		bp2.add(admixButton);
		JPanel bp3 = new JPanel();
		bp3.add(linkageButton);

		ptb.add(new JPanel());
		ptb.add(bp1);
		ptb.add(bp2);
		ptb.add(bp3);

		// Create the text fields and set them up.
		gensField = new JTextField(10);
		gensField.setText("2");
		migrField = new JTextField(10);
		migrField.setText("0.05");

		// Tell accessibility tools about label/textfield pairs.
		l1.setLabelFor(gensField);
		l2.setLabelFor(migrField);

		// Create Buttons
		okButton = new JButton("   OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		defaultButton = new JButton("Default");
		defaultButton.addActionListener(this);

		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(l1);
		labelPane.add(l2);

		// Layout the text fields in a panel.
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));

		JPanel p1 = new JPanel();
		p1.add(gensField);
		JPanel p2 = new JPanel();
		p2.add(migrField);
		fieldPane.add(p1);
		fieldPane.add(p2);

		// Layout the buttons in a panel
		JPanel buttonPane = new JPanel();
		buttonPane.add(cancelButton);
		buttonPane.add(defaultButton);
		buttonPane.add(okButton);

		// Put the panels in another panel, labels on left,
		// text fields on right.
		JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 4, 20));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(20, 50, 0, 0);

		gridbag.setConstraints(labelPane, c);
		contentPane.add(labelPane);

		c.gridx = 1;
		c.insets = new Insets(20, 0, 0, 50);
		gridbag.setConstraints(fieldPane, c);
		contentPane.add(fieldPane);

		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(20, 50, 0, 0);
		gridbag.setConstraints(pt, c);
		contentPane.add(pt);

		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(20, 0, 0, 50);
		gridbag.setConstraints(ptb, c);
		contentPane.add(ptb);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(25, 20, 20, 20);
		gridbag.setConstraints(buttonPane, c);
		contentPane.add(buttonPane);

		contentPane.setPreferredSize(new Dimension(550, 300));
		setContentPane(contentPane);

		setLocationRelativeTo(owner);

		if (!sim.isNew() && !def) {
			gens = sim.GENSBACK;
			migr = sim.MIGRPRIOR;
			gensField.setText("" + gens);
			migrField.setText("" + migr);

			if (sim.POPRECOMBINE) {
				recomb = true;
				admix = false;
				noadmix = false;
			}
			if (!sim.POPRECOMBINE && !sim.POPNOADMIX) {
				recomb = false;
				admix = true;
				noadmix = false;
			}
			if (!sim.POPRECOMBINE && sim.POPNOADMIX) {
				noadmix = true;
				admix = false;
				recomb = false;
			}
			if (admix) {
				admixDlg = new AdmixDlg(this, sim, false);
			}
			if (recomb) {
				recombineDlg = new RecombineDlg(this, sim, false);
			}

		}

		NProjObj proj = sim.getProjObj();
		if (!proj.getMapDistance()) {
			linkageBox.setEnabled(false);
			recomb = false;
		} else if (proj.getPloidy() > 2 && !proj.getPhased()) {
			linkageBox.setEnabled(false);
			recomb = false;
		}

		updatePanelUI();
	}

	private void updatePanelUI() {

		noadmixBox.setSelected(noadmix);

		admixBox.setSelected(admix);
		admixButton.setEnabled(admix);
		linkageBox.setSelected(recomb);
		linkageButton.setEnabled(recomb);
		if (recomb) {
			gensField.setText("0");
			gensField.setEnabled(false);
			migrField.setText("0.00");
			migrField.setEnabled(false);
		} else {
			gensField.setText("" + gens);
			gensField.setEnabled(true);
			migrField.setText("" + migr);
			migrField.setEnabled(true);
		}
	}

	public void actionPerformed(ActionEvent event) {

		String cmd = event.getActionCommand();

		if (cmd.equals("Default")) {
			reset();
			return;
		}

		if (cmd.equals("ok")) {
			if (!validateData()) {
				return;
			}
			buttonHit = JOptionPane.OK_OPTION;
			setVisible(false);
		}

		if (cmd.equals("cancel")) {
			buttonHit = JOptionPane.CANCEL_OPTION;
			reset();
			setVisible(false);
		}

		if (cmd.equals("admixbox")) {
			admix = true;
			noadmix = false;
			recomb = false;
			recombineDlg = null;
			updatePanelUI();
			return;
		}

		if (cmd.equals("noadmixbox")) {
			admix = false;
			noadmix = true;
			recomb = false;
			admixDlg = null;
			recombineDlg = null;
			updatePanelUI();
			return;
		}

		if (cmd.equals("recombbox")) {
			admix = false;
			noadmix = false;
			recomb = true;
			admixDlg = null;
			updatePanelUI();
			return;
		}

		if (cmd.equals("recombbutton")) {
			if (recombineDlg == null) {
				recombineDlg = new RecombineDlg(this, sim, true);
			}

			recombineDlg.showDialog();
			return;
		}

		if (cmd.equals("admixbutton")) {
			if (admixDlg == null) {
				admixDlg = new AdmixDlg(this, sim, true);
			}
			admixDlg.showDialog();

			return;
		}

	}

	public void reset() {

		migrField.setText("0.05");
		migr = 0.05f;
		gensField.setText("2");
		gens = 2;
		admix = true;
		noadmix = false;
		recomb = false;
		admixDlg = null;
		recombineDlg = null;
		updatePanelUI();

	}

	public int showDialog() {

		setModal(true);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	public boolean validateData() {
		String errString = "";

		if (!recomb) {
			String tmp = gensField.getText();
			try {
				gens = Integer.parseInt(tmp);
			} catch (NumberFormatException e) {
				errString += new String("\nGENSBACK Must be an Integer");
			}

			tmp = migrField.getText();
			try {
				migr = Float.parseFloat(tmp);
			} catch (NumberFormatException e) {
				errString += new String("\nMIGRPRIOR Must be a Real Number");
			}
		}

		if (errString.compareTo("") != 0) {
			String prefix = "Errors:\n";
			errString = prefix.concat(errString);
			errString += new String("\n\n");
			JOptionPane.showMessageDialog(this, errString, "errors",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public void updateData() {

		sim.GENSBACK = gens;
		sim.MIGRPRIOR = migr;

		if (recomb) {
			sim.POPRECOMBINE = true;
			sim.POPNOADMIX = true;

			if (recombineDlg == null) {

				sim.USEDEFAULTADMBURNIN = true;
				sim.SITEBYSITE = false;
				sim.RSTART = -2f;
				sim.RMAX = 2f;
				sim.RSTD = 0.1f;
				sim.RMIN = -4f;
				sim.MARKOVPHASE = false;

			} else {
				recombineDlg.updateData();
			}
		}

		if (!recomb && admix) {

			sim.POPNOADMIX = false;
			sim.POPRECOMBINE = false;

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
		if (!recomb && !admix) {
			sim.POPNOADMIX = true;
		}

	}

}
