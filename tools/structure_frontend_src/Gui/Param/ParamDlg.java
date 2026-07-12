package Gui.Param;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import Obj.NProjObj;
import Obj.NSimObj;
import Obj.ObjIO;

public class ParamDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int buttonHit;

	MainPanel mainPane;
	AncestryPanel ancestPane;
	FrequencyPanel freqPane;
	AdditionalPanel additionalPane;

	NSimObj sim;

	public ParamDlg(NSimObj sim) {

		this.sim = sim;
		JPanel con = new JPanel();
		con.setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();

		mainPane = new MainPanel(sim);
		ancestPane = new AncestryPanel(sim);
		freqPane = new FrequencyPanel(sim);
		additionalPane = new AdditionalPanel(sim);

		tabbedPane.addTab("Run Length", mainPane);
		tabbedPane.addTab("Ancestry Model", ancestPane);
		tabbedPane.addTab("Allele Frequency Model", freqPane);
		tabbedPane.addTab("Advanced", additionalPane);

		tabbedPane.setSelectedIndex(0);
		con.add(tabbedPane, BorderLayout.CENTER);

		JButton okButton = new JButton("  OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);

		// nextButton.setActionCommand("w1_next");
		// nextButton.addActionListener(controller);
		JButton cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(BorderLayout.EAST, buttonPanel);
		bottomPanel.setPreferredSize(new Dimension(400, 30));

		con.add(bottomPanel, BorderLayout.SOUTH);
		con.setPreferredSize(new Dimension(550, 400));
		getContentPane().add(con);

		if (sim.isNew()) {
			setTitle("New Parameter Set");
		} else {
			setTitle(sim.getSimName() + " Parameters");
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				buttonHit = JOptionPane.CANCEL_OPTION;
				setVisible(false);
			}
		});
	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	public NSimObj getSimObj() {
		return sim;
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("ok")) {

			if (mainPane.validateData()) {
				mainPane.updateData();
			} else {
				return;
			}
			if (additionalPane.validateData()) {
				additionalPane.updateData();
			} else {
				return;
			}

			ancestPane.updateData();
			freqPane.updateData();

			if (!sim.USEDEFAULTADMBURNIN && sim.ADMBURNIN > sim.BURNIN) {
				JOptionPane.showMessageDialog(this,
						"The length of admixture burnin should be \n"
								+ "less than burnin length\n", "errors",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// handle data model
			// let sim object create the simulation
			if (sim.isNew()) {
				if (sim.createSimSpace()) {
					// sim.writeSimFile(); -- we change the sim file format
					// since final release of version 2

					// new feature
					if (sim.LOCPRIOR) {
						NProjObj proj = sim.getProjObj();
						if (!proj.getLocData()) {
							if (proj.getPopId()) {
								JOptionPane
										.showMessageDialog(
												this,
												"Input data contains no sampling location information,  LOCPRIOR model will use population IDs",
												"Sampling Location Information",
												JOptionPane.WARNING_MESSAGE);
							}
							if (!proj.getPopId()) {
								JOptionPane
										.showMessageDialog(
												this,
												"No sampling location information can be found in the data, structure can not apply LOCPRIOR model",
												"Sampling Location Information",
												JOptionPane.ERROR_MESSAGE);
								sim.LOCPRIOR = false;
							}
						}

					}

					ObjIO.writeSimFile(sim);
					buttonHit = JOptionPane.OK_OPTION;
				} else {
					buttonHit = JOptionPane.CANCEL_OPTION;
				}

				setVisible(false);
				return;

			}

			NSimObj oldSim = ObjIO.loadSimObj(sim.getProjObj(), sim
					.getSimName());

			// new feature
			if (sim.LOCPRIOR && !oldSim.LOCPRIOR) {
				NProjObj proj = sim.getProjObj();
				if (!proj.getLocData()) {
					if (proj.getPopId()) {
						JOptionPane
								.showMessageDialog(
										this,
										"Input data contains no sampling location information, LOCPRIOR model will use population IDs",
										"Sampling Location Information",
										JOptionPane.WARNING_MESSAGE);
					}
					if (!proj.getPopId()) {
						JOptionPane
								.showMessageDialog(
										this,
										"No sampling location information can be found in the data, structure can not apply LOCPRIOR model",
										"Sampling Location Information",
										JOptionPane.ERROR_MESSAGE);
						sim.LOCPRIOR = false;
					}
				}

			}

			if (sim.equals(oldSim)) {
				oldSim = null;
				buttonHit = JOptionPane.OK_OPTION;
				setVisible(false);
				return;
			}

			Object[] options = { "Save as a new parameter set  ",
					"   Ignore   the   changes   " };

			int n = JOptionPane.showOptionDialog(this,
					"Simulation parameters have been modified!",
					"Simulation Parameter Settings", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == JOptionPane.YES_OPTION) {

				if (sim.createSimSpace()) {
					// sim.writeSimFile(); -- we change the sim file format
					// since final release of version 2
					ObjIO.writeSimFile(sim);
					buttonHit = JOptionPane.NO_OPTION;
				} else {
					buttonHit = JOptionPane.CANCEL_OPTION;
					sim = oldSim;
				}
				setVisible(false);
				return;
			}

			if (n == JOptionPane.NO_OPTION) {
				sim = oldSim;
				buttonHit = JOptionPane.CANCEL_OPTION;
				setVisible(false);
				return;
			}

		}

		if (event.getActionCommand().equals("cancel")) {
			buttonHit = JOptionPane.CANCEL_OPTION;
			setVisible(false);
		}
	}

}
