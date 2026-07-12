package Triangle;

import java.awt.Graphics;
import java.awt.Polygon;

public class Triangle extends Polygon {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double offsetx, offsety;
	private double length;

	public Triangle(double x0, double y0, // center of the triangle
			double length // length of edges
	) {

		super();

		this.length = length + 4;

		double x1 = x0;
		double y1 = y0 - 0.5774 * length;
		double x2 = x0 - length / 2;
		double y2 = y0 + 0.2887 * length;
		double x3 = x0 + length / 2;
		double y3 = y2;

		offsetx = x2;
		offsety = y2;

		this.addPoint((int) x1, (int) y1);
		this.addPoint((int) x2, (int) y2);
		this.addPoint((int) x3, (int) y3);
	}

	public double getOffsetX() {
		return offsetx;
	}

	public double getOffsetY() {
		return offsety;
	}

	public double getLength() {
		return length;
	}

	// Instance methods:
	public void draw(Graphics g) {
		g.drawPolygon(this);
	}

	public void fill(Graphics g) {
		g.fillPolygon(this);
	}
}
