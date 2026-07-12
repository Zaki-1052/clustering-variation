package Gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import Controller.StructureApp;

public class ProjTree implements ActionListener {

	private JTree projTree;
	private Vector<DefaultMutableTreeNode> simFolder;
	private StructureApp app;

	private String selectedSim;

	private JPopupMenu popup;
	private JMenuItem popup_active;
	public ProjTree(StructureApp app) {
		this.app = app;

		popup = new JPopupMenu();
		popup_active = new JMenuItem("Activate");
		popup_active.addActionListener(this);
		popup.add(popup_active);

	}

	public JTree getTree() {
		return projTree;
	}

	public void initProjTree() {

		RootNode rootNode = new RootNode(new String(
				"Project (No Data Available)"));
		projTree = new JTree(rootNode);
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		projTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		projTree.putClientProperty("JTree.lineStyle", "Angled");
		treeModel.reload();
		projTree.addMouseListener(new PopupListener());
	}

	public void emptyProjTree() {

		RootNode rootNode = (RootNode) projTree.getModel().getRoot();
		projTree.scrollPathToVisible(new TreePath(rootNode.getPath()));
		rootNode.setUserObject(new String("Project (No Data Available)"));
		DefaultTreeModel treeModel = (DefaultTreeModel) projTree.getModel();
		for (int i = 3; i >= 0; i--) {
			treeModel.removeNodeFromParent((DefaultMutableTreeNode) projTree
					.getModel().getChild(rootNode, i));
		}
		treeModel.reload();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();
		simFolder = null;
	}

	public void loadProjTree(String title, String[] sims) {

		projTree.addTreeSelectionListener(new ProjTreeListener());

		// else create the tree
		DefaultMutableTreeNode settingNode = new DefaultMutableTreeNode(
				new String("Project Information"));
		DefaultMutableTreeNode dataNode = new DefaultMutableTreeNode(
				new String("Project Data"));
		DefaultMutableTreeNode summaryNode = new DefaultMutableTreeNode(
				new String("Simulation Summary"));
		RootNode simNode = new RootNode(new String("Parameter Sets"));

		for (int i = 0; i < sims.length; i++) {
			simNode.add(createSimNode(sims[i], false));
		}

		RootNode rootNode = (RootNode) projTree.getModel().getRoot();

		DefaultTreeModel treeModel = (DefaultTreeModel) projTree.getModel();
		rootNode.setUserObject(new String("Project - " + title));
		treeModel.insertNodeInto(dataNode, rootNode, rootNode.getChildCount());
		treeModel.insertNodeInto(settingNode, rootNode, rootNode
				.getChildCount());
		treeModel.insertNodeInto(summaryNode, rootNode, rootNode
				.getChildCount());
		treeModel.insertNodeInto(simNode, rootNode, rootNode.getChildCount());

		treeModel.reload();
		projTree.scrollPathToVisible(new TreePath(summaryNode.getPath()));

	}

	private DefaultMutableTreeNode createSimNode(String simName, boolean active) {
		String str = simName;
		if (active) {
			str += new String(" (Active) ");
		}

		if (simFolder == null) {
			simFolder = new Vector<DefaultMutableTreeNode>();
		}
		DefaultMutableTreeNode simRootNode = new DefaultMutableTreeNode(str);
		DefaultMutableTreeNode simSettingNode = new DefaultMutableTreeNode(
				new String("Settings"));
		RootNode simRstNode = new RootNode("Results");
		String[] rst = app.getRstFiles(simName);

		if (rst != null) {
			for (int i = 0; i < rst.length; i++) {
				simRstNode.add(new DefaultMutableTreeNode(rst[i]));
			}
		}

		simRootNode.add(simSettingNode);
		simRootNode.add(simRstNode);

		simFolder.add(simRootNode);

		return simRootNode;
	}

	// label all the sim node as "inactive"
	public void resetSimNodeFlag(String[] simList) {
		if (simFolder == null) {
			return; // There is nothing yet
		}

		for (int i = 0; i < simFolder.size(); i++) {
			DefaultMutableTreeNode node = simFolder
					.elementAt(i);
			node.setUserObject(simList[i]);
		}
	}

	public void appendProjTree(String newSimName, boolean active) {

		DefaultMutableTreeNode newNode = createSimNode(newSimName, active);
		RootNode rootNode = (RootNode) projTree.getModel().getRoot();
		RootNode simNode = (RootNode) projTree.getModel().getChild(rootNode, 3);
		DefaultTreeModel treeModel = (DefaultTreeModel) projTree.getModel();
		treeModel.insertNodeInto(newNode, simNode, simNode.getChildCount());

		// Make sure the user can see the lovely new node.
		treeModel.reload(simNode);
		projTree.scrollPathToVisible(new TreePath(newNode.getPath()));
	}

	public void appendRstNode(String simName, String nodeName, int K) {
		String simName2 = simName + " (Active)";
		for (int i = 0; i < simFolder.size(); i++) {

			DefaultMutableTreeNode node = simFolder
					.elementAt(i);
			String name = (String) node.getUserObject();
			if (name.equals(simName) || name.equals(simName2)) {
				DefaultTreeModel treeModel = (DefaultTreeModel) projTree
						.getModel();
				RootNode rstNode = (RootNode) treeModel.getChild(node, 1);
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
						nodeName + " ( K=" + K + " )");
				treeModel.insertNodeInto(newNode, rstNode, rstNode
						.getChildCount());
				treeModel.reload(rstNode);
				projTree.scrollPathToVisible(new TreePath(newNode.getPath()));
				return;
			}

		}
	}

	public void updateProjTree(String simName) {
		for (int i = 0; i < simFolder.size(); i++) {
			DefaultMutableTreeNode node = simFolder
					.elementAt(i);
			String name = (String) node.getUserObject();
			if (name.equals(simName)) {
				node.setUserObject(name + " (Active)");
				DefaultTreeModel treeModel = (DefaultTreeModel) projTree
						.getModel();
				treeModel.reload();
				projTree.scrollPathToVisible(new TreePath(node.getPath()));
				return;
			}
		}
	}

	public void removeSimNode(String simName) {
		for (int i = 0; i < simFolder.size(); i++) {
			DefaultMutableTreeNode node = simFolder
					.elementAt(i);
			String name = (String) node.getUserObject();
			if (name.equals(simName)) {
				DefaultTreeModel treeModel = (DefaultTreeModel) projTree
						.getModel();
				RootNode rootNode = (RootNode) projTree.getModel().getRoot();
				RootNode simNode = (RootNode) projTree.getModel().getChild(
						rootNode, 3);
				treeModel
						.removeNodeFromParent((DefaultMutableTreeNode) treeModel
								.getChild(simNode, i));
				treeModel.reload();
				simFolder.remove(i);
				return;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (selectedSim == null) {
			return;
		}
		app.loadSimObj(selectedSim);
		selectedSim = null;
		return;
	}

	class ProjTreeListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			// flush previous selection
			app.setCurrentSelection(null, null);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) projTree
					.getLastSelectedPathComponent();
			if (node == null) {
				return;
			}
			String nodeInfo = (String) node.getUserObject();
			if (nodeInfo.equals("Project Information")) {
				app.loadDataFrame("proj", null);
				return;

			}
			if (nodeInfo.equals("Project Data")) {
				app.loadDataFrame("data", null);
				return;
			}

			if (nodeInfo.equals("Settings")) {
				DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node
						.getParent();
				String simName = (String) pnode.getUserObject();
				int i = simName.indexOf('(');
				if (i >= 0) {
					simName = simName.substring(0, i - 1);
				}
				app.loadDataFrame("sim", simName);
				return;
			}

			if (nodeInfo.equals("Simulation Summary")) {
				app.loadSummary();
			}

			DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node
					.getParent();

			if (pnode == null) {
				return;
			}
			String name = (String) pnode.getUserObject();

			// load the result file

			if (name.equals("Results")) {
				DefaultMutableTreeNode simNode = (DefaultMutableTreeNode) pnode
						.getParent();
				String simName = (String) simNode.getUserObject();
				if (simName.endsWith(" (Active)")) {
					simName = simName.substring(0, simName.length() - 9);
				}

				// here is a fix on April 14th,2003 allow space character in sim
				// name

				nodeInfo = nodeInfo.substring(0, nodeInfo.indexOf(" ("));
				app.showResults(simName, nodeInfo);

				// System.out.println(simName+" "+nodeInfo);
			}

		}
	}

	class PopupListener extends MouseAdapter {

		public void mousePressed(MouseEvent e) {
			handle(e);
		}

		public void mouseReleased(MouseEvent e) {
			handle(e);
		}

		private void handle(MouseEvent e) {
			if (e.isPopupTrigger() || e.getClickCount() == 2) {
				int row = projTree.getRowForLocation(e.getX(), e.getY());
				if (row != -1) {
					TreePath path = projTree.getPathForRow(row);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) node
							.getParent();
					if (pnode != null) {
						String ptitle = (String) pnode.getUserObject();
						if (ptitle != null && ptitle.equals("Parameter Sets")) {
							String simName = (String) node.getUserObject();
							if (simName.endsWith(" (Active)")) {
								return;
							}
							selectedSim = simName;
							int state = app.getCurrState();
							if (state == 3 || state == 4) {
								popup_active.setEnabled(false);
							} else {
								popup_active.setEnabled(true);
							}
							if (e.isPopupTrigger()) {
								popup
										.show(e.getComponent(), e.getX(), e
												.getY());
							} else {
								if (selectedSim == null) {
									return;
								}
								app.loadSimObj(selectedSim);
								selectedSim = null;
								return;

							}

						}
					}
				}
			}
		}
	}

	class RootNode extends DefaultMutableTreeNode {

		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;

		public RootNode(Object o) {
			super(o);
		}

		public boolean isLeaf() {
			return false;
		}
	}

}
