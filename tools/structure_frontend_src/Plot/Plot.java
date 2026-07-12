package Plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.io.File;
import java.net.URL;

import javax.swing.JPanel;

import Graph.Axis;
import Graph.DataSet;
import Graph.Graph2D;
import Graph.Markers;
import Proc.AppProc;

public class Plot extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Graph2D graph;
	Label title;
	DataSet data1 = new DataSet();
	Axis yaxis, xaxis;
	Image osi = null;
	Graphics osg = null;
	int iwidth = 0;
	int iheight = 0;
	Thread runner = null;

	// Data Source
	AppProc app;
	int target; // source data set number
	String cat; // The category

	/*
	 * * In milliseconds how often do we want to add a new data point.
	 */
	int period = 0;
	/*
	 * * Maximum number of points to display before scrolling the data
	 */
	int maximum = 75;

	public Plot(AppProc app, String cat, int target, String title) {

		this.app = app;
		this.target = target;
		this.cat = cat;
		int i;
		int j;

		/*
		 * * Get the passed parameters
		 */

		String mfile = "Util/marker.txt";
		period = 500;

		/*
		 * * Create the Graph instance and modify the default behaviour
		 */

		graph = new Graph2D();
		graph.zerocolor = new Color(0, 255, 0);
		graph.drawgrid = false;
		graph.borderTop = 50;
		graph.borderBottom = 50;
		graph.borderRight = 50;
		graph.setDataBackground(Color.yellow);
		graph.setGraphBackground(Color.white);
		graph.setSize(450, 250);

		/*
		 * * Load the Marker
		 */

		try {
			File f = new File(mfile);
			String url = "file:" + f.getAbsolutePath();
			URL markerURL = new URL(url);
			graph.setMarkers(new Markers(markerURL));
		} catch (Exception e) {

		}

		/*
		 * * Modify the default Data behaviour
		 */
		data1.linecolor = new Color(0, 0, 255);
		data1.marker = 7;
		data1.markerscale = 1.0;
		data1.markercolor = new Color(0, 0, 255);
		data1.linestyle = 1;
		/*
		 * * Setup the Axis. Attach it to the Graph2D instance, and attach the
		 * data* to it.
		 */
		yaxis = graph.createAxis(Axis.LEFT);
		yaxis.attachDataSet(data1);

		xaxis = graph.createAxis(Axis.BOTTOM);
		xaxis.attachDataSet(data1);
		xaxis.setTitleText(title);
		graph.attachDataSet(data1);

		add(graph);
		setPreferredSize(new Dimension(500, 300));

	}

	// plot static data
	public Plot(double[] data, String cat, String title) {

		if (data == null) {
			return;
		}

		String mfile = "marker.txt";

		/*
		 * * Create the Graph instance and modify the default behaviour
		 */

		Graph2D g = new Graph2D();
		g.zerocolor = new Color(0, 255, 0);
		g.drawgrid = false;
		g.borderTop = 50;
		g.borderBottom = 50;
		g.borderRight = 50;
		g.setDataBackground(Color.yellow);
		g.setGraphBackground(Color.white);
		g.setSize(450, 250);

		/*
		 * * Load the Marker
		 */

		try {
			File f = new File(mfile);
			String url = "file:" + f.getAbsolutePath();
			URL markerURL = new URL(url);
			g.setMarkers(new Markers(markerURL));
		} catch (Exception e) {

		}

		/*
		 * * Modify the default Data behaviour
		 */

		DataSet d1 = g.loadDataSet(data, data.length / 2);
		d1.linecolor = new Color(0, 0, 255);
		d1.marker = 7;
		d1.markerscale = 1.0;
		d1.markercolor = new Color(0, 0, 255);
		d1.linestyle = 1;
		/*
		 * * Setup the Axis. Attach it to the Graph2D instance, and attach the
		 * data* to it.
		 */
		Axis ya = g.createAxis(Axis.LEFT);
		ya.attachDataSet(d1);
		ya.setLabelColor(Color.black);
		ya.axiscolor = Color.black;

		if (cat.equals("Fst")) {
			d1.yaxis.maximum = 1.0;
			d1.yaxis.minimum = -0.1;
		} else if (cat.equals("Alpha")) {
			d1.yaxis.maximum = 2.0;
			d1.yaxis.minimum = -2.0;
		} else {
			double min, max;
			min = max = data[1];
			for (int i = 3; i < data.length; i += 2) {
				if (data[i] < min) {
					min = data[i];
				}
				if (data[i] > max) {
					max = data[i];
				}
			}

			d1.yaxis.maximum = max + 0.05 * Math.abs(max);
			d1.yaxis.minimum = min - 0.05 * Math.abs(max);

			if (cat.equals("Rec")) {
				d1.yaxis.minimum = 0.0;
				double d = 1;
				while (true) {
					if (d * 0.5 > max) {
						d1.yaxis.maximum = d * 0.5;
						break;
					}
					d += 1.0;
				}
			}
			// System.out.println(" "+d1.yaxis.maximum+"   "+ d1.yaxis.minimum);
		}

		Axis xa = g.createAxis(Axis.BOTTOM);
		xa.attachDataSet(d1);
		xa.setTitleText(title);
		xa.attachDataSet(d1);
		xa.setLabelColor(Color.black);
		xa.axiscolor = Color.black;
		add(g);
		setPreferredSize(new Dimension(500, 300));

	}

	public void start() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public void stop() {
		if (runner != null) {
			runner = null;
		}
	}

	public void run() {
		int i = 0;
		double data[] = new double[2];
		Graphics g;

		while (!app.isStarted()) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}

		boolean isDecided = false;
		boolean isFirstData = true;
		period = 0;

		int datacount = 0;
		if (target < 0) {
			return;
		}

		double max, min;
		max = min = 0;
		
		Thread thisThread = Thread.currentThread();
		while (!app.isKilled() && runner == thisThread) {
			// fast than data retrieval
			if (app.isEnd()) {
				period = 0;
			}

			data = app.getNewData(cat, target, datacount);

			if (data != null) {
				datacount += 2;
				if (app.isFast()) {
					period = (period - 1000) < 0 ? 0 : (period - 1000);
					isDecided = false;
				} else if (!isFirstData) {
					isDecided = true;
				}

				try {
					if (cat.equals("Alpha")) {
						data[1] = Math.log(data[1]) / Math.log(10);
					}
					data1.append(data, 1);
				} catch (Exception e) {
					System.out.println("Error appending Data!");
				}

				if (isFirstData) {
					isFirstData = false;
					max = min = data[1];
				}

				if (data[1] > max) {
					max = data[1];
				}
				if (data[1] < min) {
					min = data[1];
				}

				data1.yaxis.maximum = max + 0.01 * Math.abs(max);
				data1.yaxis.minimum = min - 0.01 * Math.abs(min);

				if (cat.equals("Fst")) {
					data1.yaxis.maximum = 1.0;
					data1.yaxis.minimum = -0.1;
				} else if (cat.equals("Alpha")) {
					data1.yaxis.maximum = 2.0;
					data1.yaxis.minimum = -2.0;
				} else if (cat.equals("Rec")) {
					data1.yaxis.maximum = 1.0;
					data1.yaxis.minimum = 0.0;
				}
				graph.repaint(500); // thread safe

			} else {

				if (!isDecided) {
					period += 500;
				}
			}

			try {
				Thread.sleep(period);
			} catch (Exception e) {
			}

		}

	}

}
