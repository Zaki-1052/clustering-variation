package Gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RstDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField rstField, outField;
	private String rstfile, rtmfile;
	private File dirCache;
	int buttonHit;

	public RstDlg(JFrame frame) {

		super(frame);

		setTitle("Select results files");
		setLocation(600, 500);

		// layout
		JPanel p1 = new JPanel();
		JLabel l1 = new JLabel("   Result file     ");
		rstField = new JTextField(9);
		JButton b1 = new JButton("Browse...");
		b1.setActionCommand("rst");
		b1.addActionListener(this);
		p1.add(l1);
		p1.add(rstField);
		p1.add(b1);

		JPanel p2 = new JPanel();
		JLabel l2 = new JLabel("Runtime output ");
		outField = new JTextField(9);
		JButton b2 = new JButton("Browse...");
		b2.setActionCommand("rtm");
		b2.addActionListener(this);
		p2.add(l2);
		p2.add(outField);
		p2.add(b2);

		JButton okButton = new JButton("   OK   ");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("  Cancel ");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		JPanel packPane = new JPanel();
		packPane.setLayout(new GridLayout(0, 1));
		packPane.add(p1);
		packPane.add(p2);
		packPane.setPreferredSize(new Dimension(400, 80));
		JPanel topPane = new JPanel();
		JLabel l6 = new JLabel(" ");
		topPane.add(l6);
		topPane.setPreferredSize(new Dimension(400, 30));
		buttonPane.setPreferredSize(new Dimension(400, 50));
		getContentPane().add("North", topPane);
		getContentPane().add("Center", packPane);
		getContentPane().add("South", buttonPane);

		setSize(400, 160);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				buttonHit = JOptionPane.CANCEL_OPTION;
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

	private boolean validateInput() {
		String input = rstField.getText();
		StringTokenizer st = new StringTokenizer(input);
		if (!st.hasMoreTokens()) {
			JOptionPane.showMessageDialog(null, "Result file can not be empty",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File testFile = new File(input);
		if (!testFile.canRead()) {
			JOptionPane.showMessageDialog(null, "Can not open result file "
					+ input, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		rstfile = input;

		input = outField.getText();
		st = new StringTokenizer(input);
		if (st.hasMoreTokens()) {
			testFile = new File(input);
			if (!testFile.canRead()) {
				JOptionPane.showMessageDialog(null,
						"Can not open runtime output file " + input, "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

		}

		rtmfile = input;
		return true;
	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		if (action.equals("rst")) {
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(dirCache);
			fc.setApproveButtonText("Select");

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				rstField.setText(file.getAbsolutePath());
				dirCache = file.getParentFile();
			}
		}
		if (action.equals("rtm")) {
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(dirCache);
			fc.setApproveButtonText("Select");

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				outField.setText(file.getAbsolutePath());
				dirCache = file.getParentFile();
			}
		}
		if (action.equals("ok")) {
			if (validateInput()) {
				setVisible(false);
				buttonHit = JOptionPane.OK_OPTION;
			} else {
				return;
			}

		}

		if (action.equals("cancel")) {
			setVisible(false);
			buttonHit = JOptionPane.CANCEL_OPTION;
			return;

		}

	}

	public String getRstFile() {
		return rstfile;
	}

	public String getRtmFile() {
		return rtmfile;
	}

}
