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

public class AlphaPlotManager {

	private JFrame target;
	private int dataset;
	@SuppressWarnings("unused")
	private AppProc proc;

	Plot[] stg;
	JASHist[] sth;
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

	public AlphaPlotManager() {
	}

	public void plot(double[][] plotdata, String title) {

		dataset = plotdata.length;
		stg = new Plot[dataset];

		if (dataset == 1) {
			stg[0] = new Plot(plotdata[0], "Alpha", "log(Alpha) Vs. Iterations");
		} else {

			for (int i = 0; i < dataset; i++) {
				stg[i] = new Plot(plotdata[i], "Alpha", "log(Alpha" + (i + 1)
						+ ") Vs. Iterations");
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
		if (dataset > 1) {
			sp.setPreferredSize(new Dimension(550, 500));
		} else {
			sp.setPreferredSize(new Dimension(550, 350));
		}
		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Structure Plotting: Alpha - " + title);
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

	public void plot(AppProc proc, boolean indAlpha, int dataset) {

		this.proc = proc;

		if (indAlpha) {
			this.dataset = dataset;
		} else {
			dataset = 1;
		}

		final Plot[] g = new Plot[dataset];
		if (!indAlpha) {
			g[0] = new Plot(proc, "Alpha", 0, "Log(Alpha) Vs. Iterations");
		} else {
			for (int i = 0; i < dataset; i++) {
				g[i] = new Plot(proc, "Alpha", i, "Log(Alpha" + (i + 1)
						+ ") Vs. Iterations");
			}
		}

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));

		for (int i = 0; i < dataset; i++) {
			p.add(g[i]);
			g[i].start();

		}

		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(new Dimension(500, 600));
		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Structure Plotting: Alpha");
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
			hist[0] = new HistPlot(data[0], "Alpha",
					"Histogram of Distribution of Alpha");
		} else {
			for (int i = 0; i < dataset; i++) {
				hist[i] = new HistPlot(data[i], "Alpha",
						"Histogram of Distribution of Alpha" + (i + 1));
			}
		}

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));
		p.setBackground(Color.white);

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
			sp.setPreferredSize(new Dimension(550, 500));
		} else {
			sp.setPreferredSize(new Dimension(550, 350));
		}
		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Histogram of Distribution of Alpha - " + title);
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
