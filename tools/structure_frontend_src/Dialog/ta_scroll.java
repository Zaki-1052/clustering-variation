package Dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class ta_scroll implements ActionListener {
	private JFrame f; // Main frame
	private JFrame f2;

	private JTextArea ta; // Text area
	private JTextArea ta2;

	private JScrollPane sbrText; // Scroll pane for text area
	private JScrollPane sbrText2; // Scroll pane for text area

	private JButton btnLicense; // Quit Program
	private String copyright;

	JPanel p1;
	JPanel p2;

	JPanel p3;
	JPanel p4;

	BufferedReader reader;

	public ta_scroll() { // Constructor
		String license = "";

		String noteFile = "library/neighbor/NOTE";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(noteFile));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			license = stringBuilder.toString();
		} catch (IOException e) {
			System.out.println(e);
		}

		String copyrightFile = "library/neighbor/COPYRIGHT";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					copyrightFile));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			copyright = stringBuilder.toString();
		} catch (IOException e) {
			System.out.println(e);
		}

		p1 = new JPanel(new FlowLayout());
		p2 = new JPanel(new BorderLayout());
		p3 = new JPanel(new BorderLayout());

		BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
		EmptyBorder eb = new EmptyBorder(20, 20, 5, 20);

		p2.setBorder(new CompoundBorder(eb, bb));
		p3.setBorder(new CompoundBorder(eb, bb));

		p3.setBackground(Color.gray);
		p2.setBackground(Color.gray);
		p1.setBackground(Color.gray);

		f = new JFrame("Information and credit");

		f2 = new JFrame("Copyright Notice for PHYLIP");

		f.setLocation(200, 200);

		f2.setLocation(250, 250);

		Container content = f.getContentPane();
		Container content2 = f2.getContentPane();

		content.setLayout(new BorderLayout());
		content2.setLayout(new BorderLayout());

		content.add("Center", p1);
		content.add("North", p2);

		content2.add("Center", p3);

		// Create Scrolling Text Area in Swing
		ta = new JTextArea(license, 18, 53);
		ta.setLineWrap(true);
		ta.setEditable(false);

		ta2 = new JTextArea(copyright, 18, 53);
		ta2.setLineWrap(true);
		ta2.setEditable(false);

		sbrText = new JScrollPane(ta);
		sbrText
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		sbrText2 = new JScrollPane(ta2);
		sbrText2
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		new JButton("Close");
		btnLicense = new JButton("License");

		btnLicense.setActionCommand("license");
		btnLicense.addActionListener(this);
		btnLicense.setEnabled(true);

		p1.add(btnLicense, BorderLayout.CENTER);
		p2.add(sbrText, BorderLayout.CENTER);

		p3.add(sbrText2, BorderLayout.CENTER);

	}

	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();

		// System.out.println(action);

		if (action.equals("license")) {
			f2.setVisible(true);
		}
	}

	public void launchFrame() { // Create Layout
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.pack(); // Adjusts frame to size of components
		f.setVisible(true);

		f2.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f2.pack(); // Adjusts frame to size of components
		f2.setVisible(false);
	}

	public static void main(String args[]) {
		ta_scroll gui = new ta_scroll();
		gui.launchFrame();
	}
}
