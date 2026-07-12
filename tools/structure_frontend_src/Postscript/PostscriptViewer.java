package Postscript;

/*
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import interpreter.PAContext;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import Util.GeneralFileFilter;

//import javax.swing.border.*;
//import javax.swing.event.*;
//import Postscript.Controller.*;

/**
 * Render postscript files with the Java 2D(TM) api. This demo is not meant to
 * be a fully compliant postscript interpreter. The demo works with the provided
 * postscript files and simple postscript files like the examples from
 * "Postscript By Example". Most of the language control operators are
 * implemented and about 20% of the graphic operators are implemented.
 * 
 * @version @(#)PostscriptViewer.java 1.3 98/12/17
 * @author Uwe Hoffmann
 */
public class PostscriptViewer extends JApplet implements ActionListener,
		Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static String postscriptFiles[] = { "plotfile" };
	static JComboBox combo;
	static URL url;
	JPanel p1;
	Demo demo;
	private static Graphics2D g2;

	public void init() {

		p1 = new JPanel(new BorderLayout());
		EmptyBorder eb = new EmptyBorder(5, 20, 10, 20);
		p1.setBorder(eb);
		p1.setBackground(Color.gray);
		// combo = new JComboBox();
		// combo.setLightWeightPopupEnabled(false);
		/*
		 * for (int i = 0; i < postscriptFiles.length; i++) {
		 * combo.addItem(postscriptFiles[i]); }
		 */
		// combo.addActionListener(this);
		// p1.add("West", combo);

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

		JPanel p2 = new JPanel(new BorderLayout());
		eb = new EmptyBorder(20, 20, 5, 20);
		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		p2.setBorder(new CompoundBorder(eb, bb));
		p2.setBackground(Color.gray);
		p2.add(demo = new Demo());

		getContentPane().add("Center", p2);

		url = PostscriptViewer.class
				.getResource("../library/neighbor/plots/plotfile");

		JButton saveButton = new JButton("Save Image");
		saveButton.addActionListener(this);
		saveButton.setActionCommand("save");
		saveButton.setEnabled(true);
		p1.add("West", saveButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.setActionCommand("close");
		closeButton.setEnabled(true);
		p1.add("East", closeButton);

		// JLabel pname = new JLabel("Parameter set  ");
		// JLabel rname = new JLabel("  Result file     ");
		// JPanel pnamep = new JPanel();
		// pnamep.add(pname);
		// JPanel rnamep = new JPanel();
		// rnamep.add(rname);

	}

	/*
	 * public void addOpenButton() { JButton b = new JButton("Open...");
	 * b.addActionListener(this); p1.add("East", b); }
	 */

	/*
	 * public void addSaveImageButton() { JButton saveButton = new
	 * JButton("Save Image"); saveButton.setActionCommand("save");
	 * //b.addActionListener(this); p1.add("East", saveButton); }
	 */

	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();

		System.out.println(action);
		/*
		 * if (e.getSource() instanceof JButton) {
		 * 
		 * }
		 */

		if (action.equals("save")) {

			JFileChooser fc = new JFileChooser();
			GeneralFileFilter jpgFilter = new GeneralFileFilter("jpg",
					"JPEG image files");
			fc.addChoosableFileFilter(jpgFilter);

			// int returnVal = fc.showDialog(frame,"Save");
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
					/*
					 * int n=JOptionPane.showOptionDialog(frame,
					 * "Target file "+file
					 * .getName()+" already exists, Overwrite it?",
					 * "Save image", JOptionPane.YES_NO_OPTION,
					 * JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					 */

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

				// tplot.saveImage(file.getAbsolutePath());

				BufferedImage image = new BufferedImage(420, 380,
						BufferedImage.TYPE_INT_RGB);

				Graphics fg = image.getGraphics();
				this.paint(fg);

				try {
					File f = new File(file.getAbsolutePath());
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

		}

		if (action.equals("close")) {
			// this.dispose();
			// dispose();
		}
		/*
		 * else { String str = (String)
		 * PostscriptViewer.combo.getSelectedItem(); url =
		 * Demo.class.getResource("../library/neighbor/plots/" + str); }
		 */
		new Thread(this).start();

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

		public Demo() {
			setBackground(Color.white);
		}

		public void paint(Graphics g) {

			Dimension d = getSize();

			g2 = (Graphics2D) g;
			// Graphics2D g2 = (Graphics2D) g;

			System.out.println(d);
			// double s = Math.min(d.width, d.height) / 800.0;
			double s = Math.min(d.width, d.height) / 700.0;
			// double s = 1.0;

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

	// public static void main(String s[]) {
	/*
	 * static public void main(String[] args){
	 * 
	 * final PostscriptViewer demo = new PostscriptViewer(); demo.init();
	 * //demo.addOpenButton(); //demo.addSaveImageButton(); WindowListener l =
	 * new WindowAdapter() { public void windowClosing(WindowEvent e)
	 * {System.exit(0);} };
	 * 
	 * Frame f = new Frame("Neighbor Joining Tree"); f.addWindowListener(l);
	 * f.add("Center", demo); f.pack();
	 * 
	 * //int w = 390*2; //int h = 440*2;
	 * 
	 * int w = 400*2; int h = 400*2;
	 * 
	 * 
	 * f.setSize(new Dimension(w, h)); Dimension screenSize =
	 * Toolkit.getDefaultToolkit().getScreenSize();
	 * f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2); //the
	 * position of the window f.show();
	 * 
	 * }
	 */
}
