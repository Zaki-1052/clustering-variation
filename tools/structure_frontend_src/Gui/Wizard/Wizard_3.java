package Gui.Wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Wizard_3 extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JCheckBox b1, b2, b3, b4, b5;

	private WizardController controller;

	boolean useLocusName, useGeneName, usePhaseInfo, oneRow, recessiveAllele;
	private JButton dataButton;
	@SuppressWarnings("unused")
	private String ploidy;

	public Wizard_3(Frame parent, WizardController controller) {
		super(parent, "Step 3 of 4 - Project Wizard", true);

		this.controller = controller;

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		buttonHit = JOptionPane.CLOSED_OPTION;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JLabel dlglabel = new JLabel(
				"       Step 3 of 4: Format of input data set ");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add("Center", dlglabel);
		topPanel.setPreferredSize(new Dimension(500, 25));

		JLabel label = new JLabel(
				"Please check box if data file contains following row(s):");
		//
		// Make the format option check list
		//

		// Create the check boxes

		b1 = new JCheckBox("Row of marker names");
		b1.setSelected(false);

		b5 = new JCheckBox("Row of recessive alleles");
		b5.setSelected(false);

		b2 = new JCheckBox("Map distances between loci");
		b2.setSelected(false);

		b3 = new JCheckBox("Phase information");
		b3.setSelected(false);

		JLabel el = new JLabel("                              ");
		JLabel label2 = new JLabel("                      Special format ");
		b4 = new JCheckBox(
				"Data file stores data for individuals in a single line");
		b4.setSelected(false);

		// Register a listener for the check boxes.
		CheckBoxListener listener = new CheckBoxListener();
		b1.addItemListener(listener);
		b2.addItemListener(listener);
		b3.addItemListener(listener);
		b4.addItemListener(listener);
		b5.addItemListener(listener);

		JPanel checkPanel = new JPanel();
		checkPanel.setLayout(new GridLayout(0, 1));
		checkPanel.add(label);
		checkPanel.add(b1);
		checkPanel.add(b5);
		checkPanel.add(b2);
		checkPanel.add(b3);
		checkPanel.add(el);
		checkPanel.add(label2);
		checkPanel.add(b4);

		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 20, 0, 0);
		gridbag.setConstraints(checkPanel, c);
		formatPanel.add(checkPanel);
		formatPanel.setPreferredSize(new Dimension(500, 200));

		JButton prevButton = new JButton("<<Back");
		prevButton.setActionCommand("w3_back");
		prevButton.addActionListener(controller);
		JButton nextButton = new JButton("Next>>");
		nextButton.setActionCommand("w3_next");
		nextButton.addActionListener(controller);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("w3_cancel");
		cancelButton.addActionListener(controller);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(cancelButton);

		dataButton = new JButton("Show data file format");
		dataButton.setActionCommand("datainfo");
		dataButton.addActionListener(controller);
		JPanel pd = new JPanel();
		pd.add(dataButton);
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BorderLayout());
		dataPanel.add(pd, BorderLayout.CENTER);
		dataPanel.setPreferredSize(new Dimension(500, 50));

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(buttonPanel);
		bottomPanel.setPreferredSize(new Dimension(500, 75));

		getContentPane().setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(topPanel, c);
		getContentPane().add(topPanel);

		c.gridx = 0;
		c.gridy = 1;
		gridbag.setConstraints(formatPanel, c);
		getContentPane().add(formatPanel);

		c.gridy = 2;
		gridbag.setConstraints(dataPanel, c);
		getContentPane().add(dataPanel);

		c.gridy = 3;
		gridbag.setConstraints(bottomPanel, c);
		getContentPane().add(bottomPanel);
		addWindowListener(controller);

	}

	class CheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getItemSelectable();
			boolean val = true;

			if (e.getStateChange() == ItemEvent.DESELECTED) {
				val = false;
			}

			if (source == b1) {
				useGeneName = val;
			} else if (source == b2) {
				useLocusName = val;
			} else if (source == b3) {
				usePhaseInfo = val;
			} else if (source == b4) {
				oneRow = val;
			} else if (source == b5) {
				recessiveAllele = val;
			}

		}
	}

	public int showDialog(String ploidy) {

		if (controller.getDataFile() == null) {
			dataButton.setEnabled(false);
		} else {
			dataButton.setEnabled(true);
		}

		if (ploidy != null) {
			this.ploidy = ploidy;
			if (!ploidy.equals("2")) {
				b3.setEnabled(false);
			} else {
				b3.setEnabled(true);
			}
		}
		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;

	}

	public boolean getGeneName() {
		return useGeneName;
	}

	public boolean getLocusName() {
		return useLocusName;
	}

	public boolean getPhaseInfo() {
		return usePhaseInfo;
	}

	public boolean getOneRow() {
		return oneRow;
	}

	public boolean getRecessiveAllele() {
		return recessiveAllele;
	}

}
