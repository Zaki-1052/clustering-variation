package Triangle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import Controller.StructureApp;
import Util.GeneralFileFilter;

public class TrianglePlotManager implements ActionListener {

	private StructureApp app;

	private String datafile; // the data file
	private TrianglePlot tplot; // the drawing area
	private DataParser parser; // the data parser
	private TriPlotFm frame; // the Gui frame

	private int popsize;
	private JComboBox[] palettelist;
	private Hashtable<Integer, Color> colormap;

	private Vector<Integer> idVec;

	private int leftindex; // the index to the bottom left corner
	private int rightindex; // the index to the bottom right corner
	private double[][] data;
	private int[] id;

	private File[] runlist;
	private String[] simlist;

	private Color[] colorlist;

	private int simlist_select;
	private int filelist_select;
	private int bleft_select;
	private int bright_select;

	public TrianglePlotManager(StructureApp app, String[] simList) {
		this.app = app;
		if (simList == null || simList.length == 0) {
			return;
		}
		simlist = new String[simList.length + 1];
		simlist[0] = "Select one";
		for (int i = 0; i < simList.length; i++) {
			simlist[i + 1] = simList[i];
		}
	}

	// constructor use to show specified result file

	public TrianglePlotManager() {
	}

	public void showFrame(String paramset, String filename) {
		frame = new TriPlotFm(this);
		tplot = new TrianglePlot();
		File file = new File(filename);

		frame.initState(paramset, file.getName());
		frame.setTitle("Triangle Plot - Structure");

		if (!parseFile(filename)) {
			return;
		}

		// init a default plot
		if (frame.v1list.getItemCount() >= 3) {
			frame.v1list.setSelectedIndex(1);
			bleft_select = leftindex = 1;
			frame.v2list.setSelectedIndex(2);
			bright_select = rightindex = 2;
			updatePlot();
		}
		frame.setLocation(300, 200);
		frame.pack();
		frame.setVisible(true);
	}

	public void showFrame() {

		frame = new TriPlotFm(this);
		tplot = new TrianglePlot();
		frame.initState(simlist);
		frame.setTitle("Triangle Plot - Structure");
		frame.pack();
		frame.setVisible(true);

	}

	public JScrollPane createPalette() {

		palettelist = new JComboBox[popsize];

		JPanel palette = new JPanel();
		palette.setLayout(new GridLayout(0, 1));

		Object[] boxObj = new Object[popsize];
		for (int i = 0; i < popsize; i++) {
			boxObj[i] = new Object[] { colorlist[i] };
		}

		for (int i = 0; i < popsize; i++) {
			String ls;
			if (popsize == 1) {
				ls = "";
			} else {
				ls = new String("Pop " + idVec.elementAt(i));
			}
			JLabel l = new JLabel(ls);

			JPanel lp = new JPanel();
			lp.add(l);
			JComboBox box = new JComboBox(boxObj);
			box.setRenderer(new ColorRenderer());
			box.setSelectedIndex(i);

			box.setActionCommand("mcolor" + i);
			box.addActionListener(this);
			palettelist[i] = box;
			JPanel bp = new JPanel();
			bp.add(box);

			JPanel pp = new JPanel();
			pp.add(lp);
			pp.add(bp);
			palette.add(pp);

		}

		palette.setPreferredSize(new Dimension(250, 40 * popsize));

		return new JScrollPane(palette);
	}

	// Read data file , extract information
	// load data to plotter
	// update the frame gui

	public boolean parseFile(String datafile) {
		parser = new DataParser(datafile);

		// get number of inferred cluster
		// update bottomleft and bottomright list
		int cluster = parser.getK();

		data = parser.getData();
		if (data == null) {
			JOptionPane.showMessageDialog(null,
					"Data file is not in expected format", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		id = parser.getId();

		frame.v1list.removeActionListener(this);
		frame.v2list.removeActionListener(this);

		frame.v1list.setEnabled(true);
		frame.v1list.removeAllItems();
		frame.v1list.addItem("Select one");
		frame.v2list.setEnabled(true);
		frame.v2list.removeAllItems();
		frame.v2list.addItem("Select one");

		for (int i = 0; i < cluster; i++) {
			frame.v1list.addItem("cluster " + (i + 1));
			frame.v2list.addItem("cluster " + (i + 1));
		}
		frame.v1list.addActionListener(this);
		frame.v2list.addActionListener(this);

		// get the population information
		// update the palette

		Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>();
		idVec = new Vector<Integer>(); // use to sort the pop id
		if (id != null) {
			// build a hash table to count pre-input population numbers

			for (int i = 0; i < id.length; i++) {
				Integer index = new Integer(id[i]);
				if (!ht.containsKey(index)) {
					ht.put(index, new Integer(1));
					idVec.add(index);
				}
			}

			popsize = ht.size();
		} else {
			popsize = 1;
		}

		// create color mapping
		colorlist = new Color[popsize];

		for (int i = 0; i < popsize; i++) {
			colorlist[i] = null;
		}

		for (int i = 0; i < popsize; i++) {
			if (i == 0) {
				colorlist[i] = new Color(1f, 0f, 0f);
			}
			if (i == 1) {
				colorlist[i] = new Color(0f, 1f, 0f);
			}
			if (i == 2) {
				colorlist[i] = new Color(0f, 0f, 1f);
			}
			if (i == 3) {
				colorlist[i] = new Color(1f, 1f, 0f);
			}
			if (i == 4) {
				colorlist[i] = new Color(1f, 0f, 1f);
			}
			if (i == 5) {
				colorlist[i] = new Color(0f, 1f, 1f);
			}
			if (i == 6) {
				colorlist[i] = new Color(0.98f, 0.60f, 0.0f);
			}
			if (i == 7) {
				colorlist[i] = new Color(0.5f, 0.25f, 0.37f);
			}
			if (i == 8) {
				colorlist[i] = new Color(0.98f, 0.6f, 0.37f);
			}
			if (i == 9) {
				colorlist[i] = new Color(0.1f, 0.6f, 0.75f);
			}
			if (i > 9) {
				colorlist[i] = randomize_color();
			}
		}

		colormap = new Hashtable<Integer, Color>();
		if (popsize > 1) {
			// build a hashtable for colormap
			Collections.sort(idVec);

			for (int i = 0; i < idVec.size(); i++) {
				colormap.put(idVec.elementAt(i), colorlist[i]);

			}
		} else {
			colormap.put(new Integer(0), colorlist[0]);
		}

		frame.updateGui(createPalette());
		return true;
	}

	public void setRunList(String simName) {

		runlist = app.getRunFiles(simName);

		frame.rlist.removeActionListener(this);
		frame.rlist.setEnabled(true);
		frame.rlist.removeAllItems();
		frame.rlist.addItem("Select one");

		if (runlist == null || runlist.length == 0) {
			frame.rlist.setEnabled(false);
			return;
		}

		for (int i = 0; i < runlist.length; i++) {
			String filename = runlist[i].getName();
			frame.rlist.addItem(filename.substring(0, filename.length() - 2));
		}
		frame.rlist.addActionListener(this);
	}

	private Color randomize_color() {

		float nr = 0f;
		float ng = 0f;
		float nb = 0f;
		Random gr = new Random();
		do {
			nr = gr.nextFloat();
			ng = gr.nextFloat();
			nb = gr.nextFloat();
		} while (exist_in_colormap(nr, ng, nb));

		return new Color(nr, ng, nb);

	}

	private boolean exist_in_colormap(float nr, float ng, float nb) {

		for (int i = 0; i < colorlist.length; i++) {
			if (colorlist[i] == null) {
				return false;
			}

			float er = (colorlist[i].getRed()) / 255f;
			float eg = (colorlist[i].getGreen()) / 255f;
			float eb = (colorlist[i].getBlue()) / 255f;
			float diff = (er - nr) * (er - nr) + (eg - ng) * (eg - ng)
					+ (eb - nb) * (eb - nb);
			if (diff <= 0.02) {
				return true;
			}
		}

		return false;
	}

	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();

		if (action.equals("simlist")) {
			int select = frame.plist.getSelectedIndex();
			if (select == 0) {
				frame.plist.setSelectedIndex(simlist_select);
				return;
			}
			simlist_select = select;
			frame.plist.setSelectedIndex(select);
			setRunList(simlist[select]);

			frame.resetGui();
			tplot.resetData();
			tplot.repaint();

			return;
		}

		if (action.equals("filelist")) {
			int select = frame.rlist.getSelectedIndex();
			if (select == 0) {
				frame.rlist.setSelectedIndex(filelist_select);
				return;
			}
			filelist_select = select;
			resetAll();
			parseFile(runlist[select - 1].getAbsolutePath());

			// init a default plot
			if (frame.v1list.getItemCount() >= 3) {
				frame.v1list.setSelectedIndex(1);
				bleft_select = leftindex = 1;
				frame.v2list.setSelectedIndex(2);
				bright_select = rightindex = 2;
				updatePlot();
			}
		}
		if (action.equals("bottomleft")) {
			int select = frame.v1list.getSelectedIndex();
			if (select == 0) {
				frame.v1list.setSelectedIndex(bleft_select);
				return;
			}
			leftindex = select;

			if (!updatePlot()) {
				frame.v1list.setSelectedIndex(bleft_select);
				leftindex = bleft_select;
			} else {
				bleft_select = leftindex;
			}

			return;
		}

		if (action.equals("bottomright")) {
			int select = frame.v2list.getSelectedIndex();
			if (select == 0) {
				frame.v2list.setSelectedIndex(bright_select);
				return;
			}
			rightindex = select;

			if (!updatePlot()) {
				frame.v2list.setSelectedIndex(bright_select);
				rightindex = bright_select;
			} else {
				bright_select = rightindex;
			}

			return;
		}

		if (action.equals("save")) {

			JFileChooser fc = new JFileChooser();
			GeneralFileFilter jpgFilter = new GeneralFileFilter("jpg",
					"JPEG image files");
			fc.addChoosableFileFilter(jpgFilter);
			int returnVal = fc.showDialog(frame, "Save");

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();
				String fname = file.getAbsolutePath();
				if (!fname.endsWith(".jpg") && !fname.endsWith(".jpeg")) {
					file = new File(new String(fname + ".jpg"));
				}

				// check if file exists
				if (file.exists()) {
					Object[] options = { "Yes", "No " };

					int n = JOptionPane.showOptionDialog(frame,
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
				tplot.saveImage(file.getAbsolutePath());
			}
		}
		if (action.equals("close")) {
			frame.dispose();
		}

		if (action.startsWith("mcolor")) {

			for (int i = 0; i < popsize; i++) {
				if (action.equals(new String("mcolor" + i))) {
					int select = palettelist[i].getSelectedIndex();
					// colormap[i]=colorlist[select];
					if (popsize <= 1) {
						colormap.put(new Integer(0), colorlist[select]);
					} else {
						colormap.put(idVec.elementAt(i), colorlist[select]);
					}
					updatePlot();
					return;
				}
			}
		}

	}

	private boolean updatePlot() {

		// error checking
		if (rightindex == 0 || leftindex == 0) {
			return true;
		}
		if (rightindex == leftindex) {
			return false;
		}

		tplot.loadData(data, id, colormap, leftindex, rightindex);
		tplot.repaint();
		frame.updatePlot(tplot);
		return true;
	}

	private void resetAll() {

		palettelist = null;
		colormap = null;
		leftindex = rightindex = 0;
		data = null;
		id = null;

		tplot.resetData();
		tplot.repaint();
	}

	/*
	 * static public void main(String[] args){ TrianglePlotManager tpm = new
	 * TrianglePlotManager(); tpm.showFrame();
	 * 
	 * tpm.setRunList(new File[] { new File("ps1_run_1_f"), new
	 * File("ps1_run_2_f"), new File("ps3_run_1_f"), new File("ps3_run_2_f"),
	 * });;
	 * 
	 * 
	 * }
	 */
}

class ColorRenderer extends JLabel implements ListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static ColorIcon icon = new ColorIcon();

	private Border redBorder = BorderFactory.createLineBorder(Color.red, 2),
			emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Object[] array = (Object[]) value;

		icon.setColor((Color) array[0]);
		setIcon(icon);

		if (isSelected) {
			setBorder(redBorder);
		} else {
			setBorder(emptyBorder);
		}

		return this;
	}
}

class ColorIcon implements Icon {
	private Color color;
	private int w, h;

	public ColorIcon() {
		this(Color.gray, 50, 15);
	}

	public ColorIcon(Color color, int w, int h) {
		this.color = color;
		this.w = w;
		this.h = h;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.black);
		g.drawRect(x, y, w - 1, h - 1);
		g.setColor(color);
		g.fillRect(x + 1, y + 1, w - 2, h - 2);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getIconWidth() {
		return w;
	}

	public int getIconHeight() {
		return h;
	}
}
