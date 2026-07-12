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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Obj.NSimObj;

public class AdmixDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int buttonHit;

	private JCheckBox alphaBox;
	private JButton alphaButton;

	private boolean infer = true;

	// Buttons
	private JButton okButton;
	private JButton cancelButton;
	private JButton defaultButton;

	private AlphaDlg alphaDlg;
	private AlphaInputDlg alphaInputDlg;

	// Data Model
	private NSimObj sim;

	public AdmixDlg(Component owner, NSimObj sim, boolean def) {

		this.sim = sim;

		// Create the labels.

		alphaBox = new JCheckBox("Infer ALPHA ");
		alphaBox.setSelected(true);

		CheckBoxListener cl = new CheckBoxListener();
		alphaBox.addItemListener(cl);

		alphaButton = new JButton("Configure ...");
		alphaButton.setActionCommand("alpha");
		alphaButton.addActionListener(this);

		JPanel p0 = new JPanel();
		p0.add(alphaButton);

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getActionCommand().equals("Default")) {
					reset();
					return;
				}

				// disappear the current dialog box
				setVisible(false);
				if (event.getActionCommand().equals("ok")) {
					buttonHit = JOptionPane.OK_OPTION;
				}
				if (event.getActionCommand().equals("cancel")) {
					buttonHit = JOptionPane.CANCEL_OPTION;
					reset();
				}

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
		labelPane.add(alphaBox);

		// Layout the text fields in a panel.
		JPanel fieldPane = new JPanel();
		fieldPane.setLayout(new GridLayout(0, 1));
		fieldPane.add(p0);

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
			infer = sim.INFERALPHA;
			alphaBox.setSelected(infer);
			if (infer) {
				alphaDlg = new AlphaDlg(this, sim, false);
			} else {
				alphaInputDlg = new AlphaInputDlg(this, sim, false);
			}
		}

	}

	public void reset() {
		infer = true;
		alphaBox.setSelected(true);
	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;
	}

	public void actionPerformed(ActionEvent event) {

		if (event.getActionCommand().equals("alpha")) {
			if (infer) {
				if (alphaDlg == null) {
					alphaDlg = new AlphaDlg(this, sim, true);
				}

				alphaDlg.showDialog();
			} else {
				if (alphaInputDlg == null) {
					alphaInputDlg = new AlphaInputDlg(this, sim, true);
				}

				alphaInputDlg.showDialog();
			}
		}
	}

	public void updateData() {

		sim.INFERALPHA = infer;
		if (infer) {

			if (alphaDlg == null) { // give the defaults
				sim.ALPHA = 1.0f;
				sim.POPALPHAS = false;
				sim.UNIFPRIORALPHA = true;
				sim.ALPHAMAX = 10.0f;
				sim.ALPHAPROPSD = 0.025f;
			} else {
				alphaDlg.updateData();
			}

		}

		else {
			if (alphaInputDlg == null) {
				sim.ADMBURNIN = 1;
			} else {
				alphaInputDlg.updateData();
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

			if (source == alphaBox) {
				infer = val;
			}
		}

	}

}
