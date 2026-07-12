package Gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StartRunDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JTextField inputArea;
	private JButton okButton;
	private JButton seedButton;
	private JButton cancelButton;

	public int seed;
	public int K;
	public int seed_flag = 0;

	public StartRunDlg() {
		setTitle("Run Structure Simulation");
		setLocationRelativeTo(null);

		inputArea = new JTextField(5);
		JLabel title = new JLabel("       Set number of populations assumed  ");
		JPanel sp = new JPanel();
		sp.add(title);
		sp.add(inputArea);

		okButton = new JButton("        OK        ");
		seedButton = new JButton("Set random seed ...");
		cancelButton = new JButton("       Cancel      ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		seedButton.setActionCommand("seed");
		seedButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		JPanel buttonPane = new JPanel();

		buttonPane.add(okButton);
		buttonPane.add(seedButton);
		buttonPane.add(cancelButton);
		buttonPane.setPreferredSize(new Dimension(350, 50));
		getContentPane().add("North", new JLabel("           "));
		getContentPane().add("Center", sp);
		getContentPane().add("South", buttonPane);
	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	private boolean validateInput() {

		String sK = inputArea.getText();
		if (sK == null) {
			return false;
		}
		try {
			K = Integer.parseInt(sK);
		} catch (NumberFormatException e) {
			return false;
		}
		if (K <= 0) {
			return false;
		}

		return true;
	}

	public void actionPerformed(ActionEvent event) {

		String action = event.getActionCommand();
		if (action.equals("ok")) {
			if (!validateInput()) {
				JOptionPane.showMessageDialog(null,
						"The input for K is invalid", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			buttonHit = 1;
			setVisible(false);
			return;
		}
		if (action.equals("seed")) {
			while (true) {
				String sS = JOptionPane
						.showInputDialog("Set user defined random seed");
				if (sS == null) {
					return;
				}
				try {
					seed = Integer.parseInt(sS);
					break;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
							"Current input is not a valid integer random seed",
							"Error", JOptionPane.ERROR_MESSAGE);
					continue;
				}
			}
			seed_flag = 1;
		}

		if (action.equals("cancel")) {
			buttonHit = -1;
			setVisible(false);
		}
	}
}
