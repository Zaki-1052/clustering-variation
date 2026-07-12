package Plot;

import jas.plot.PrintHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import Util.GeneralFileFilter;

public class RstPlotManager extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[][] data;
	private int[] labels;
	private int[] popid;

	private JScrollPane sp;

	private RstPlot rp; // the plotter
	private RstDraw rd; // printable panel
	private JRadioButton sortButton, groupButton, origButton, singleButton,
			multiButton;
	private JPanel contentPane, optionPane, buttonPane;

	// 0 for orig , 1 for sort , 2 for group
	private int currPlot;

	private boolean single_line = true;

	// constructor , build and show the frame
	public RstPlotManager(JFrame owner, String filepath, String title) {

		super(owner, false);

		rp = new RstPlot(filepath);

		// plot in original order
		data = rp.getData();
		if (data == null) {
			JOptionPane.showMessageDialog(null,
					"Result data file is not in expected format", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		labels = rp.getLabel();
		popid = rp.getPopId();
		currPlot = 0;

		rd = new RstDraw(data, labels, popid);

		sp = new JScrollPane(rd);
		sp.setPreferredSize(new Dimension(700, 230));

		JButton printButton = new JButton("Print");
		printButton.setActionCommand("print");

		JButton saveButton = new JButton(" Save");
		saveButton.setActionCommand("save");

		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");

		sortButton = new JRadioButton("Sort by Q    ");
		sortButton.setActionCommand("sort");

		groupButton = new JRadioButton("Group by POP Id");
		groupButton.setActionCommand("group");

		if (popid == null) {
			groupButton.setEnabled(false);
		}

		origButton = new JRadioButton("Original order");
		origButton.setActionCommand("orig");

		singleButton = new JRadioButton("Plot in single line");
		singleButton.setActionCommand("single");
		multiButton = new JRadioButton("Plot in multiple lines");
		multiButton.setActionCommand("multi");

		printButton.addActionListener(this);
		saveButton.addActionListener(this);
		closeButton.addActionListener(this);
		sortButton.addActionListener(this);
		groupButton.addActionListener(this);
		origButton.addActionListener(this);
		singleButton.addActionListener(this);
		multiButton.addActionListener(this);

		origButton.setSelected(true);
		singleButton.setSelected(true);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridLayout(0, 1));
		leftPane.add(origButton);
		leftPane.add(groupButton);
		leftPane.add(sortButton);

		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(origButton);
		bg1.add(sortButton);
		bg1.add(groupButton);

		JPanel rightPane = new JPanel();
		rightPane.setLayout(new GridLayout(0, 1));
		rightPane.add(singleButton);
		rightPane.add(multiButton);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(singleButton);
		bg2.add(multiButton);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		optionPane = new JPanel();
		optionPane.setLayout(gridbag);
		// optionPane.setPreferredSize(new Dimemsion(700,
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 10, 5, 10);
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(leftPane, c);
		optionPane.add(leftPane);

		c.insets = new Insets(5, 10, 5, 20);
		c.gridx = 1;
		gridbag.setConstraints(rightPane, c);
		optionPane.add(rightPane);

		buttonPane = new JPanel();
		buttonPane.add(printButton);
		buttonPane.add(saveButton);
		buttonPane.add(closeButton);

		JPanel btPane = new JPanel();
		btPane.setLayout(new BorderLayout());
		btPane.add(optionPane, BorderLayout.CENTER);
		btPane.add(buttonPane, BorderLayout.SOUTH);
		btPane.setPreferredSize(new Dimension(700, 130));

		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(sp, BorderLayout.CENTER);
		contentPane.add(btPane, BorderLayout.SOUTH);

		getContentPane().add(contentPane);

		setTitle(title);
		setLocation(300, 200);
		data = null;
		setSize(700, 360);
		setVisible(true);

	}

	public void updatePlot(int option) {

		// 0 original order
		// 1 for sorted by Q value
		// 2 group by popid

		if (option == 0) {
			data = rp.getData();
		} else if (option == 1) {
			data = rp.sortData();
		} else if (option == 2) {
			data = rp.groupData();
		} else {
			return;
		}

		labels = rp.getLabel();
		popid = rp.getPopId();

		if (popid == null && option == 2) {
			JOptionPane.showMessageDialog(null,
					"No population information available", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (data == null) {
			return;
		}

		int num = data.length;
		if (num == 0) {
			return;
		}

		sp.remove(rd);
		contentPane.remove(sp);

		sp = new JScrollPane(rd);

		if (option == 2 && single_line) {
			rd.setPoplabel(true);
		} else {
			rd.setPoplabel(false);
		}
		rd.reLoadData(data, labels, popid, single_line);
		if (single_line) {
			sp.setPreferredSize(new Dimension(700, 230));
		} else {
			sp.setPreferredSize(new Dimension(700, 480));
		}
		contentPane.add(sp, BorderLayout.CENTER);
		pack();
		currPlot = option;
		data = null;
	}

	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();

		if (action.equals("print")) {

			try {
				PrintHelper ph = PrintHelper.instance();
				ph.printTarget(rd);
			} catch (Exception x) {// Error while printing
				x.printStackTrace();
			}
			return;
		}

		if (action.equals("save")) {

			JFileChooser fc = new JFileChooser();
			GeneralFileFilter jpgFilter = new GeneralFileFilter("jpg",
					"JPEG image files");
			fc.addChoosableFileFilter(jpgFilter);
			int returnVal = fc.showDialog(this, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();
				String fname = file.getAbsolutePath();
				if (!fname.endsWith(".jpg") && !fname.endsWith(".jpeg")) {
					file = new File(new String(fname + ".jpg"));
				}
				// check if file exists
				if (file.exists()) {
					Object[] options = { "Yes", "No " };

					int n = JOptionPane.showOptionDialog(this,
							"Target file " + file.getName()
									+ " already exists, Overwrite it?",
							"Save image", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[1]);
					if (n == JOptionPane.NO_OPTION) {
						return;
					}
				}
				File fc_dir = file.getParentFile();
				if (!fc_dir.canWrite()) {
					JOptionPane.showMessageDialog(null,
							"Can not write image file: permission denied",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				rd.saveImage(file.getAbsolutePath());
			}

		}

		if (action.equals("sort")) {
			updatePlot(1);
			return;
		}
		if (action.equals("group")) {
			updatePlot(2);
			return;
		}

		if (action.equals("orig")) {
			updatePlot(0);
			return;
		}

		if (action.equals("single") && !single_line) {
			single_line = true;
			updatePlot(currPlot);
			return;
		}

		if (action.equals("multi") && single_line) {
			single_line = false;
			updatePlot(currPlot);
			return;
		}

		if (action.equals("close")) {
			dispose();
		}
	}

}
