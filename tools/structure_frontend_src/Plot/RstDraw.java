package Plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class RstDraw extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Axis geometry
	private int yHeight = 140;
	private int xLength;

	private int anchorLength = 6;

	// number of rows and number of bins per row
	private int numRows;
	private int numBins = 50;

	// if use a single row plotting
	private boolean single_row = true;

	// bin parameter
	private int bhFactor = yHeight;
	private int bWidth = 25;

	// row geometry
	private int rowHeight = 200;
	private int rowLength = 900;

	// data to draw
	private double[][] plotdata;
	private int[] plotlabels;
	private int[] popid;

	private boolean nopopinfo;
	private boolean labelpop = false;

	// colors
	private Color[] colors;

	// savable image dimension
	private int file_x;
	private int file_y;

	// draw in multi-row mode
	public RstDraw(double[][] data, int[] labels, int[] popid) {

		bWidth = ((620 / data[0].length) > 2) ? 620 / (data[0].length) : 2;
		setPreferredSize(new Dimension(bWidth * data[0].length + 100, rowHeight));
		xLength = bWidth * data[0].length + 20;
		setBackground(Color.white);

		plotdata = data;
		if (colors == null) {
			set_color(plotdata.length);
		}

		plotlabels = labels;
		this.popid = popid;
		if (popid == null) {
			nopopinfo = true;
		}
		setFont(new Font("TimesRoman", Font.PLAIN, 11));
		file_x = bWidth * data[0].length + 100;
		file_y = rowHeight;

	}

	public void reLoadData(double[][] data, int[] labels, int[] popid,
			boolean single_line) {

		this.single_row = single_line;

		if (single_line) {

			numRows = 1;
			bWidth = ((620 / data[0].length) > 2) ? 620 / (data[0].length) : 2;
			xLength = bWidth * data[0].length + 20;
			setPreferredSize(new Dimension(bWidth * data[0].length + 100,
					numRows * rowHeight));
			file_x = bWidth * data[0].length + 100;
			file_y = numRows * rowHeight;
		} else {

			bWidth = 25;
			double temp = (double) data[0].length / (double) numBins;
			numRows = (data[0].length / numBins) + 1;
			if (temp == Math.floor(temp)) {
				numRows -= 1;
			}
			setPreferredSize(new Dimension(bWidth * numBins + 100, numRows
					* rowHeight));
			xLength = bWidth * numBins + 20;
			file_x = bWidth * numBins + 100;
			file_y = numRows * rowHeight;
		}

		plotdata = data;
		if (colors == null) {
			set_color(plotdata.length);
		}

		plotlabels = labels;
		this.popid = popid;
		if (popid == null) {
			nopopinfo = true;
		}
		this.repaint();

	}

	private void set_color(int size) {

		// create color mapping
		colors = new Color[size];

		for (int i = 0; i < size; i++) {
			colors[i] = null;
		}

		for (int i = 0; i < size; i++) {
			if (i == 0) {
				colors[i] = new Color(1f, 0f, 0f);
			}
			if (i == 1) {
				colors[i] = new Color(0f, 1f, 0f);
			}
			if (i == 2) {
				colors[i] = new Color(0f, 0f, 1f);
			}
			if (i == 3) {
				colors[i] = new Color(1f, 1f, 0f);
			}
			if (i == 4) {
				colors[i] = new Color(1f, 0f, 1f);
			}
			if (i == 5) {
				colors[i] = new Color(0f, 1f, 1f);
			}
			if (i == 6) {
				colors[i] = new Color(0.98f, 0.60f, 0.0f);
			}
			if (i == 7) {
				colors[i] = new Color(0.5f, 0.25f, 0.37f);
			}
			if (i == 8) {
				colors[i] = new Color(0.98f, 0.6f, 0.37f);
			}
			if (i == 9) {
				colors[i] = new Color(0.1f, 0.6f, 0.75f);
			}
			if (i == 10) {
				colors[i] = new Color(0.2f, 0.6f, 0.5f);
			}
			if (i == 11) {
				colors[i] = new Color(0.7f, 0.3f, 0.75f);
			}
			if (i == 12) {
				colors[i] = new Color(0.9f, 0.2f, 0.75f);
			}
			if (i == 13) {
				colors[i] = new Color(0.35f, 0.1f, 0.75f);
			}
			if (i == 14) {
				colors[i] = new Color(0.15f, 0.89f, 0.30f);
			}
			if (i == 15) {
				colors[i] = new Color(0.6f, 0.2f, 0.75f);
			}
			if (i == 16) {
				colors[i] = new Color(0.35f, 0.9f, 0.4f);
			}
			if (i == 17) {
				colors[i] = new Color(0.12f, 0.38f, 0.15f);
			}
			if (i == 18) {
				colors[i] = new Color(0.9f, 0.09f, 0.23f);
			}
			if (i == 19) {
				colors[i] = new Color(0.59f, 0.23f, 0.11f);
			}
			if (i == 20) {
				colors[i] = new Color(0.38f, 0.2f, 0.95f);
			}
			if (i > 20) {
				colors[i] = randomize_color();
			}
		}
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

		for (int i = 0; i < colors.length; i++) {
			if (colors[i] == null) {
				return false;
			}

			float er = (colors[i].getRed()) / 255f;
			float eg = (colors[i].getGreen()) / 255f;
			float eb = (colors[i].getBlue()) / 255f;
			float diff = (er - nr) * (er - nr) + (eg - ng) * (eg - ng)
					+ (eb - nb) * (eb - nb);
			if (diff <= 0.02) {
				return true;
			}
		}
		return false;
	}

	public void paintComponent(Graphics g) {

		clear(g);
		Graphics2D g2d = (Graphics2D) g;

		for (int i = 0; i < plotdata.length; i++) {
			drawData(g2d, plotdata[i], colors[i]);
		}

		drawFrame(g2d);

		if (!single_row) {
			drawLabel(g2d, plotlabels);
		}

		if (labelpop) {
			labelPop(g2d);
		}

	}

	private void drawFrame(Graphics2D g2d) {

		String[] scale = { "1.00", "0.80", "0.60", "0.40", "0.20", "0.00" };

		if (single_row) {
			numRows = 1;
		}

		for (int i = 0; i < numRows; i++) {
			int offsetX = 40;
			int offsetY = i * rowHeight + 30;

			g2d.drawLine(offsetX, offsetY - 5, offsetX, offsetY + yHeight);
			g2d.drawLine(offsetX, offsetY + yHeight, offsetX + xLength, offsetY
					+ yHeight);

			for (int j = 0; j < 6; j++) {
				g2d.drawLine(offsetX - anchorLength, offsetY + j * yHeight / 5,
						offsetX, offsetY + j * yHeight / 5);
				g2d.drawString(scale[j], offsetX - anchorLength - 30, offsetY
						+ j * yHeight / 5 + 3);
			}

		}
	}

	private void drawData(Graphics2D g2d, double[] data, Color color) {

		for (int i = 0; i < data.length; i++) {
			int row = 0;
			int col = i;

			if (!single_row) {
				row = i / numBins;
				col = i % numBins;
			}

			int h = (int) (data[i] * bhFactor);

			// offsetX
			int x = 40 + col * bWidth;
			// offsetY
			int y = (row * rowHeight + 30) + yHeight - h;

			g2d.setColor(color);
			g2d.fillRect(x, y, bWidth, h);
			g2d.setColor(Color.black);

			if (!single_row) {
				g2d.drawRect(x, y, bWidth, h);
			}

		}
	}

	private void drawLabel(Graphics2D g2d, int[] labels) {
		for (int i = 0; i < labels.length; i++) {
			int row = i / numBins;
			int col = i % numBins;
			// offsetX
			int x = 40 + col * bWidth + bWidth / 2;
			// offsetY
			int y = (row * rowHeight + 30) + yHeight;
			g2d.drawLine(x, y, x, y + anchorLength);
			// additonal offset in y
			int yoff = 0;

			if (i % 2 == 1) {
				yoff = 2 * anchorLength;
			}
			if (nopopinfo) {
				g2d.drawString(new String("" + labels[i]), x - anchorLength, y
						+ 3 * anchorLength + yoff);
			} else {
				g2d.drawString(
						new String("" + labels[i] + "(" + popid[i] + ")"), x
								- anchorLength, y + 3 * anchorLength + yoff);
			}
		}
	}

	public void setPoplabel(boolean labelpop) {
		this.labelpop = labelpop;
	}

	private void labelPop(Graphics g2d) {

		if (nopopinfo) {
			return;
		}
		int anchor = 0;
		int offset = 0;
		for (int i = 0; i < popid.length; i++) {
			if ((i > 0 && popid[i] != popid[i - 1])
					|| (i != 0 && i == popid.length - 1)) {
				int midpoint = i - (i - 1 - anchor) / 2;
				int x = 40 + midpoint * bWidth + bWidth / 2;
				int y = 30 + yHeight;
				g2d.drawLine(x, y, x, y + anchorLength);
				g2d.drawString(new String("" + popid[i - 1]), x - anchorLength,
						y + 3 * anchorLength + (offset % 2) * anchorLength);
				anchor = i;
				offset += 1;
				// draw the border
				x = 40 + i * bWidth;
				if (i != popid.length - 1) {
					g2d.drawLine(x, y, x, y - yHeight);
				} else {
					g2d.drawLine(x + bWidth, y, x + bWidth, y - yHeight);
				}
			}

		}

	}

	protected void clear(Graphics g) {
		super.paintComponent(g);
	}

	// save the image as jpeg file
	protected void saveImage(String filename) {

		BufferedImage image = new BufferedImage(file_x, file_y,
				BufferedImage.TYPE_INT_RGB);

		Graphics fg = image.getGraphics();
		this.paint(fg);

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
