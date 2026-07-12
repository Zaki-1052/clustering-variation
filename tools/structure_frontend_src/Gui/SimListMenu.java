package Gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import Controller.StructureApp;

public class SimListMenu implements ActionListener {

	private JMenu listMenu;
	private Vector<JCheckBoxMenuItem> simVector;
	private ButtonGroup buttonGroup;

	// The Controller
	private StructureApp app;

	public SimListMenu(StructureApp app) {
		this.app = app;
		listMenu = new JMenu("Parameter Set List");
	}

	public JMenu getMenu() {
		return listMenu;
	}

	// init the list menu with single "empty" disabled item
	public void initMenu() {
		JMenuItem emptyItem = new JMenuItem(" (Empty) ");
		emptyItem.setEnabled(false);
		listMenu.add(emptyItem);
	}

	// restore the list menu to the init state
	public void emptyMenu() {
		listMenu.removeAll();
		simVector = null;
		buttonGroup = null;
		initMenu();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();
	}

	public void loadMenu(String[] sims) {
		if (sims.length == 0) {
			return;
		}

		for (int i = 0; i < sims.length; i++) {
			appendMenuItem(sims[i], false);
		}
	}

	public void appendMenuItem(String simName, boolean active) {

		if (simVector == null) {
			simVector = new Vector<JCheckBoxMenuItem>();
		}
		if (buttonGroup == null) {
			buttonGroup = new ButtonGroup();
			listMenu.removeAll();
		}

		JCheckBoxMenuItem newSim = new JCheckBoxMenuItem(simName);
		newSim.setActionCommand(simName);
		newSim.addActionListener(this);

		// add to the collection
		simVector.add(newSim);
		buttonGroup.add(newSim);
		listMenu.add(newSim);

		// set the selection mode
		newSim.setSelected(active);
		return;
	}

	public void updateMenu(String simName) {
		for (int i = 0; i < simVector.size(); i++) {
			JCheckBoxMenuItem mi = simVector.elementAt(i);
			if (mi.getActionCommand().equals(simName)) {
				mi.setSelected(true);
				return;
			}
		}

	}

	public void removeMenuItem(String simName) {
		for (int i = 0; i < simVector.size(); i++) {
			JCheckBoxMenuItem mi = simVector.elementAt(i);
			if (mi.getActionCommand().equals(simName)) {
				listMenu.remove(mi);
				return;
			}
		}
	}

	public void setActiveFlag(String simName, boolean b) {
		for (int i = 0; i < simVector.size(); i++) {
			JCheckBoxMenuItem mi = simVector.elementAt(i);
			if (mi.getActionCommand().equals(simName)) {
				mi.setSelected(b);
				return;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String simName = e.getActionCommand();
		app.loadSimObj(simName);
		return;
	}

}
