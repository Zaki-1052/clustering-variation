package Gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Controller.PFGenManager;

public class PFileGenDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sSim; // selected simulation
	private int buttonHit;
	private JComboBox simBox;
	private JCheckBox seedBox;
	private JButton okButton, cancelButton, browseButton;
	private JTextField dirField, fromKField, toKField, seedField;
	private String saveDir;
	private int fromK;
	private int toK;
	private int seed;
	private PFGenManager manager;
	private boolean requireSeed;

	public PFileGenDlg(String[] list) {

		setTitle("Parameter file generator");
		setLocationRelativeTo(null);

		JPanel listPane = new JPanel();
		JLabel l1 = new JLabel("Select a parameter set  ");
		simBox = new JComboBox(list);
		simBox.setEditable(false);
		simBox.setPreferredSize(new Dimension(160, 20));
		simBox.setSelectedIndex(0);
		listPane.add(l1);
		listPane.add(simBox);

		JPanel kPane = new JPanel();
		JLabel l2 = new JLabel("Generate files for k from    ");
		fromKField = new JTextField(3);
		JLabel l3 = new JLabel(" to ");
		JLabel l5 = new JLabel("    ");
		toKField = new JTextField(3);
		kPane.add(l2);
		kPane.add(fromKField);
		kPane.add(l3);
		kPane.add(toKField);
		kPane.add(l5);

		seedBox = new JCheckBox(
				"Use sequential integer random seeds starting at  ");
		seedField = new JTextField(4);
		seedBox.setActionCommand("seed");
		seedBox.addActionListener(this);
		requireSeed = false;

		seedField.setEnabled(false);
		JPanel seedPane = new JPanel();
		seedPane.add(seedBox);
		seedPane.add(seedField);

		JPanel dirPane = new JPanel();
		JLabel l4 = new JLabel("Save in directory ");
		dirField = new JTextField(9);
		browseButton = new JButton("Browse...");
		browseButton.setActionCommand("browse");
		browseButton.addActionListener(this);
		dirPane.add(l4);
		dirPane.add(dirField);
		dirPane.add(browseButton);

		okButton = new JButton("Generate");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton(" Dismiss ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		JPanel packPane = new JPanel();
		packPane.setLayout(new GridLayout(0, 1));
		packPane.add(listPane);
		packPane.add(kPane);
		packPane.add(seedPane);
		packPane.add(dirPane);

		packPane.setPreferredSize(new Dimension(400, 150));
		buttonPane.setPreferredSize(new Dimension(400, 50));

		JPanel topPane = new JPanel();
		JLabel l6 = new JLabel(" ");
		topPane.add(l6);
		topPane.setPreferredSize(new Dimension(400, 30));

		getContentPane().add("North", topPane);
		getContentPane().add("Center", packPane);
		getContentPane().add("South", buttonPane);

	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	public void setManager(PFGenManager manager) {
		this.manager = manager;
	}

	private boolean validateInput() {

		sSim = (String) simBox.getSelectedItem();

		// test fromK
		String input = fromKField.getText();
		int input_data = 0;
		try {
			input_data = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "K must be an Integer",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (input_data <= 0) {
			JOptionPane.showMessageDialog(null, "K must be greater than 0",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		fromK = input_data;

		// test toK

		input = toKField.getText();
		input_data = 0;
		try {
			input_data = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "K must be an Integer",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (input_data <= 0) {
			JOptionPane.showMessageDialog(null, "K must be greater than 0",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (input_data < fromK) {
			JOptionPane.showMessageDialog(null, "Invalid Range for K: from "
					+ fromK + " to " + input_data, "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		toK = input_data;
		saveDir = dirField.getText();
		File testFile = new File(saveDir);
		if (!testFile.exists()) {
			JOptionPane.showMessageDialog(null,
					"Target directory does not exist", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (!testFile.canWrite()) {
			JOptionPane.showMessageDialog(null,
					"No write permission in target directory", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (requireSeed) {
			String sS = seedField.getText();
			try {
				seed = Integer.parseInt(sS);

			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null,
						"Random seed input is invalid", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;

	}

	public void actionPerformed(ActionEvent event) {

		String action = event.getActionCommand();
		if (action.equals("ok")) {
			if (validateInput()) {
				buttonHit = JOptionPane.OK_OPTION;
				manager.generateFile(sSim, fromK, toK, saveDir, requireSeed,
						seed);
				return;
			}
			return;

		}

		if (action.equals("cancel")) {
			buttonHit = JOptionPane.CANCEL_OPTION;
			setVisible(false);

			return;

		}
		if (action.equals("browse")) {
			final JFileChooser fc = new JFileChooser();
			fc.setApproveButtonText("Select");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				dirField.setText(file.getAbsolutePath());
			}
		}
		if (action.equals("seed")) {
			requireSeed = !requireSeed;
			seedField.setEnabled(requireSeed);
		}
	}

	public String getSaveDir() {
		return this.saveDir;
	}

	public int getFromK() {
		return fromK;
	}

	public int getToK() {
		return toK;
	}

	public boolean isSeedRequired() {
		return requireSeed;
	}

	public int getSeed() {
		return seed;
	}

	public String getSim() {
		return sSim;
	}

}
