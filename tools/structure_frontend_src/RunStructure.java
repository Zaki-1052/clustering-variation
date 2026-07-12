import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import Controller.StructureApp;

public class RunStructure {

	public static void main(String[] args) {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.getDefaults().put("InternalFrame.icon", "");
		} catch (Exception e) {
			// handle exception
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				StructureApp app = 	new StructureApp();
				app.initStructure();	
			}
		});
	}		
}
