package Gui.Param;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Obj.NSimObj;

public class LambdInputDlg extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JTextField lambdField;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;

	// Data
	private float lambd = 1.0f;

	// Data Model
	private NSimObj sim;

	public LambdInputDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		JLabel l1 = new JLabel("Set  LAMBDA:");

		lambdField = new JTextField(10);
		lambdField.setText("1.0");
		JPanel p1 = new JPanel();
		p1.add(lambdField);

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getActionCommand().equals("Default")) {
					reset();
					return;
				}

				if (event.getActionCommand().equals("ok")) {
					if (!validateData()) {
						return;
					}
					buttonHit = JOptionPane.OK_OPTION;
				}
				if (event.getActionCommand().equals("cancel")) {
					buttonHit = JOptionPane.CANCEL_OPTION;
					reset();
				}

				// disappear the current dialog box
				setVisible(false);

			}
		};

		// Create Buttons
		okButton = new JButton("   OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(listener);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(listener);
		defaultButton = new JButton("Default");
		defaultButton.addActionListener(listener);

		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(l1);

		// Layout the text fields in a panel.
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));

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
		c.insets = new Insets(20, 0, 0, 0);

		gridbag.setConstraints(labelPane, c);
		contentPane.add(labelPane);

		c.gridx = 1;
		gridbag.setConstraints(fieldPane, c);
		contentPane.add(fieldPane);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(25, 20, 20, 20);
		gridbag.setConstraints(buttonPane, c);
		contentPane.add(buttonPane);
		setContentPane(contentPane);
		setLocationRelativeTo(owner);

		if (!sim.isNew() && !def) {
			lambd = sim.LAMBDA;
			lambdField.setText("" + lambd);
		}

	}

	public void reset() {
		lambdField.setText("1.0");
		lambd = 1.0f;
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

		String tmp = lambdField.getText();
		try {
			lambd = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\nLAMBDA Must be a Real Number");
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
		sim.LAMBDA = lambd;
	}

}
