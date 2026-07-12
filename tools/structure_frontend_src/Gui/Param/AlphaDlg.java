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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Obj.NSimObj;

public class AlphaDlg extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int buttonHit;

	private JTextField prioraField, priorbField, maxField, alphaField, sdField;
	private JCheckBox unifBox, popalphaBox;

	// Data Members
	private float priora = 0.05f;
	private float priorb = 0.001f;
	private float alpha = 1.0f;
	private float max = 10.0f;
	private float sd = 0.025f;

	private boolean unif = true;
	private boolean popalpha;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;

	// Data Model
	private NSimObj sim;

	public AlphaDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		// Create the labels.

		JLabel l1 = new JLabel("Initial ALPHA: ");
		JLabel l2 = new JLabel("ALPHAMAX:      ");
		JLabel l3 = new JLabel("ALPHAPRIORA:   ");
		JLabel l4 = new JLabel("ALPHAPRIORB:   ");
		JLabel l5 = new JLabel("ALPHAPROPSD:   ");

		// Create the text fields and set them up.
		prioraField = new JTextField(10);
		prioraField.setText("0.05");
		prioraField.setEnabled(false);

		priorbField = new JTextField(10);
		priorbField.setText("0.001");
		priorbField.setEnabled(false);

		maxField = new JTextField(10);
		maxField.setText("10.0");

		alphaField = new JTextField(10);
		alphaField.setText("1.0");

		sdField = new JTextField(10);
		sdField.setText("0.025");

		// Tell accessibility tools about label/textfield pairs.
		l1.setLabelFor(alphaField);
		l2.setLabelFor(maxField);
		l3.setLabelFor(prioraField);
		l4.setLabelFor(priorbField);
		l5.setLabelFor(sdField);

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

		unifBox = new JCheckBox("  Use a Uniform Prior for ALPHA");
		unifBox.setSelected(true);
		popalphaBox = new JCheckBox("  Separate Alpha for each Population");

		CheckBoxListener boxlistener = new CheckBoxListener();
		unifBox.addItemListener(boxlistener);
		popalphaBox.addItemListener(boxlistener);

		// Layout the labels in a panel.
		JPanel labelPane1 = new JPanel();
		labelPane1.setLayout(new GridLayout(0, 1));
		labelPane1.add(l1);

		JPanel labelPane2 = new JPanel();
		labelPane2.setLayout(new GridLayout(0, 1));
		labelPane2.add(l2);
		labelPane2.add(l5);
		labelPane2.add(l3);
		labelPane2.add(l4);

		// Layout the text fields in a panel.
		JPanel fieldPane1 = new JPanel();
		fieldPane1.setLayout(new GridLayout(0, 1));

		JPanel p1 = new JPanel();
		p1.add(alphaField);
		fieldPane1.add(p1);

		JPanel fieldPane2 = new JPanel();
		fieldPane2.setLayout(new GridLayout(0, 1));
		JPanel p2 = new JPanel();
		p2.add(maxField);
		JPanel p3 = new JPanel();
		p3.add(prioraField);
		JPanel p4 = new JPanel();
		p4.add(priorbField);
		JPanel p5 = new JPanel();
		p5.add(sdField);

		fieldPane2.add(p2);
		fieldPane2.add(p5);
		fieldPane2.add(p3);
		fieldPane2.add(p4);

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
		gridbag.setConstraints(labelPane1, c);
		contentPane.add(labelPane1);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		gridbag.setConstraints(fieldPane1, c);
		contentPane.add(fieldPane1);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 0, 0, 0);
		gridbag.setConstraints(popalphaBox, c);
		contentPane.add(popalphaBox);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		gridbag.setConstraints(unifBox, c);
		contentPane.add(unifBox);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		gridbag.setConstraints(labelPane2, c);
		contentPane.add(labelPane2);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		gridbag.setConstraints(fieldPane2, c);
		contentPane.add(fieldPane2);

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(25, 20, 20, 20);
		gridbag.setConstraints(buttonPane, c);
		contentPane.add(buttonPane);
		setContentPane(contentPane);

		setLocationRelativeTo(owner);

		if (!sim.isNew() && !def) {

			unif = sim.UNIFPRIORALPHA;
			popalpha = sim.POPALPHAS;
			priora = sim.ALPHAPRIORA;
			priorb = sim.ALPHAPRIORB;
			alpha = sim.ALPHA;
			max = sim.ALPHAMAX;
			sd = sim.ALPHAPROPSD;

			unifBox.setSelected(unif);
			popalphaBox.setSelected(popalpha);
			maxField.setText("" + max);
			maxField.setEnabled(unif);
			sdField.setText("" + sd);
			sdField.setEnabled(unif);
			prioraField.setText("" + priora);
			prioraField.setEnabled(!unif);
			priorbField.setText("" + priorb);
			priorbField.setEnabled(!unif);

		}

	}

	public void reset() {

		prioraField.setText("0.05");
		prioraField.setEnabled(false);
		priora = 0.05f;

		priorbField.setText("0.001");
		priorbField.setEnabled(false);
		priorb = 0.001f;

		maxField.setText("10.0");
		max = 10.0f;
		sdField.setText("0.025");
		sd = 0.025f;

		alphaField.setText("1.0");
		alpha = 1.0f;

		unifBox.setSelected(true);
		unif = true;

		popalpha = false;
		popalphaBox.setSelected(popalpha);
	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;
	}

	public boolean validateData() {
		String errString = "";

		String tmp = alphaField.getText();
		try {
			alpha = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\n\"Initial Alpha\" Must be a Real Number");
		}

		if (unif) { // check the max and sd

			tmp = maxField.getText();

			try {
				max = Float.parseFloat(tmp);
			} catch (NumberFormatException e) {
				errString += new String("\n\"ALPHAMAX\" Must be a Real Number");
			}

			tmp = sdField.getText();

			try {
				sd = Float.parseFloat(tmp);
			} catch (NumberFormatException e) {
				errString += new String(
						"\n\"ALPHAPROPSD\" Must be a Real Number");
			}
		} else {

			tmp = prioraField.getText();

			try {
				priora = Float.parseFloat(tmp);
			} catch (NumberFormatException e) {
				errString += new String(
						"\n\"ALPHAPRIORA\" Must be a Real Number");
			}

			tmp = priorbField.getText();

			try {
				priorb = Float.parseFloat(tmp);
			} catch (NumberFormatException e) {
				errString += new String(
						"\n\"ALPHAPRIORB\" Must be a Real Number");
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
		sim.ALPHA = alpha;
		sim.POPALPHAS = popalpha;
		sim.UNIFPRIORALPHA = unif;
		// if(unif){
		sim.ALPHAMAX = max;
		sim.ALPHAPROPSD = sd;
		// }else{
		sim.ALPHAPRIORA = priora;
		sim.ALPHAPRIORB = priorb;
		// }
	}

	class CheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getItemSelectable();
			boolean val = true;

			if (e.getStateChange() == ItemEvent.DESELECTED) {
				val = false;
			}

			if (source == unifBox) {
				unif = val;
			} else if (source == popalphaBox) {
				popalpha = val;
			}

			maxField.setEnabled(unif);
			sdField.setEnabled(unif);
			prioraField.setEnabled(!unif);
			priorbField.setEnabled(!unif);
		}
	}

}
