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

import Obj.NSimObj;

public class FreqDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int buttonHit;

	// Values for the text fields

	private float mean = 0.01f;
	private float sd = 0.05f;
	private float lambd = 1.0f;
	private boolean onefst;
	private boolean setlambd = true;
	private boolean inferlambd = false;

	// Text fields for data entry
	JTextField meanField, sdField;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;
	private JCheckBox onefstBox;

	private JRadioButton setlambdBox;
	private JRadioButton inferlambdBox;

	private JTextField lambdField;
	private JButton lambdButton;

	private LambdDlg lambdDlg;

	// Data Model
	private NSimObj sim;

	private boolean focusIsSet;

	public FreqDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		// Create the labels.

		JLabel l1 = new JLabel("Prior Mean: ");
		JLabel l2 = new JLabel("Prior SD:   ");

		setlambdBox = new JRadioButton("Set Lambda  ");
		setlambdBox.setActionCommand("setlambd");
		setlambdBox.addActionListener(this);

		inferlambdBox = new JRadioButton("Infer Lambda");
		inferlambdBox.setActionCommand("inferlambd");
		inferlambdBox.addActionListener(this);

		ButtonGroup bg = new ButtonGroup();
		bg.add(setlambdBox);
		bg.add(inferlambdBox);

		// Create the text fields and set them up.
		meanField = new JTextField(10);
		meanField.setText("0.01");
		sdField = new JTextField(10);
		sdField.setText("0.05");
		lambdField = new JTextField(10);

		lambdButton = new JButton("Configure ...");
		lambdButton.setActionCommand("lambdbutton");
		lambdButton.addActionListener(this);

		JPanel plm = new JPanel();
		plm.add(lambdButton);

		// Tell accessibility tools about label/textfield pairs.
		l1.setLabelFor(meanField);
		l2.setLabelFor(sdField);

		// Create Buttons
		okButton = new JButton("   OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		defaultButton = new JButton("Default");
		defaultButton.addActionListener(this);

		CheckBoxListener cl = new CheckBoxListener();
		onefstBox = new JCheckBox(
				"Assume Same Value of Fst for All Subpopulations");
		onefstBox.addItemListener(cl);

		// Layout the labels in a panel.
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(l1);
		labelPane.add(l2);
		labelPane.add(setlambdBox);
		labelPane.add(inferlambdBox);

		// Layout the text fields in a panel.
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));

		JPanel p1 = new JPanel();
		p1.add(meanField);
		JPanel p2 = new JPanel();
		p2.add(sdField);
		JPanel p3 = new JPanel();
		p3.add(lambdField);
		fieldPane.add(p1);
		fieldPane.add(p2);
		fieldPane.add(p3);
		fieldPane.add(plm);

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
		c.gridwidth = 2;
		c.insets = new Insets(20, 0, 0, 0);
		gridbag.setConstraints(onefstBox, c);

		contentPane.add(onefstBox);
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
		c.insets = new Insets(25, 20, 20, 20);
		gridbag.setConstraints(buttonPane, c);
		contentPane.add(buttonPane);
		// contentPane.add(buttonPane, BorderLayout.SOUTH);
		setContentPane(contentPane);
		setLocationRelativeTo(owner);

		if (!sim.isNew() && !def) {
			onefst = sim.ONEFST;
			onefstBox.setSelected(sim.ONEFST);
			mean = sim.FPRIORMEAN;
			sd = sim.FPRIORSD;
			meanField.setText("" + mean);
			sdField.setText("" + sd);

			setlambd = sim.FQSETLAMBDA;
			inferlambd = sim.FQINFERLAMBDA;
			if (setlambd) {
				lambd = sim.LAMBDA;
			}
			if (inferlambd) {
				lambdDlg = new LambdDlg(this, sim, false);
			}

		}

		updatePanelUI();

	}

	private void updatePanelUI() {
		lambdField.setText("" + lambd);
		lambdField.setEnabled(setlambd);
		inferlambdBox.setSelected(inferlambd);
		lambdButton.setEnabled(inferlambd);
		setlambdBox.setSelected(setlambd);
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
			return;
		}

		if (cmd.equals("cancel")) {
			buttonHit = JOptionPane.CANCEL_OPTION;
			reset();
			setVisible(false);
			return;
		}

		if (cmd.equals("setlambd")) {
			if (!setlambd) {
				setlambd = true;
				inferlambd = false;
			}
			updatePanelUI();
			return;
		}

		if (cmd.equals("inferlambd")) {
			if (!inferlambd) {
				inferlambd = true;
				setlambd = false;
				lambd = 1.0f;
			}
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
	}

	@SuppressWarnings("unused")
	private void setFocus() {
		if (!focusIsSet) {
			meanField.requestFocus();
			focusIsSet = true;
		}
	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;
	}

	public void reset() {
		meanField.setText("0.01");
		mean = 0.01f;
		sdField.setText("0.05");
		sd = 0.05f;
		onefstBox.setSelected(false);
		onefst = false;

		setlambd = true;
		inferlambd = false;
		lambd = 1.0f;
		updatePanelUI();
	}

	public boolean validateData() {
		String errString = "";

		String tmp = meanField.getText();
		try {
			mean = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\n\"Prior Mean\" Must be a Real Number");
		}

		tmp = sdField.getText();
		try {
			sd = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\n\"Prior SD\" Must be a Real Number");
		}

		if (setlambd) {
			tmp = lambdField.getText();
			try {
				lambd = Float.parseFloat(tmp);
			} catch (NumberFormatException e) {
				errString += new String("\nLambda Must be a Real Number");
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

		sim.ONEFST = onefst;
		sim.FPRIORMEAN = mean;
		sim.FPRIORSD = sd;

		sim.FQSETLAMBDA = setlambd;
		sim.FQINFERLAMBDA = inferlambd;

		if (setlambd) {
			sim.LAMBDA = lambd;
		}
		if (inferlambd) {
			if (lambdDlg == null) {
				sim.LAMBDA = 1.0f;
				sim.POPSPECIFICLAMBDA = false;
			} else {
				lambdDlg.updateData();
			}
		}

	}

	class CheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getItemSelectable();
			boolean val = true;

			if (e.getStateChange() == ItemEvent.DESELECTED) {
				val = false;
			}

			if (source == onefstBox) {
				onefst = val;
			}
		}
	}

}
