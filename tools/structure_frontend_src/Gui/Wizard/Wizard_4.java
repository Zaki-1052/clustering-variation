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
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class Wizard_4 extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JCheckBox b1, b2, b3, b4, b5, b6;
	private JTextField numfield;
	private WizardController controller;

	private JButton dataButton;

	boolean usePopId, useIndLabel, useExtraCol, usePopFlag, usePheno,
			useLocData;

	public Wizard_4(Frame parent, WizardController controller) {
		super(parent, "Step 4 of 4 - Project Wizard", true);

		this.controller = controller;

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		buttonHit = JOptionPane.CLOSED_OPTION;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JLabel dlglabel = new JLabel(
				"       Step 4 of 4: Format of input data set (cont'd)");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add("Center", dlglabel);
		topPanel.setPreferredSize(new Dimension(500, 25));

		JLabel label = new JLabel(
				"Please check box if data file contains following column(s):");
		//
		// Make the format option check list
		//

		// Create the check boxes
		b1 = new JCheckBox("Individual ID for each individual");
		b1.setSelected(false);

		b2 = new JCheckBox("Putative population origin for each individual");
		b2.setSelected(false);

		b3 = new JCheckBox("USEPOPINFO selection flag");
		b3.setSelected(false);

		b6 = new JCheckBox("Sampling location information");
		b6.setSelected(false);

		b5 = new JCheckBox("Phenotype information ");
		b5.setSelected(false);

		b4 = new JCheckBox("Other extra columns");
		b4.setSelected(false);

		// Register a listener for the check boxes.
		CheckBoxListener listener = new CheckBoxListener();
		b1.addItemListener(listener);
		b2.addItemListener(listener);
		b3.addItemListener(listener);
		b4.addItemListener(listener);
		b5.addItemListener(listener);
		b6.addItemListener(listener);

		numfield = new JTextField(3);
		numfield.setEnabled(false);
		JLabel colLabel = new JLabel("Number of Extra Columns: ");
		JPanel numPanel = new JPanel();
		// numPanel.setLayout(new FlowLayout());
		numPanel.add(colLabel);
		numPanel.add(numfield);

		JPanel checkPanel = new JPanel();
		checkPanel.setLayout(new GridLayout(0, 1));
		checkPanel.add(label);
		checkPanel.add(b1);
		checkPanel.add(b2);
		checkPanel.add(b3);
		checkPanel.add(b6);
		checkPanel.add(b5);
		checkPanel.add(b4);
		checkPanel.add(numPanel);

		checkPanel.setPreferredSize(new Dimension(500, 240));
		checkPanel.setMaximumSize(new Dimension(500, 240));

		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 20, 0, 0);
		gridbag.setConstraints(checkPanel, c);
		formatPanel.add(checkPanel);
		formatPanel.setPreferredSize(new Dimension(500, 240));

		dataButton = new JButton("Show data file format");
		dataButton.setActionCommand("datainfo");
		dataButton.addActionListener(controller);
		JPanel pd = new JPanel();
		pd.add(dataButton);
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BorderLayout());
		dataPanel.add(pd, BorderLayout.CENTER);
		dataPanel.setPreferredSize(new Dimension(500, 50));

		JButton prevButton = new JButton("<<Back");
		prevButton.setActionCommand("w4_back");
		prevButton.addActionListener(controller);
		JButton finButton = new JButton("Finish");
		finButton.setActionCommand("finish");
		finButton.addActionListener(controller);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("w4_cancel");
		cancelButton.addActionListener(controller);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));
		buttonPanel.add(prevButton);
		buttonPanel.add(finButton);
		buttonPanel.add(cancelButton);

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
				useIndLabel = val;
			} else if (source == b2) {
				usePopId = val;
			} else if (source == b4) {
				useExtraCol = val;
				// TODO: if val is true enable the text field
			} else if (source == b3) {
				usePopFlag = val;
			} else if (source == b5) {
				usePheno = val;
			} else if (source == b6) {
				useLocData = val;
			}

			numfield.setEnabled(useExtraCol);
			if (!useExtraCol) {
				numfield.setText("");
			}
		}
	}

	public int showDialog() {

		if (controller.getDataFile() == null) {
			dataButton.setEnabled(false);
		} else {
			dataButton.setEnabled(true);
		}

		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;

	}

	public boolean getIndLabel() {
		return useIndLabel;
	}

	public boolean getPopId() {
		return usePopId;
	}

	public boolean getPopFlag() {
		return usePopFlag;
	}

	public boolean getPheno() {
		return usePheno;
	}

	public boolean getLocData() {
		return useLocData;
	}

	public String getExtraCols() {
		if (useExtraCol == false || numfield.getText() == null) {
			return null;
		} else {
			// TODO: string transfer ...
			return numfield.getText();
		}
	}

}
