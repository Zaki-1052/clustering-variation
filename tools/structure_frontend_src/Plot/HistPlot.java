package Plot;

import jas.hist.JASHist;
import jas.hist.JASHist1DHistogramStyle;
import jas.hist.JASHistData;

import java.awt.Color;
import java.awt.Dimension;

public class HistPlot {

	private String cat;
	private double min;
	private double max;
	private String title;
	private double[] data;

	public HistPlot(double[] data, String cat, String title) {
		this.data = data;
		this.cat = cat;
		this.title = title;
	}

	double[] sortData() {

		int binnum = 10;
		if (data == null) {
			return null;
			// put the data to the array
		}

		for (int j = 0; j < data.length; j++) {
			if (j == 0) {
				min = max = data[0];
			}

			if (data[j] > max) {
				max = data[j];
			}
			if (data[j] < min) {
				min = data[j];
			}

		}

		double interval = (max - min) / binnum;
		// System.out.println("Max: "+max+" Min: "+min+" interval: "+interval);
		double[] bin = new double[binnum];

		for (int i = 0; i < binnum; i++) {
			bin[i] = 0;
		}

		for (int i = 0; i < data.length; i++) {
			int num = (int) ((data[i] - min) / interval);
			if (num == binnum) {
				num -= 1;
			}
			bin[num] += 1;
		}

		return bin;
	}

	public JASHist draw() {
		double[] source = sortData();
		if (source == null) {
			return null;
		}

		JASHist plot = new JASHist();

		JASHistData jd = plot.addData(new ArrayDataSource(source, title, min,
				max));
		((JASHist1DHistogramStyle) jd.getStyle()).setShowErrorBars(false);
		jd.show(true);
		plot.setPreferredSize(new Dimension(500, 300));
		plot.setTitle(title);
		plot.getXAxis().setLabel(cat);
		plot.setAllowUserInteraction(false);
		plot.setBackground(Color.white);

		return plot;
	}

}
