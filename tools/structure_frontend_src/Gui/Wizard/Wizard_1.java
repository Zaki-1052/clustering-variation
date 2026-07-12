package Gui.Wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class Wizard_1 extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int buttonHit;
	private JTextField name, location, dataFile;
	@SuppressWarnings("unused")
	private WizardController controller;

	// startpoint of file chooser
	private File fc_dir1, fc_dir2;

	public Wizard_1(Frame parent, WizardController controller) {
		super(parent, "Step 1 of 4 - Project Wizard", true);
		this.controller = controller;

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		buttonHit = JOptionPane.CLOSED_OPTION;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		//
		// Create Content of the dialog
		//

		JLabel dlglabel = new JLabel("       Step 1 of 4: Project information ");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add("Center", dlglabel);
		topPanel.setPreferredSize(new Dimension(500, 25));

		JLabel namelabel = new JLabel("Name the project");
		name = new JTextField(10);
		JPanel pn = new JPanel();
		pn.add(name);
		// name.addActionListener(this);
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new FlowLayout());
		namePanel.add(namelabel);
		namePanel.add(pn);

		JLabel locationlabel = new JLabel("Select directory");
		location = new JTextField(10);
		JPanel locationPanel = new JPanel();
		locationPanel.setLayout(new FlowLayout());
		JButton browseButton = new JButton("Browse ...");
		browseButton.setActionCommand("dir");
		browseButton.addActionListener(this);
		JPanel pb = new JPanel();
		pb.add(browseButton);

		JPanel pt = new JPanel();
		pt.add(location);

		JPanel ptb = new JPanel();
		ptb.add(pt);
		ptb.add(pb);

		locationPanel.add(locationlabel);
		locationPanel.add(ptb);
		// locationPanel.add(browseButton);

		JLabel fileLabel = new JLabel("Choose data file");
		dataFile = new JTextField(10);
		JPanel pd = new JPanel();
		pd.add(dataFile);

		JButton fileButton = new JButton("Browse ... ");
		fileButton.setActionCommand("file");
		fileButton.addActionListener(this);
		JPanel pf = new JPanel();
		pf.add(fileButton);

		JPanel pdf = new JPanel();
		pdf.add(pd);
		pdf.add(pf);

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new FlowLayout());
		filePanel.add(fileLabel);
		filePanel.add(pdf);

		JPanel paraPane = new JPanel();
		paraPane.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(10, 10, 5, 10);
		gridbag.setConstraints(namePanel, c);
		paraPane.add(namePanel);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(0, 10, 5, 10);
		gridbag.setConstraints(locationPanel, c);
		paraPane.add(locationPanel);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		gridbag.setConstraints(filePanel, c);
		paraPane.add(filePanel);

		paraPane.setPreferredSize(new Dimension(500, 250));

		JButton nextButton = new JButton("Next>>");
		nextButton.setActionCommand("w1_next");
		nextButton.addActionListener(controller);
		JButton cancelButton = new JButton("Cancel ");
		cancelButton.setActionCommand("w1_cancel");
		cancelButton.addActionListener(controller);
		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(nextButton);
		buttonPanel.add(cancelButton);

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(BorderLayout.EAST, buttonPanel);
		bottomPanel.setPreferredSize(new Dimension(500, 75));

		getContentPane().setLayout(gridbag);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(topPanel, c);
		getContentPane().add(topPanel);

		c.gridy = 1;
		gridbag.setConstraints(paraPane, c);
		getContentPane().add(paraPane);

		c.gridy = 2;
		gridbag.setConstraints(bottomPanel, c);
		getContentPane().add(bottomPanel);

		addWindowListener(controller);

	}

	public int showDialog() {
		setModal(true);
		pack();
		super.setVisible(true);
		return buttonHit;
	}

	public String getProjName() {
		return name.getText();
	}

	public String getProjDir() {
		return location.getText();
	}

	public String getDataFile() {
		return dataFile.getText();
	}

	public void actionPerformed(ActionEvent e) {

		final JFileChooser fc = new JFileChooser();
		fc.setApproveButtonText("Select");
		if ((e.getActionCommand()).equals("dir")) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setCurrentDirectory(fc_dir1);
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.getParentFile() != null) {
					fc_dir1 = file.getParentFile();
				}
				if (!file.canWrite()) {
					JOptionPane.showMessageDialog(null, "You do not have write permission to directory \"" + file.getName() + "\"","Write Permission Error", JOptionPane.ERROR_MESSAGE);
				
				} else {
				// this is where a real application would open the file.
					location.setText(file.getAbsolutePath());
				}
			}
		} else if ((e.getActionCommand()).equals("file")) {
			if (fc_dir2 == null && fc_dir1 != null) {
				fc_dir2 = fc_dir1;
			}
			fc.setCurrentDirectory(fc_dir2);
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				fc_dir2 = file.getParentFile();
				// this is where a real application would open the file.
				dataFile.setText(file.getAbsolutePath());
			}

		}

	}
}
