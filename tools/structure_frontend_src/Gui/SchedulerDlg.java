package Gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class SchedulerDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] list;
	private int buttonHit;
	private JList simList;
	private JButton okButton, cancelButton;
	private JTextField iterField, fromKField, toKField, seedField;
	private JCheckBox seedBox;
	private int iteration;
	private int fromK;
	private int toK;
	private boolean requireSeed;
	private int seed;
	public SchedulerDlg(String[] list) {

		setTitle("Structure Scheduler");
		setLocationRelativeTo(null);
		this.list = list;
		simList = new JList(list);
		// use the default selection model
		simList.setToolTipText("Hold \'Ctrl\' to select multiple simulations");

		JScrollPane listPane = new JScrollPane(simList);
		listPane.setPreferredSize(new Dimension(300, 150));
		JPanel listp = new JPanel();
		listp.add(listPane, BorderLayout.CENTER);
		JLabel topl = new JLabel("Select Simulations to Run");
		JPanel topp = new JPanel();
		topp.add(topl);
		topp.setPreferredSize(new Dimension(400, 20));

		JPanel upPane = new JPanel();
		upPane.setLayout(new BorderLayout());
		upPane.add(topp, BorderLayout.NORTH);
		upPane.add(listp, BorderLayout.CENTER);

		JLabel l1 = new JLabel("Number of Iterations: ");
		iterField = new JTextField(3);
		iterField.setText("1");
		iteration = 1;
		JPanel itPane = new JPanel();
		itPane.add(l1);
		itPane.add(iterField);

		JLabel l2 = new JLabel("Set K from ");
		fromKField = new JTextField(3);
		JLabel l3 = new JLabel(" to ");
		toKField = new JTextField(3);

		JPanel kPane = new JPanel();
		kPane.add(l2);
		kPane.add(fromKField);
		kPane.add(l3);
		kPane.add(toKField);

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

		JPanel midPane = new JPanel();
		midPane.setLayout(new GridLayout(0, 1));
		midPane.add(kPane);
		midPane.add(seedPane);
		midPane.add(itPane);

		// Buttons

		okButton = new JButton("Start ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		midPane.setPreferredSize(new Dimension(400, 90));
		buttonPane.setPreferredSize(new Dimension(400, 50));

		getContentPane().add("North", upPane);
		getContentPane().add("Center", midPane);
		getContentPane().add("South", buttonPane);
	}

	private boolean validateInput() {

		int[] indices = simList.getSelectedIndices();
		if (indices == null || indices.length == 0) {
			JOptionPane.showMessageDialog(null,
					"Please select at least one simulation to run", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		String input = iterField.getText();
		int input_data = 0;
		try {
			input_data = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null,
					"Interation number must be an Integer", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (input_data <= 0) {
			JOptionPane.showMessageDialog(null,
					"Interation number must be greater than 0", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		iteration = input_data;

		// test fromK
		input = fromKField.getText();
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

	public int showDialog() {
		setModal(true);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	public void actionPerformed(ActionEvent event) {

		String action = event.getActionCommand();
		if (action.equals("ok")) {
			if (validateInput()) {
				buttonHit = JOptionPane.OK_OPTION;
				setVisible(false);
				return;
			}
			return;

		}

		if (action.equals("cancel")) {
			buttonHit = JOptionPane.CANCEL_OPTION;
			setVisible(false);
			return;

		}

		if (action.equals("seed")) {
			requireSeed = !requireSeed;
			seedField.setEnabled(requireSeed);
		}

	}

	public int getIterationNum() {
		return this.iteration;
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

	public String[] getSelectedList() {

		int[] indices = simList.getSelectedIndices();
		if (indices == null || indices.length == 0) {
			return null;
		}

		String[] rst = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			rst[i] = list[indices[i]];

		}
		return rst;
	}

}
