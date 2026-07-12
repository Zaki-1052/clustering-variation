package Gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RemoveSimDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JList all, select;
	private JButton leftButton, rightButton, okButton, cancelButton;
	private DefaultListModel allModel, selectModel;

	public RemoveSimDlg(String[] list) {
		setTitle("Remove Simulation");
		allModel = new DefaultListModel();
		for (int i = 0; i < list.length; i++) {
			allModel.addElement(list[i]);
		}
		all = new JList(allModel);
		// use the default selection model

		JScrollPane leftPane = new JScrollPane(all);
		leftPane.setPreferredSize(new Dimension(180, 250));

		selectModel = new DefaultListModel();
		select = new JList(selectModel);
		JScrollPane rightPane = new JScrollPane(select);
		rightPane.setPreferredSize(new Dimension(180, 250));

		leftButton = new JButton(new ImageIcon("images/left.gif"));
		rightButton = new JButton(new ImageIcon("images/right.gif"));
		leftButton.setActionCommand("left");
		JPanel b1 = new JPanel();
		b1.add(leftButton);
		rightButton.setActionCommand("right");
		leftButton.addActionListener(this);
		rightButton.addActionListener(this);
		JPanel b2 = new JPanel();
		b2.add(rightButton);

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		p1.add(b1);
		p1.add(b2);
		JPanel p2 = new JPanel();
		p2.add("Center", p1);
		p2.setPreferredSize(new Dimension(40, 250));

		JPanel upPane = new JPanel();
		upPane.add(leftPane);
		upPane.add(p1);
		upPane.add(rightPane);

		// Buttons

		okButton = new JButton("   OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		JLabel top = new JLabel(
				" All Simulations                      Simulations to be Removed ");
		JPanel topPane = new JPanel();
		topPane.add(top);
		getContentPane().add("North", topPane);
		getContentPane().add("Center", upPane);
		getContentPane().add("South", buttonPane);
	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		if (action.equals("right")) {
			int[] items = all.getSelectedIndices();
			for (int i = 0; i < items.length; i++) {
				selectModel.addElement(allModel.elementAt(items[i]));
			}
			for (int i = items.length - 1; i >= 0; i--) {
				allModel.remove(items[i]);
			}
		}

		if (action.equals("left")) {
			int[] items = select.getSelectedIndices();
			for (int i = 0; i < items.length; i++) {
				allModel.addElement(selectModel.elementAt(items[i]));
			}
			for (int i = items.length - 1; i >= 0; i--) {
				selectModel.remove(items[i]);
			}
		}

		if (action.equals("ok")) {
			buttonHit = JOptionPane.OK_OPTION;
			setVisible(false);
		}

		if (action.equals("cancel")) {
			buttonHit = JOptionPane.CANCEL_OPTION;
			setVisible(false);
		}

	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	public Object[] getSelectedList() {
		return selectModel.toArray();
	}

}
