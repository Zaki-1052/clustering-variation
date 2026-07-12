package Tree;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MessageBoxes extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel;

	public MessageBoxes() {

		setTitle("Message Boxes");

		panel = new JPanel();
		panel.setLayout(new GridLayout(2, 2));

		JButton error = new JButton("Error");
		JButton warning = new JButton("Warning");
		JButton question = new JButton("Question");
		JButton information = new JButton("Information");

		error.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(panel, "Could not open file",
						"Error", JOptionPane.ERROR_MESSAGE);
			}

		});

		warning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(panel, "A deprecated call",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}

		});

		question.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(panel, "Are you sure to quit?",
						"Question", JOptionPane.QUESTION_MESSAGE);
			}

		});

		information.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(panel, "Download completed",
						"Question", JOptionPane.INFORMATION_MESSAGE);
			}

		});

		panel.add(error);
		panel.add(warning);
		panel.add(question);
		panel.add(information);

		add(panel);

		setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static void main(String[] args) {
		new MessageBoxes();
	}
}