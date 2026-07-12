package Gui.Param;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Obj.NSimObj;

public class MainPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private JTextField maxpopsField;
	private JTextField burninField;
	private JTextField numrepsField;

	// The data members

	// private int maxpops;
	private int burnin;
	private int numreps;

	// Data Model
	private NSimObj sim;

	public MainPanel(NSimObj sim) {

		this.sim = sim;

		// JLabel l1 = new JLabel("   Number of Populations Assumed (K):   ");
		JLabel l2 = new JLabel("   Length of Burnin Period:             ");
		JLabel l3 = new JLabel("   Number of MCMC Reps after Burnin :   ");

		// maxpopsField = new JTextField(5);
		burninField = new JTextField(5);
		numrepsField = new JTextField(5);

		// JPanel pt1 = new JPanel();
		// pt1.add(maxpopsField);
		JPanel pt2 = new JPanel();
		pt2.add(burninField);
		JPanel pt3 = new JPanel();
		pt3.add(numrepsField);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		// p1.add(l1);
		p1.add(l2);
		p1.add(l3);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		// p2.add(pt1);
		p2.add(pt2);
		p2.add(pt3);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(p1, c);
		this.add(p1);
		c.gridx = 1;
		gridbag.setConstraints(p2, c);
		this.add(p2);
		setPreferredSize(new Dimension(200, 150));

		if (!sim.isNew()) {

			// maxpops = sim.MAXPOPS;
			burnin = sim.BURNIN;
			numreps = sim.NUMREPS;

			// maxpopsField.setText(""+sim.MAXPOPS);
			burninField.setText("" + sim.BURNIN);
			numrepsField.setText("" + sim.NUMREPS);

		}

	}

	public boolean validateData() {

		String errString = "";
		/*
		 * String tmp = maxpopsField.getText(); try { maxpops =
		 * Integer.parseInt(tmp); }catch (NumberFormatException e){ errString +=
		 * new String("\n\"Number of Populations\" Must be an Integer    "); }
		 */
		String tmp = burninField.getText();
		try {
			burnin = Integer.parseInt(tmp);
		} catch (NumberFormatException e) {
			errString += new String(
					"\n\"Length of Burnin\" Must be an Integer    ");
		}

		tmp = numrepsField.getText();
		try {
			numreps = Integer.parseInt(tmp);
		} catch (NumberFormatException e) {
			errString += new String(
					"\n\"Number of MCMC Reps\" Must be an Integer    ");
		}

		if (errString.compareTo("") != 0) {
			String prefix = "Errors:\n\n";
			errString = prefix.concat(errString);
			errString += new String("\n\n");
			JOptionPane.showMessageDialog(this, errString, "errors",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public void updateData() {

		sim.BURNIN = burnin;
		sim.NUMREPS = numreps;
	}

}
