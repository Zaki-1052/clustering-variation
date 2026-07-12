package Gui.Param;

import java.awt.Component;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import Obj.NSimObj;

public class IndiFreqDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int buttonHit;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;

	private JRadioButton setlambdBox;
	private JRadioButton inferlambdBox;

	private JTextField lambdField;
	private JButton lambdButton;

	private LambdDlg lambdDlg;

	// Data Members
	private boolean setlambd = true;
	private boolean inferlambd = false;
	private float lambd = 1.0f;

	// Data Model
	private NSimObj sim;

	public IndiFreqDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		setlambdBox = new JRadioButton("Set Lambda  ");
		inferlambdBox = new JRadioButton("Infer Lambda");

		// Create the text fields and set them up.

		setlambdBox.setActionCommand("setlambd");
		setlambdBox.addActionListener(this);

		inferlambdBox.setActionCommand("inferlambd");
		inferlambdBox.addActionListener(this);

		ButtonGroup bg = new ButtonGroup();
		bg.add(setlambdBox);
		bg.add(inferlambdBox);

		lambdField = new JTextField(10);

		lambdButton = new JButton("Configure ...");
		lambdButton.setActionCommand("lambdbutton");
		lambdButton.addActionListener(this);

		JPanel plm = new JPanel();
		plm.add(lambdButton);

		// Create Buttons
		okButton = new JButton("   OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		defaultButton = new JButton("Default");
		defaultButton.addActionListener(this);

		// Layout the labels in a panel.
		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(setlambdBox);
		labelPane.add(inferlambdBox);

		// Layout the text fields in a panel.
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));

		JPanel p1 = new JPanel();
		p1.add(lambdField);
		fieldPane.add(p1);
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
			setlambd = sim.INDIFQSETLAMBDA;
			inferlambd = sim.INDIFQINFERLAMBDA;
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
		setlambdBox.setSelected(setlambd);
		lambdField.setText("" + lambd);
		lambdField.setEnabled(setlambd);
		inferlambdBox.setSelected(inferlambd);
		lambdButton.setEnabled(inferlambd);
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

	public int showDialog() {
		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;
	}

	public void reset() {
		setlambd = true;
		inferlambd = false;
		lambd = 1.0f;
		updatePanelUI();
	}

	public boolean validateData() {

		String errString = "";
		if (setlambd) {
			String tmp = lambdField.getText();
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

		sim.INDIFQSETLAMBDA = setlambd;
		sim.INDIFQINFERLAMBDA = inferlambd;

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

}
