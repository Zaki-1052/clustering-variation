package Gui.Wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class Wizard_2 extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JTextField numInds, numLoci, ploidy, missingVal;
	private WizardController controller;
	private JButton dataButton;

	public Wizard_2(Frame parent, WizardController controller) {

		super(parent, "Step 2 of 4 - Project Wizard", true);
		this.controller = controller;

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		buttonHit = JOptionPane.CLOSED_OPTION;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JLabel dlglabel = new JLabel(
				"       Step 2 of 4: Information of input data set ");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add("Center", dlglabel);
		topPanel.setPreferredSize(new Dimension(500, 25));

		JLabel l1 = new JLabel(" Number of individuals: ");
		JLabel l2 = new JLabel(" Ploidy of data:        ");
		JLabel l3 = new JLabel(" Number of loci:        ");
		JLabel l4 = new JLabel(" Missing data value:    ");

		numInds = new JTextField(4);
		JPanel pt1 = new JPanel();
		pt1.add(numInds);

		ploidy = new JTextField(4);
		JPanel pt2 = new JPanel();
		pt2.add(ploidy);
		ploidy.setText("2");

		numLoci = new JTextField(4);
		JPanel pt3 = new JPanel();
		pt3.add(numLoci);

		missingVal = new JTextField(4);
		JPanel pt4 = new JPanel();
		pt4.add(missingVal);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		p1.add(l1);
		p1.add(l2);
		p1.add(l3);
		p1.add(l4);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		p2.add(pt1);
		p2.add(pt2);
		p2.add(pt3);
		p2.add(pt4);

		JPanel p3 = new JPanel();
		JLabel l5 = new JLabel("                        ");
		p3.add(l5);

		JPanel paraPanel = new JPanel();

		paraPanel.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(p1, c);
		paraPanel.add(p1);
		c.gridx = 1;
		gridbag.setConstraints(p2, c);
		paraPanel.add(p2);

		paraPanel.setPreferredSize(new Dimension(500, 175));

		dataButton = new JButton("Show data file format");
		dataButton.setActionCommand("datainfo");
		dataButton.addActionListener(controller);

		JPanel pd = new JPanel();
		pd.add(dataButton);
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BorderLayout());
		dataPanel.add(pd, BorderLayout.CENTER);

		JButton prevButton = new JButton("<<Back");
		prevButton.setActionCommand("w2_back");
		prevButton.addActionListener(controller);
		JButton nextButton = new JButton("Next>>");
		nextButton.setActionCommand("w2_next");
		nextButton.addActionListener(controller);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("w2_cancel");
		cancelButton.addActionListener(controller);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(cancelButton);

		JPanel bottomPanel = new JPanel();
		// bottomPanel.setLayout();

		bottomPanel.add(buttonPanel);
		bottomPanel.setPreferredSize(new Dimension(500, 75));

		dataPanel.setPreferredSize(new Dimension(500, 75));

		getContentPane().setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(topPanel, c);
		getContentPane().add(topPanel);

		c.gridx = 0;
		c.gridy = 1;
		gridbag.setConstraints(paraPanel, c);
		getContentPane().add(paraPanel);

		c.gridy = 2;
		gridbag.setConstraints(dataPanel, c);
		getContentPane().add(dataPanel);

		c.gridy = 3;
		gridbag.setConstraints(bottomPanel, c);
		getContentPane().add(bottomPanel);

		addWindowListener(controller);
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

	public String getNumInds() {
		return numInds.getText();
	}

	public String getNumLoci() {
		return numLoci.getText();
	}

	public String getPloidy() {
		return ploidy.getText();
	}

	public String getMissingVal() {
		return missingVal.getText();
	}

}
