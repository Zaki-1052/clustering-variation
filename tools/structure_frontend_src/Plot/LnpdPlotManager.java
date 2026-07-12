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

public class LnpdPlotManager {

	private JFrame target;
	private int dataset;
	private AppProc proc;
	Plot stg;
	JASHist sth;
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				saveCurveImage();
			} catch (Exception exception) {
			}
		}
	};

	private ActionListener hlistener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				saveHistImage();
			} catch (Exception exception) {
			}
		}
	};

	public LnpdPlotManager() {
	}

	public void plot(double[][] plotdata, String title) {

		stg = new Plot(plotdata[0], "LnPD", "Ln P(D) Vs. Iterations");
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));

		JPanel gPane = new JPanel();
		gPane.setLayout(new BorderLayout());
		gPane.add(stg, BorderLayout.CENTER);
		JButton saveButton = new JButton("Save image");
		saveButton.addActionListener(listener);
		JPanel bPane = new JPanel();
		bPane.add(saveButton);
		gPane.add(bPane, BorderLayout.SOUTH);
		p.add(gPane);

		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(new Dimension(500, 350));

		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Structure Plotting: Ln P(D) - " + title);
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

	public void plot(AppProc proc) {

		this.proc = proc;

		final Plot g = new Plot(proc, "LnPD", 0, "Ln P(D) Vs. Iterations");
		g.start();
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));
		p.add(g);

		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(new Dimension(500, 300));
		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Structure Plotting: Ln P(D)");
		target.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				g.stop();
				target.setVisible(false);
			}
		});

		target.setLocation(300, 200);
		target.getContentPane().add(sp);
		target.pack();
		target.setVisible(true);
	}

	public void plotHist(double[][] data, String title) {

		HistPlot hist = new HistPlot(data[0], "Ln P(D)", "Histogram");

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 1));
		p.setBackground(Color.white);

		JPanel ph = new JPanel();
		ph.setBackground(Color.white);
		sth = hist.draw();
		ph.setLayout(new BorderLayout());
		ph.add(sth, BorderLayout.CENTER);
		JButton saveButton = new JButton("Save image");
		saveButton.addActionListener(hlistener);
		JPanel bPane = new JPanel();
		bPane.add(saveButton);
		ph.add(bPane, BorderLayout.SOUTH);
		ph.setPreferredSize(new Dimension(550, 350));
		p.add(ph);

		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(new Dimension(550, 360));

		if (target != null) {
			target.dispose();
		}
		target = new JFrame("Histogram of distribution of Ln P(D) - " + title);
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

	protected void saveCurveImage() {
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

		Rectangle rect = stg.getBounds();
		BufferedImage image = new BufferedImage(rect.width, rect.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setPaint(Color.white);
		g.fill(rect);
		(stg.getComponent(0)).paint(g);

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

	protected void saveHistImage() {
		String filename = "";
		// ask for file name/dir to save image
		JFileChooser fc = new JFileChooser();
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

		Rectangle rect = sth.getBounds();
		BufferedImage image = new BufferedImage(rect.width, rect.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setBackground(Color.white);
		sth.paint(g);

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
