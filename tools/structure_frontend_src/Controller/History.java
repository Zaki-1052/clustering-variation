package Controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import Util.InFile;

public class History implements ActionListener {

	private int maxRecord = 4;
	private Vector<File> projs;

	private JMenu listMenu;

	// the controller
	private StructureApp app;

	public History(StructureApp app) {

		this.app = app;
		File historyFile = new File(System.getProperty("user.home"),
				".structure");
		projs = new Vector<File>();
		if (historyFile.exists()) {
			loadProjVec(historyFile.getAbsolutePath());
		}
	}

	private void loadProjVec(String historyPath) {

		InFile infile = null;
		try {
			infile = new InFile(historyPath);
		} catch (FileNotFoundException e) {
			return;
		}

		String str = new String();
		// read file line by line
		while (true) {
			try {
				str = infile.readLine();
			} catch (Exception re) {
			}
			if (str == null) {
				return;
			}
			projs.add(new File(str));
		}

	}

	public JMenu getHistoryMenu() {

		listMenu = new JMenu("Recent projects");

		if (projs.size() == 0) {
			JMenuItem emptyItem = new JMenuItem(" (Empty) ");
			emptyItem.setEnabled(false);
			listMenu.add(emptyItem);
			return listMenu;
		}

		// else write out proj on the menu
		Vector<String> savedNames = new Vector<String>();
		for (int i = 0; i < projs.size(); i++) {
			String projname = (projs.elementAt(i)).getName();
			int count = 0;
			for (int j = 0; j < savedNames.size(); j++) {
				String tempname = savedNames.elementAt(j);
				if (tempname.equals(projname)) {
					count++;
				}
			}
			savedNames.addElement(projname);

			JMenuItem item = null;

			if (count == 0) {
				item = new JMenuItem(projname);
			} else {
				item = new JMenuItem(projname + "(" + (count + 1) + ")");
			}
			String fullpath = (projs.elementAt(i)).getAbsolutePath();
			item.setActionCommand(fullpath + ".spj");
			item.addActionListener(this);

			listMenu.add(item);
		}

		return listMenu;
	}

	public void updateHistory(String fullname, boolean updatemenu) {

		// check if it is a old one, put it to the first if yes
		for (int i = 0; i < projs.size(); i++) {
			String projname = projs.elementAt(i).getAbsolutePath();
			if (projname.equals(fullname)) {
				projs.removeElementAt(i);
			}
		}

		// else

		if (projs.size() == maxRecord) {
			projs.removeElementAt(maxRecord - 1);
		}

		projs.insertElementAt(new File(fullname), 0);

		// if need , visually update the menu
		if (updatemenu) {
			listMenu.removeAll();
			Vector<String> savedNames = new Vector<String>();
			for (int i = 0; i < projs.size(); i++) {
				String projname = (projs.elementAt(i)).getName();
				int count = 0;
				for (int j = 0; j < savedNames.size(); j++) {
					String tempname = savedNames.elementAt(j);
					if (tempname.equals(projname)) {
						count++;
					}
				}
				savedNames.addElement(projname);

				JMenuItem item = null;
				if (count == 0) {
					item = new JMenuItem(projname);
				} else {
					item = new JMenuItem(projname + "(" + (count + 1) + ")");
				}
				String fullpath = (projs.elementAt(i))
						.getAbsolutePath();
				item.setActionCommand(fullpath + ".spj");
				item.addActionListener(this);

				listMenu.add(item);
			}
		}
	}

	public void writeHistoryFile() {
		File historyFile = new File(System.getProperty("user.home"),
				".structure");
		PrintStream out = OpenOutputFile(historyFile);
		for (int i = 0; i < projs.size(); i++) {
			out.println(projs.elementAt(i).getAbsolutePath());
		}
		out.close();
	}

	public void actionPerformed(ActionEvent e) {

		String projName = e.getActionCommand();
		/*
		 * for(int i=0;i<projs.size();i++){
		 * 
		 * File projDir = (File)projs.elementAt(i);
		 * if(projName.equals(projDir.getName())){ File spjFile = new
		 * File(projDir.getAbsolutePath()+".spj");
		 * app.openProj(spjFile.getAbsolutePath()); return; } }
		 */
		app.openProj(projName);

	}

	private static PrintStream OpenOutputFile(File file) {

		PrintStream out;
		try { // try to open output file as well...
			out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("**Error: unable to open output file.");
			out = null;
		}// try-catch
		catch (SecurityException e) {
			System.err.println("**Error: no permission to write output file.");
			out = null;
		}// try-catch


		return out;
	}

}
