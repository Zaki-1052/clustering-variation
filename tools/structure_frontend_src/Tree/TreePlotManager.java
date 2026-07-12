package Tree;

import interpreter.PAContext;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import Controller.StructureApp;
import Util.GeneralFileFilter;

public class TreePlotManager extends JApplet implements ActionListener,
		Runnable {
	// public class TreePlotManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private StructureApp app;

	private String datafile; // the data file

	// private int popsize;
	private JComboBox[] palettelist;
	private Hashtable colormap;

	private Vector idVec;

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

	private Frame frame;
	private int K;
	private Process proc; // the running process

	private String parameterSetName;
	private String plotFile;
	private String plotFileName;

	private String[] pieces;

	static String postscriptFiles[] = { "test-parameter-set_run_5_f" };

	static JComboBox combo;
	static URL url;
	JPanel p1;
	Demo demo;

	public TreePlotManager(StructureApp app, String[] simList,
			String plottingSimName, String plottingFilePath, String plotString) {

		// System.out.println(plotString);

		// String plotFileName;

		plotString = plotString.replaceFirst(plottingSimName, "");
		plotString = plotString.replace("(", "");
		plotString = plotString.replace(")", "");

		plotFileName = plotString;

		// System.out.println(plotFileName);

		parameterSetName = plottingSimName;

		// System.out.println(parameterSetName);

		// pieces = plottingFilePath.split("/");

		// System.out.println(pieces[pieces.length-1]);
		// System.out.println(plottingFilePath);
		// System.out.println(simList[0]);

		// String fileSep = System.getProperty("file.separator");
		// pieces = plottingFilePath.split(fileSep);

		// could be a portability issue ##portable##
		// plotFile = "library/neighbor/plots/"+pieces[pieces.length-1];
		plotFile = "library/neighbor/plots/" + plotString;

		// plotFile =
		// System.getProperty("user.dir")+fileSep+"library"+fileSep+"neighbor"+fileSep+"plots"+fileSep+pieces[pieces.length-1];

		// System.out.println(plotFile);

		this.app = app;
		if (simList == null || simList.length == 0) {
			return;
		}
		simlist = new String[simList.length + 1];
		simlist[0] = "Select one";
		for (int i = 0; i < simList.length; i++) {
			//System.out.println("Simulation List: " + simList[i]);
			simlist[i + 1] = simList[i];
		}

	}

	public TreePlotManager() {

	}
	
	public boolean canWrite() {
		File path = new File(System.getProperty("user.dir"), "/library/neighbor/plots/");
		return path.canWrite();
	}

	public Integer getK(String paramset, String filename) {
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fINstream = new FileInputStream(filename);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fINstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line

			K = 0;

			while ((strLine = br.readLine()) != null) {
				if (strLine.indexOf("populations assumed") >= 0) {
					StringTokenizer st = new StringTokenizer(strLine);
					K = Integer.valueOf(st.nextToken()).intValue();
					// return K;
				}
			} // end while loop

			// Close the input stream
			in.close();
		} catch (Exception e) {
			System.err.println("Error reading K: " + e.getMessage());
		}
		return new Integer(K);
	}// end getK

	public boolean parseDistanceMatrix(String paramset, String filename) {
		// System.out.println(paramset);
		String userDir = System.getProperty("user.dir");
		// System.out.println(userDir);
		System.out.println("can write: " + canWrite());

		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fINstream = new FileInputStream(filename);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fINstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line

			int matrixRow = 0;
			boolean flag = false;

			try {
				// Create file
				// FileWriter fOUTstream = new
				// FileWriter("./library/neighbor/plots/infile");
				// System.out.println(userDir+"/library/neighbor/plots/infile");
				FileWriter fOUTstream = new FileWriter(userDir
						+ "/library/neighbor/plots/infile");

				BufferedWriter out = new BufferedWriter(fOUTstream);

				while ((strLine = br.readLine()) != null) {

					if (strLine.indexOf("populations assumed") >= 0) {
						StringTokenizer st = new StringTokenizer(strLine);
						K = Integer.valueOf(st.nextToken()).intValue();
						// The phylip algorithm is very picky about the input
						// file, hence the preprocessing.
						out.write("    " + K);
						out.newLine();
					}

					if (strLine.indexOf("Net nucleotide distance") >= 0) {
						flag = true;
					}

					if (matrixRow > 3 && matrixRow < (3 + K + 1)) {
						// The phylip algorithm is very picky about the input
						// file, hence the preprocessing.
						strLine = strLine.replace("   -  ", "0.0000");
						strLine = strLine.replace("   ", "            ");
						out.write(strLine);
						out.newLine();
						// System.out.println(strLine);
					}

					if (flag) {
						matrixRow++;
					}
				} // end while loop

				// Close the output stream
				out.close();
			} catch (Exception e) {
				System.err.println("Error parsing Distance Matrix: " + e.getMessage());
				return false;
			}

			// Close the input stream
			in.close();
		} catch (Exception e) {
			System.err.println("Error reading Distance Matrix: " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean runNeighborAlgorithm(String paramset, String filename) {

		// String userDir = System.getProperty("user.dir");
		// String fileSep = System.getProperty("file.separator");

		// String []pieces = filename.split("/");
		// System.out.println(userDir);
		// System.out.println(fileSep);
		// String []pieces = filename.split(fileSep);
		// String []pieces = filename.split("/");

		String paraSet = paramset;
		// String resultFile = pieces[pieces.length-1];

		try {
			String str;
			// System.out.println(userDir+fileSep+"library"+fileSep+"neighbor"+fileSep+"neighbor "+resultFile);
			// System.out.println("library/neighbor/neighbor "+plotFileName);

			// String dataFolder = System.getProperty("user.dir") +
			// System.getProperty("file.separator") + "data";
			// System.out.println("Data Folder = " + dataFolder);

			proc = Runtime.getRuntime().exec(
					"library/neighbor/neighbor " + plotFileName);
			// proc =
			// Runtime.getRuntime().exec(userDir+fileSep+"library"+fileSep+"neighbor"+fileSep+"neighbor "+resultFile);

			BufferedReader proc_in = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));

			try {
				while ((str = proc_in.readLine()) != null) {
					// output from the c routines.
					// System.out.print(str+"\n");
				}
			} catch (IOException e) {
				System.err.println(e);//System.exit(0);
				return false;
			}

		} catch (IOException e1) {
			System.err.println(e1);
			return false;
		}
		return true;
	}

	public void createTreePlot(String paramset, String filename) {
		//System.out.println("Creating Tree Plot Window");
		// String userDir = System.getProperty("user.dir");

		JLabel pname = new JLabel("Parameter set:");
		JLabel rname = new JLabel("    Result file:");

		// String fileSep = System.getProperty("file.separator");
		// String []pieces = filename.split(fileSep);
		// String []pieces = filename.split("/");

		String[] paraSet = { paramset };

		// String[] resultFile = {pieces[pieces.length-1]};
		String[] resultFile = { plotFileName };

		// final String resultFileStr = pieces[pieces.length-1];
		final String resultFileStr = plotFileName;

		JComboBox paraList = new JComboBox(paraSet);
		JComboBox resultFileList = new JComboBox(resultFile);

		paraList.setEnabled(false);
		resultFileList.setEnabled(false);

		JPanel ptop = new JPanel(new FlowLayout());

		ptop.setBackground(Color.gray);

		ptop.add(pname);
		ptop.add(paraList);
		ptop.add(rname);
		ptop.add(resultFileList);

		JPanel p1 = new JPanel(new FlowLayout());
		JPanel p2 = new JPanel(new BorderLayout());

		EmptyBorder eb = new EmptyBorder(5, 20, 10, 20);
		p1.setBorder(eb);
		p1.setBackground(Color.gray);

		p1.setToolTipText("click to start/stop iterating");
		p1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (demo.thread == null) {
					demo.start();
				} else {
					demo.stop();
				}
			}
		});

		getContentPane().add("South", p1);
		getContentPane().add("North", ptop);

		eb = new EmptyBorder(20, 20, 5, 20);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		p2.setBorder(new CompoundBorder(eb, bb));
		p2.setBackground(Color.gray);
		p2.add(demo = new Demo());

		getContentPane().add("Center", p2);

		// System.out.println(resultFileStr);

		// System.out.println(plotFile);
		// System.out.println("../library/neighbor/plots/"+resultFileStr);
		// System.out.println(userDir+"/library/neighbor/plots/"+resultFileStr);

		// url =
		// PostscriptViewer.class.getResource(userDir+"/library/neighbor/plots/"+resultFileStr);
		// url = PostscriptViewer.class.getResource(plotFile);

		// url =
		// PostscriptViewer.class.getResource("../library/neighbor/plots/"+resultFileStr);
		// url =
		// PostscriptViewer.xxxxxcc("../library/neighbor/plots/"+resultFileStr);
		// url = "file:"+plotFile;

		// System.out.println(url);
		File file = new File(plotFile);

		try {
			url = file.toURL(); // file:/d:/almanac1.4/java.io/filename
		} catch (MalformedURLException e) {

		}

		JButton saveButton = new JButton("Save");
		JButton closeButton = new JButton("Close");

		saveButton.addActionListener(this);
		saveButton.setActionCommand("save");
		saveButton.setEnabled(true);

		closeButton.addActionListener(this);
		closeButton.setActionCommand("close");
		closeButton.setEnabled(true);

		p1.add(saveButton, BorderLayout.CENTER);

		frame = new Frame("Neighbor Joining Tree - Structure");

		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				deletefile(plotFile);
			}
		};

		frame.addWindowListener(l);
		frame.add("Center", this);

		frame.pack();

		int w = 400 * 2;
		int h = 400 * 2;

		frame.setSize(new Dimension(w, h));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		frame.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2
				- h / 2); // the position of the window
		frame.setVisible(true);
		frame.setEnabled(true);

	}

	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();

		// System.out.println(action);

		if (action.equals("save")) {

			JFileChooser fc = new JFileChooser();
			GeneralFileFilter jpgFilter = new GeneralFileFilter("ps",
					"POSTSCRIPT image files");
			fc.addChoosableFileFilter(jpgFilter);
			int returnVal = fc.showDialog(frame, "Save");

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();
				String fname = file.getAbsolutePath();
				if (!fname.endsWith(".jpg") && !fname.endsWith(".ps")) {
					file = new File(new String(fname + ".ps"));
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

				demo.saveImagePlotFile(file.getAbsolutePath(), plotFile);
			}
		}

		if (action.equals("close")) {
			frame.dispose();
		}

	}

	public void deletefile(String file) {
		File f1 = new File(file);
		boolean success = f1.delete();
		if (!success) {
			System.out.println("Deletion failed.");
			//System.exit(0);
		} else {
			// System.out.println("File deleted.");
		}
	}

	public void run() {
		demo.repaint();
	}

	static class Demo extends Canvas implements Runnable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Thread thread;
		Graphics2D g2;
		BufferedImage image;
		Graphics fg;

		public Demo() {
			setBackground(Color.white);
		}

		public void paint(Graphics g) {
			Dimension d = getSize();
			Graphics2D g2 = (Graphics2D) g;
			// System.out.println(d);
			// double s = Math.min(d.width, d.height) / 800.0;
			double s = Math.min(d.width, d.height) / 700.0;

			PAContext context = new PAContext(g2, d);

			if (s != 1.0) {
				AffineTransform fitInPage = new AffineTransform();
				fitInPage.scale(s * (1.2), s * (0.9));
				// fitInPage.scale(s,s);
				g2.transform(fitInPage);
			}
			try {
				InputStream inputStream = url.openStream();
				context.draw(inputStream);
				inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void saveImage(String filename) {
			BufferedImage image = new BufferedImage(420, 380,
					BufferedImage.TYPE_INT_RGB);

			Graphics fg = image.getGraphics();
			this.paint(fg);

			try {
				File f = new File(filename);
				// JimiRasterImage jrf =
				// Jimi.createRasterImage(image.getSource());
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(f);
				} catch (Exception ee) {
				}

				if (fos != null) {
					// Jimi.putImage("image/jpeg",jrf,fos);
					ImageIO.write(image, "JPG", fos);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			// end testing code
		}

		public void saveImagePlotFile(String filename, String plotfile) {

			// System.out.println(filename);
			// String srFile = "library/neighbor/plots/plotfile";

			String dtFile = filename;
			String srFile = plotfile;

			try {
				File f1 = new File(srFile);
				File f2 = new File(dtFile);

				InputStream in = new FileInputStream(f1);

				// For Overwrite the file.
				OutputStream out = new FileOutputStream(f2);

				byte[] buf = new byte[1024];
				int len;

				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

			} catch (FileNotFoundException ex) {
				System.out.println(ex.getMessage()
						+ " in the specified directory.");
				//System.exit(0);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		public void start() {
			if (thread != null) {
				return;
			}
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}

		public synchronized void stop() {
			if (thread != null) {
				thread.interrupt();
			}
			thread = null;
		}

		public void run() {
			Thread me = Thread.currentThread();
			while (thread == me) {
				for (int i = 0; i < postscriptFiles.length; i++) {
					combo.setSelectedIndex(i);
					try {
						Thread.sleep(6000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
			thread = null;
		}
	} // End Demo class

	/*
	 * public static void main(String args[]){ TreePlotManager tpm = new
	 * TreePlotManager();
	 * 
	 * }
	 */
}
