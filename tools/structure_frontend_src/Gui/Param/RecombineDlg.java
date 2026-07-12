// update 02/06/2002 change noqsBox maps to 
// new sim varible sitebysite

package Gui.Param;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import Obj.NProjObj;
import Obj.NSimObj;

public class RecombineDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int buttonHit;

	JTextField rstartField, rstepField, rmaxField, rminField, rstdField;
	JCheckBox noqsBox;
	JCheckBox markovBox;
	JRadioButton defaultburninBox;
	JRadioButton specifyburninBox;

	JTextField burninField;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;

	// Data Member
	private float rstart = -2f;
	private float rmax = 2f;
	private float rmin = -4f;
	private float rstd = 0.1f;
	private boolean sitebysite;
	private boolean markovphase;
	private boolean defaultburnin = true;
	private int admburnin;

	// Data Model
	private NSimObj sim;

	public RecombineDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		// Create the labels.

		JLabel l3 = new JLabel("      LOG10(RMIN):");
		JLabel l4 = new JLabel("      LOG10(RMAX):");
		JLabel l5 = new JLabel("      LOG10(RPROPSD):");
		JLabel l1 = new JLabel("      LOG10(RSTART):");
		JLabel l2 = new JLabel("      RSTEP:    ");

		// Create the text fields and set them up.
		rstartField = new JTextField(10);
		rstartField.setText("-2");

		rmaxField = new JTextField(10);
		rmaxField.setText("2");

		rminField = new JTextField(10);
		rminField.setText("-4");

		rstdField = new JTextField(10);
		rstdField.setText("0.1");

		rstepField = new JTextField(10);
		rstepField.setText("1.05");

		// Tell accessibility tools about label/textfield pairs.
		l1.setLabelFor(rstartField);
		l2.setLabelFor(rstepField);
		l3.setLabelFor(rminField);
		l4.setLabelFor(rmaxField);
		l5.setLabelFor(rstdField);

		// Create Buttons
		okButton = new JButton("   OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		defaultButton = new JButton("Default");
		defaultButton.addActionListener(this);

		JLabel blabel = new JLabel("Admixture Burnin Length   ");

		defaultburninBox = new JRadioButton(
				"  Use default admixture burnin length:");
		defaultburninBox.setSelected(true);
		defaultburninBox.setActionCommand("dburnin");
		defaultburninBox.addActionListener(this);

		specifyburninBox = new JRadioButton(
				"  Specify admixture burnin length ");
		specifyburninBox.setSelected(true);
		specifyburninBox.setActionCommand("sburnin");
		specifyburninBox.addActionListener(this);

		ButtonGroup bp = new ButtonGroup();
		bp.add(defaultburninBox);
		bp.add(specifyburninBox);

		burninField = new JTextField(5);

		JPanel tl = new JPanel();
		tl.setLayout(new GridLayout(0, 1));
		tl.add(blabel);
		tl.add(defaultburninBox);
		tl.add(specifyburninBox);

		JPanel tr = new JPanel();
		tr.setLayout(new GridLayout(0, 1));
		tr.add(new JLabel("               "));
		tr.add(new JLabel("      BURNIN/2 "));
		JPanel pbf = new JPanel();
		pbf.add(burninField);
		tr.add(pbf);

		CheckBoxListener cl = new CheckBoxListener();
		noqsBox = new JCheckBox("  Print Site-by-Site Results ");
		noqsBox.addItemListener(cl);

		markovBox = new JCheckBox("  Phase info follows a Markov model ");
		markovBox.addItemListener(cl);

		// Layout the labels in a panel.
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(l3);
		labelPane.add(l4);
		labelPane.add(l5);
		labelPane.add(l1);

		// Layout the text fields in a panel.
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));

		JPanel p1 = new JPanel();
		p1.add(rstartField);

		JPanel p2 = new JPanel();
		p2.add(rstepField);

		JPanel p3 = new JPanel();
		p3.add(rminField);

		JPanel p4 = new JPanel();
		p4.add(rmaxField);

		JPanel p5 = new JPanel();
		p5.add(rstdField);

		fieldPane.add(p3);
		fieldPane.add(p4);
		fieldPane.add(p5);
		fieldPane.add(p1);

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
		c.gridwidth = 1;
		c.insets = new Insets(20, 0, 0, 0);
		gridbag.setConstraints(tl, c);
		contentPane.add(tl);

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(20, 0, 0, 0);
		gridbag.setConstraints(tr, c);
		contentPane.add(tr);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		gridbag.setConstraints(labelPane, c);
		contentPane.add(labelPane);

		c.gridx = 1;
		gridbag.setConstraints(fieldPane, c);
		contentPane.add(fieldPane);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(5, 0, 0, 0);
		gridbag.setConstraints(noqsBox, c);
		contentPane.add(noqsBox);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.insets = new Insets(5, 0, 0, 0);
		gridbag.setConstraints(markovBox, c);
		contentPane.add(markovBox);

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(25, 20, 20, 20);
		gridbag.setConstraints(buttonPane, c);
		contentPane.add(buttonPane);
		setContentPane(contentPane);

		setLocationRelativeTo(owner);

		NProjObj proj = sim.getProjObj();
		if (!proj.getPhaseInfo()) {
			markovBox.setEnabled(false);
			markovphase = false;
		}

		if (!sim.isNew() && !def) {
			rstart = sim.RSTART;
			sitebysite = sim.SITEBYSITE;
			markovphase = sim.MARKOVPHASE;

			noqsBox.setSelected(sitebysite);
			markovBox.setSelected(markovphase);
			rstartField.setText("" + rstart);
			rmaxField.setText("" + rmax);
			rminField.setText("" + rmin);
			rstdField.setText("" + rstd);

			defaultburnin = sim.USEDEFAULTADMBURNIN;
			admburnin = sim.ADMBURNIN;
		}

		if (defaultburnin) {
			defaultburninBox.setSelected(true);
			burninField.setText("");
			burninField.setEnabled(false);
		} else {
			specifyburninBox.setSelected(true);
			burninField.setText("" + admburnin);
			burninField.setEnabled(true);
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

		if (cmd.equals("sburnin")) {
			burninField.setEnabled(true);
			defaultburnin = false;
		}

		if (cmd.equals("dburnin")) {
			burninField.setText("");
			burninField.setEnabled(false);
			defaultburnin = true;
		}

	}

	public void reset() {
		rstartField.setText("-2");
		rstart = -2f;

		rmaxField.setText("2");
		rmax = 2f;

		rminField.setText("-4");
		rmin = -4f;

		rstdField.setText("0.1");
		rstd = 0.1f;

		burninField.setText("");
		burninField.setEnabled(false);
		defaultburnin = true;
		defaultburninBox.setSelected(true);
		noqsBox.setSelected(false);
		markovBox.setSelected(false);
		markovphase = false;
		sitebysite = false;
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

		String tmp = rstartField.getText();
		try {
			rstart = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\nLOG10(RSTART) Must be a Real Number");
		}

		tmp = rmaxField.getText();
		try {
			rmax = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\nLOG10(RMAX) Must be a Real Number");
		}

		tmp = rminField.getText();
		try {
			rmin = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\nLOG10(RMIN) Must be a Real Number");
		}

		tmp = rstdField.getText();
		try {
			rstd = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\nLOG10(RPROPSTD) Must be a Real Number");
		}

		if (!defaultburnin) {
			tmp = burninField.getText();
			try {
				admburnin = Integer.parseInt(tmp);
			} catch (NumberFormatException e) {
				errString += new String(
						"\nAdmixture Burnin Length  Must be an Integer");
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

		sim.USEDEFAULTADMBURNIN = defaultburnin;
		if (!defaultburnin) {
			sim.ADMBURNIN = admburnin;
		}
		sim.SITEBYSITE = sitebysite;
		sim.MARKOVPHASE = markovphase;
		sim.RMAX = rmax;
		sim.RSTD = rstd;
		sim.RMIN = rmin;
		sim.RSTART = rstart;
	}

	class CheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getItemSelectable();
			boolean val = true;

			if (e.getStateChange() == ItemEvent.DESELECTED) {
				val = false;
			}

			if (source == noqsBox) {
				sitebysite = val;
			}

			if (source == markovBox) {
				markovphase = val;
			}

			return;
		}
	}

}
