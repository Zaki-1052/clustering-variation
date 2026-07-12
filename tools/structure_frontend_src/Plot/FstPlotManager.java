package Plot;

import jas.hist.JASHist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import Proc.AppProc;
import Util.GeneralFileFilter;

public class FstPlotManager {

	private JFrame target;
	Plot[] stg;
	JASHist[] sth;

	private int dataset;
	@SuppressWarnings("unused")
	private AppProc proc;

	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				saveCurveImage(Integer.parseInt(e.getActionCommand()));
			} catch (Exception exception) {
			}
		}
	};

	private ActionListener hlistener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				saveHistImage(Integer.parseInt(e.getActionCommand()));
			} catch (Exception exception) {
			}
		}
	};

	public FstPlotManager() {
	}

	public void plot(double[][] plotdata, String title) {

		dataset = plotdata.length;
		stg = new Plot[dataset];

		if (dataset == 1) {
			stg[0] = new Plot(plotdata[0], "Fst", "Fst Vs. Iterations");
		} else {

			for (int i = 0; i < dataset; i++) {
				stg[i] = new Plot(plotdata[i], "Fst", "Fst" + (i + 1)
						+ " Vs. Iterations");

			}
		}
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));

		for (int i = 0; i < dataset; i++) {
			JPanel gPane = new JPanel();
			gPane.setLayout(new BorderLayout());
			gPane.add(stg[i], BorderLayout.CENTER);
			JButton saveButton = new JButton("Save image");
			saveButton.addActionListener(listener);
			saveButton.setActionCommand("" + i);
			JPanel bPane = new JPanel();
			bPane.add(saveButton);
			gPane.add(bPane, BorderLayout.SOUTH);
			p.add(gPane);

		}
		JScrollPane sp = new JScrollPane(p);
		sp.setBackground(Color.white);
		if (dataset > 1) {
			sp.setPreferredSize(new Dimension(500, 500));
		} else {
			sp.setPreferredSize(new Dimension(500, 350));
		}
		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Structure Plotting: Fst - " + title);

		target.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				target.setVisible(false);
			}

		});

		JPanel top = new JPanel();
		top.setPreferredSize(new Dimension(500, 20));

		target.getContentPane().add("North", top);
		target.getContentPane().add("Center", sp);

		target.setLocation(300, 200);
		target.pack();
		target.setVisible(true);
	}

	// Plotting the run-time data

	public void plot(AppProc proc, boolean onefst, int dataset) {

		this.proc = proc;
		this.dataset = dataset;
		if (onefst) {
			dataset = 1;
		}

		final Plot[] g = new Plot[dataset];

		if (onefst) {
			g[0] = new Plot(proc, "Fst", 0, "Fst Vs. Iterations");
		} else {
			for (int i = 0; i < dataset; i++) {
				g[i] = new Plot(proc, "Fst", i, "Fst" + (i + 1)
						+ " Vs. Iterations");
			}
		}

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));

		for (int i = 0; i < dataset; i++) {
			p.add(g[i]);
			g[i].start();

		}

		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(new Dimension(550, 500));
		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Structure Plotting: Fst");
		target.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				for (int i = 0; i < g.length; i++) {
					if (g[i] != null) {
						g[i].stop();
					}
					target.setVisible(false);
				}

			}
		});

		target.setLocation(300, 200);
		target.getContentPane().add(sp);
		target.pack();
		target.setVisible(true);
	}

	public void plotHist(double[][] data, String title) {

		this.dataset = data.length;

		HistPlot[] hist = new HistPlot[dataset];

		if (dataset == 1) {
			hist[0] = new HistPlot(data[0], "Fst",
					"Histogram of Distribution of Fst");
		} else {
			for (int i = 0; i < dataset; i++) {
				hist[i] = new HistPlot(data[i], "Fst",
						"Histogram of Distribution of Fst" + (i + 1));
			}
		}

		JPanel p = new JPanel();
		p.setBackground(Color.white);
		p.setLayout(new GridLayout(0, 1));

		sth = new JASHist[dataset];

		for (int i = 0; i < dataset; i++) {
			JPanel ph = new JPanel();
			ph.setBackground(Color.white);
			sth[i] = hist[i].draw();
			ph.setLayout(new BorderLayout());
			ph.add(sth[i], BorderLayout.CENTER);
			JButton saveButton = new JButton("Save image");
			saveButton.addActionListener(hlistener);
			saveButton.setActionCommand("" + i);
			JPanel bPane = new JPanel();
			bPane.add(saveButton);
			ph.add(bPane, BorderLayout.SOUTH);
			p.add(ph);

		}

		JScrollPane sp = new JScrollPane(p);
		if (dataset > 1) {
			sp.setPreferredSize(new Dimension(500, 600));
		} else {
			sp.setPreferredSize(new Dimension(500, 350));
		}

		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Histogram of Distribution of Fst - " + title);
		target.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				target.setVisible(false);
			}
		});

		target.setLocation(300, 200);
		target.getContentPane().add(sp);
		target.pack();
		target.setVisible(true);
	}

	protected void saveCurveImage(int i) {

		String filename = "";
		// ask for file name/dir to save image
		JFileChooser fc = new JFileChooser();
		GeneralFileFilter jpgFilter = new GeneralFileFilter("jpg",
				"JPEG image files");
		fc.addChoosableFileFilter(jpgFilter);
		int returnVal = fc.showDialog(target, "Save");

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			String fname = file.getAbsolutePath();
			if (!fname.endsWith(".jpg") && !fname.endsWith(".jpeg")) {
				file = new File(new String(fname + ".jpg"));
			}
			// check if file exists
			if (file.exists()) {
				Object[] options = { "Yes", "No " };

				int n = JOptionPane
						.showOptionDialog(target, "Target file "
								+ file.getName()
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
						"Can not write image file: permission denied", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			filename = file.getAbsolutePath();

		}

		Rectangle rect = stg[i].getBounds();
		BufferedImage image = new BufferedImage(rect.width, rect.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setBackground(Color.white);
		g.setPaint(Color.white);
		g.fill(rect);
		(stg[i].getComponent(0)).paint(g);

		try {
			File f = new File(filename);
			// JimiRasterImage jrf = Jimi.createRasterImage(image.getSource());
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

	}

	protected void saveHistImage(int i) {
		String filename = "";
		// ask for file name/dir to save image
		JFileChooser fc = new JFileChooser();
		GeneralFileFilter jpgFilter = new GeneralFileFilter("jpg",
				"JPEG image files");
		fc.addChoosableFileFilter(jpgFilter);
		int returnVal = fc.showDialog(target, "Save");

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			String fname = file.getAbsolutePath();
			if (!fname.endsWith(".jpg") && !fname.endsWith(".jpeg")) {
				file = new File(new String(fname + ".jpg"));
			}
			// check if file exists
			if (file.exists()) {
				Object[] options = { "Yes", "No " };

				int n = JOptionPane
						.showOptionDialog(target, "Target file "
								+ file.getName()
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
						"Can not write image file: permission denied", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			filename = file.getAbsolutePath();

		}

		Rectangle rect = sth[i].getBounds();
		BufferedImage image = new BufferedImage(rect.width, rect.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setBackground(Color.white);
		sth[i].paint(g);

		try {
			File f = new File(filename);
			// JimiRasterImage jrf = Jimi.createRasterImage(image.getSource());
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

	}

}
