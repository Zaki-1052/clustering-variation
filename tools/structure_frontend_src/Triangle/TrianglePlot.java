package Triangle;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class TrianglePlot extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Triangle tra;
	private double[][] pointData;

	private int[] id;
	private boolean useIdInfo;
	private Hashtable<Integer, Color> colormap;
	private int leftindex;
	private int rightindex;

	public TrianglePlot() {
		tra = new Triangle(200, 220, 320);
		setPreferredSize(new Dimension(420, 380));
		setBackground(Color.white);
		setFont(new Font("TimesRoman", Font.ITALIC, 14));
		setBorder(new LineBorder(Color.black, 2));
	}

	public void resetData() {
		pointData = null;
		useIdInfo = false;
		colormap = null;
		id = null;
		leftindex = rightindex = -1;
	}

	public void loadData(double[][] pointData, int[] id, Hashtable<Integer, Color> colormap,
			int leftindex, int rightindex) {

		this.pointData = pointData;
		this.id = id;
		if (id != null) {
			useIdInfo = true;
		}
		this.colormap = colormap;
		this.leftindex = leftindex - 1;
		this.rightindex = rightindex - 1;

	}

	private AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(type, alpha));
	}

	public void paintComponent(Graphics g) {
		clear(g);
		Graphics2D g2d = (Graphics2D) g;

		if (pointData != null) {
			g2d.setComposite(makeComposite(1.0f));

			for (int i = 0; i < pointData[0].length; i++) {
				if (!useIdInfo) {
					drawPoint(g2d, pointData[rightindex][i],
							pointData[leftindex][i], colormap.get(new Integer(0)));
				} else {
					drawPoint(g2d, pointData[rightindex][i],
							pointData[leftindex][i], colormap.get(new Integer(id[i])));
				}

			}
			g2d.setColor(Color.black);
			g2d.drawString("Cluster " + (leftindex + 1), (int) (tra
					.getOffsetX() - 35), (int) (tra.getOffsetY() + 25));
			g2d.drawString("Cluster " + (rightindex + 1), (int) (tra
					.getOffsetX()
					+ tra.getLength() - 35), (int) (tra.getOffsetY() + 25));
			g2d.drawString("All others", (int) (tra.getOffsetX()
					+ tra.getLength() / 2 - 25), (int) (tra.getOffsetY()
					- 1.732 * tra.getLength() / 2 - 12));

		}

		// draw the triangle frame
		g2d.draw(new Triangle(200, 218, 340));
	}

	// super.paintComponent clears offscreen pixmap,
	// since we're using double buffering by default.

	private void drawPoint(Graphics2D g2d, double x, double y, Color color) {
		if (color == null) {
			g2d.setPaint(Color.black);
		} else {
			g2d.setPaint(color);
		}
		g2d.fill(getPoint(x, y));
	}

	// x denotes bottom-right corner and y denote bottom-left corner

	private Ellipse2D.Double getPoint(double x, double y) {

		double scale = tra.getLength() * 1.7321 / 2;

		double y0 = 1 - x - y;
		double x0 = x;
		double y1 = -y0 * scale + tra.getOffsetY();
		double x1 = ((2 * x0 + y0) / 1.7321) * scale + tra.getOffsetX();
		return new Ellipse2D.Double((int) (x1 - 4), (int) (y1 - 4), 8, 8);
	}

	protected void clear(Graphics g) {
		super.paintComponent(g);
	}

	protected void saveImage(String filename) {

		BufferedImage image = new BufferedImage(420, 380,
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
		// end testing code
	}

}
