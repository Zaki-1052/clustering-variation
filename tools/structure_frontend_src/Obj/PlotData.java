package Obj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PlotData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[][] data;

	public PlotData(double[][] data) {
		this.data = data;
	}

	public PlotData() {
	}

	static public PlotData loadData(String filename) {

		File f = new File(filename);
		if (!f.exists()) {
			return null;
		}

		try {
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(f));

			PlotData pd = (PlotData) ois.readObject();
			ois.close();
			return pd;

		} catch (IOException e) {
			System.err.println(e);
			return null;
		} catch (ClassNotFoundException cnfe) {
			System.err.println(cnfe);
			return null;
		}
	}

	public void writeDataFile(String path, String filename) {

		// structure project file
		File f = new File(path, filename);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
		}
	}

	public double[][] getDataArray() {
		return data;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {

		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		in.defaultReadObject();
	}

}
