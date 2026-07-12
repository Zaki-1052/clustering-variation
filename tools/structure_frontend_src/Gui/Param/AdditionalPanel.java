package Gui.Param;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Obj.NProjObj;
import Obj.NSimObj;

public class AdditionalPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JCheckBox probBox, distBox, startBox, updateBox;
	private JCheckBox printqBox;
	private JButton ancestButton, defaultButton;
	private JTextField metroField;

	// Data Members
	private boolean updateflag;
	private boolean prob = true;
	private boolean dist;
	private boolean printq;
	private boolean start;
	private int metro = 10;

	private AncestDlg ancestDlg;

	// Data Model
	private NSimObj sim;

	public AdditionalPanel(NSimObj sim) {

		this.sim = sim;
		// make the controls

		updateBox = new JCheckBox(
				"Update allele frequencies using only individuals with POPFLAG=1 data");
		probBox = new JCheckBox(
				"Compute probability of the data (for estimating K)");
		distBox = new JCheckBox("Print credible regions  ");
		startBox = new JCheckBox("Initalize at POPINFO  ");
		printqBox = new JCheckBox("Print Q-hat  ");
		probBox.setSelected(true);

		CheckBoxListener listener = new CheckBoxListener();

		ancestButton = new JButton("Configure ...");
		ancestButton.setActionCommand("ancest");
		ancestButton.addActionListener(this);

		JPanel p2 = new JPanel();
		p2.add(ancestButton);
		ancestButton.setEnabled(false);

		defaultButton = new JButton("Default Setting");
		defaultButton.setActionCommand("default");
		defaultButton.addActionListener(this);

		JPanel buttonPane = new JPanel();
		buttonPane.add(defaultButton);
		JLabel l_prtq = new JLabel("              ");
		JLabel l_comp = new JLabel("             ");
		JLabel l_start = new JLabel("             ");
		JLabel l_metro = new JLabel("Freq. of Metropolis update for Q ");

		metroField = new JTextField(10);
		metroField.setText("10");
		JPanel p3 = new JPanel();
		p3.add(metroField);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridLayout(0, 1));

		leftPane.add(updateBox);
		leftPane.add(probBox);
		leftPane.add(distBox);
		leftPane.add(startBox);

		leftPane.add(l_metro);
		leftPane.add(printqBox);

		JPanel rightPane = new JPanel();
		rightPane.setLayout(new GridLayout(0, 1));
		rightPane.add(new JLabel("          "));
		rightPane.add(l_comp);
		rightPane.add(p2);
		rightPane.add(l_start);
		rightPane.add(p3);
		rightPane.add(l_prtq);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(leftPane, c);
		this.add(leftPane);
		c.gridx = 1;
		gridbag.setConstraints(rightPane, c);
		this.add(rightPane);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		gridbag.setConstraints(buttonPane, c);
		this.add(buttonPane);

		if (!sim.isNew()) {

			prob = sim.COMPUTEPROB;
			dist = sim.ANCESTDIST;

			start = sim.STARTPOPINFO;
			metro = sim.METROFREQ;
			updateflag = sim.PFROMPOPFLAGONLY;
			updateBox.setSelected(updateflag);
			printq = sim.PRINTQ;

			probBox.setSelected(prob);
			distBox.setSelected(dist);
			printqBox.setSelected(printq);
			startBox.setSelected(start);
			metroField.setText("" + metro);
			ancestButton.setEnabled(dist);

			if (dist) {
				ancestDlg = new AncestDlg(this, sim, false);
			}

		}

		NProjObj proj = sim.getProjObj();
		String pconf = proj.getConfig();
		if (pconf.indexOf("POPFLAG 0") >= 0) {
			updateBox.setEnabled(false);
		}

		proj = null;
		pconf = null;

		updateBox.addItemListener(listener);
		distBox.addItemListener(listener);
		startBox.addItemListener(listener);
		probBox.addItemListener(listener);
		printqBox.addItemListener(listener);
	}

	public void reset() {

		probBox.setSelected(true);
		prob = true;
		distBox.setSelected(false);
		updateBox.setSelected(false);
		updateflag = false;
		dist = false;
		startBox.setSelected(false);
		start = false;
		ancestButton.setEnabled(false);
		printq = false;
		printqBox.setSelected(false);
		metroField.setText("10");
		metro = 10;

	}

	public void actionPerformed(ActionEvent event) {

		if (event.getActionCommand().equals("default")) {
			reset();
			return;
		}

		if (event.getActionCommand().equals("ancest")) {
			if (ancestDlg == null) {
				ancestDlg = new AncestDlg(this, sim, true);
			}
			ancestDlg.showDialog();
			return;
		}

	}

	public boolean validateData() {
		String errString = "";

		String tmp = metroField.getText();
		try {
			metro = Integer.parseInt(tmp);
		} catch (NumberFormatException e) {
			errString += new String("\nMETROFREQ Must be an Integer");
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

		sim.METROFREQ = metro;

		sim.STARTPOPINFO = start;

		sim.PRINTQ = printq;

		sim.ANCESTDIST = dist;
		if (dist) {
			if (ancestDlg == null) {
				sim.NUMBOXES = 1000;
				sim.ANCESTPINT = 0.90f;
			} else {
				ancestDlg.updateData();
			}
		}

		sim.COMPUTEPROB = prob;
		sim.PFROMPOPFLAGONLY = updateflag;

	}

	class CheckBoxListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getItemSelectable();
			boolean val = true;

			if (e.getStateChange() == ItemEvent.DESELECTED) {
				val = false;
			}

			if (source == probBox) {
				prob = val;
			} else if (source == startBox) {
				start = val;
			} else if (source == distBox) {
				dist = val;
			} else if (source == printqBox) {
				printq = val;
			} else if (source == updateBox) {
				updateflag = val;
			}

			ancestButton.setEnabled(dist);

		}

	}

}
