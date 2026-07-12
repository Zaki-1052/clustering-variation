package Gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class JobLogDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JTextArea outputArea;
	private JButton okButton;
	public JobLogDlg(JTextArea log) {
		setTitle("Structure Job Log");
		setLocationRelativeTo(null);

		outputArea = log;
		outputArea.setEditable(false);
		JScrollPane sp = new JScrollPane(outputArea);

		sp.setPreferredSize(new Dimension(350, 350));

		okButton = new JButton("  OK  ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		JPanel buttonPane = new JPanel();

		buttonPane.add(okButton);
		buttonPane.setPreferredSize(new Dimension(350, 50));

		JPanel topPane = new JPanel();
		JLabel title = new JLabel("Structure Job Log");
		topPane.add(title);
		getContentPane().add("North", topPane);
		getContentPane().add("Center", sp);
		getContentPane().add("South", buttonPane);
	}

	public int showDialog() {
		setModal(false);
		pack();
		super.setEnabled(true);
		super.setVisible(true);
		return buttonHit;
	}

	public JTextArea getOutput() {
		return outputArea;
	}

	public void actionPerformed(ActionEvent event) {

		String action = event.getActionCommand();
		if (action.equals("ok")) {
			buttonHit = JOptionPane.OK_OPTION;
			setVisible(false);
			return;
		}

	}

}
