package Triangle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

public class TriPlotFm extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JComboBox plist;
	JComboBox rlist;

	JComboBox v1list;
	JComboBox v2list;

	private JComboBox v1col;
	private JComboBox v2col;
	private JComboBox v3col;

	private JButton closeButton;
	private JButton saveButton;

	private TrianglePlotManager manager;

	private JPanel plotArea;
	private JPanel rightPane;

	private JPanel palettePane;
	private JScrollPane palette;

	public TriPlotFm(TrianglePlotManager manager) {

		this.manager = manager;
	}

	public void initState(String paramset, String filename) {

		JLabel pname = new JLabel("Parameter set  ");
		JLabel rname = new JLabel("  Result file     ");
		JPanel pnamep = new JPanel();
		pnamep.add(pname);
		JPanel rnamep = new JPanel();
		rnamep.add(rname);

		String[] ds1 = null;
		if (paramset != null) {
			ds1 = new String[1];
			ds1[0] = paramset;
		}

		String[] ds2 = { filename };

		if (ds1 != null) {
			plist = new JComboBox(ds1);
		} else {
			plist = new JComboBox();
		}
		plist.setEnabled(false);

		rlist = new JComboBox(ds2);
		rlist.setEnabled(false);

		JPanel plistp = new JPanel();
		plistp.add(plist);
		JPanel rlistp = new JPanel();
		rlistp.add(rlist);

		JPanel pp = new JPanel();
		pp.add(pnamep);
		pp.add(plistp);

		JPanel rp = new JPanel();
		rp.add(rnamep);
		rp.add(rlistp);

		JPanel upPane = new JPanel();
		// upPane.setLayout(new GridLayout(0,1));
		// upPane.add(elp);
		upPane.add(pp);
		upPane.add(rp);
		upPane.setPreferredSize(new Dimension(760, 75));
		upPane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JLabel l1 = new JLabel(" Bottom left ");
		JPanel l1p = new JPanel();
		l1p.add(l1);

		String[] vl1 = { "Select one" };
		v1list = new JComboBox(vl1);
		v1list.setActionCommand("bottomleft");
		v1list.addActionListener(manager);
		JPanel v1listp = new JPanel();
		v1listp.add(v1list);

		JPanel vp1 = new JPanel();
		vp1.add(l1p, BorderLayout.NORTH);
		vp1.add(v1listp, BorderLayout.CENTER);

		JLabel l2 = new JLabel("Bottom right");
		JPanel l2p = new JPanel();
		l2p.add(l2);

		String[] vl2 = { "Select one" };
		v2list = new JComboBox(vl2);
		v2list.setActionCommand("bottomright");
		v2list.addActionListener(manager);
		JPanel v2listp = new JPanel();
		v2listp.add(v2list);

		JPanel vp2 = new JPanel();

		vp2.add(l2p, BorderLayout.NORTH);
		vp2.add(v2listp, BorderLayout.CENTER);

		JLabel l3 = new JLabel("    Top    ");
		JPanel l3p = new JPanel();
		l3p.add(l3);

		String[] vl3 = { "All others" };
		v3col = new JComboBox(vl3);
		JPanel v3colp = new JPanel();
		v3colp.add(v3col);
		v3col.setEnabled(false);

		JPanel vp3 = new JPanel();
		vp3.setLayout(new BorderLayout());
		vp3.add(l3p, BorderLayout.NORTH);
		vp3.add(v3colp, BorderLayout.CENTER);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridLayout(0, 1));
		vp1.setPreferredSize(new Dimension(150, 120));
		vp2.setPreferredSize(new Dimension(150, 120));
		vp3.setPreferredSize(new Dimension(150, 120));
		leftPane.add(vp1);
		leftPane.add(vp2);
		leftPane.add(vp3);
		leftPane.setPreferredSize(new Dimension(170, 400));
		leftPane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		rightPane = new JPanel();
		rightPane.setPreferredSize(new Dimension(430, 400));
		rightPane.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		plotArea = new JPanel();
		plotArea.setBackground(Color.white);
		plotArea.setBorder(new LineBorder(Color.black, 2));
		plotArea.setPreferredSize(new Dimension(420, 380));
		rightPane.add(plotArea);

		palettePane = new JPanel();
		palettePane.setPreferredSize(new Dimension(250, 400));
		palettePane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JPanel middlePane = new JPanel();
		middlePane.add(palettePane);
		middlePane.add(rightPane);
		middlePane.add(leftPane);

		closeButton = new JButton(" close window");
		saveButton = new JButton(" save image  ");
		closeButton.setActionCommand("close");
		saveButton.setActionCommand("save");
		closeButton.addActionListener(manager);
		saveButton.addActionListener(manager);

		saveButton.setEnabled(false);

		JPanel cbp = new JPanel();
		cbp.add(saveButton);
		cbp.add(closeButton);

		JPanel bottomPane = new JPanel();
		bottomPane.add(cbp);
		bottomPane.setPreferredSize(new Dimension(780, 50));

		getContentPane().add(upPane, BorderLayout.NORTH);
		getContentPane().add(middlePane, BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);

	}

	public void initState(String[] pslist) {

		JLabel pname = new JLabel("Parameter set  ");
		JLabel rname = new JLabel("  Result file     ");
		JPanel pnamep = new JPanel();
		pnamep.add(pname);
		JPanel rnamep = new JPanel();
		rnamep.add(rname);

		String[] ds2 = { "Select one" };

		plist = new JComboBox(pslist);
		plist.setActionCommand("simlist");
		plist.addActionListener(manager);

		rlist = new JComboBox(ds2);
		rlist.setEnabled(false);
		rlist.setActionCommand("filelist");
		rlist.addActionListener(manager);

		JPanel plistp = new JPanel();
		plistp.add(plist);
		JPanel rlistp = new JPanel();
		rlistp.add(rlist);

		JPanel pp = new JPanel();
		pp.add(pnamep);
		pp.add(plistp);

		JPanel rp = new JPanel();
		rp.add(rnamep);
		rp.add(rlistp);

		JPanel upPane = new JPanel();
		// upPane.setLayout(new GridLayout(0,1));
		// upPane.add(elp);
		upPane.add(pp);
		upPane.add(rp);
		upPane.setPreferredSize(new Dimension(760, 75));
		upPane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JLabel l1 = new JLabel(" Bottom left ");
		JPanel l1p = new JPanel();
		l1p.add(l1);

		String[] vl1 = { "Select one" };
		v1list = new JComboBox(vl1);
		v1list.setActionCommand("bottomleft");
		v1list.addActionListener(manager);
		JPanel v1listp = new JPanel();
		v1listp.add(v1list);

		JPanel vp1 = new JPanel();
		vp1.add(l1p, BorderLayout.NORTH);
		vp1.add(v1listp, BorderLayout.CENTER);

		JLabel l2 = new JLabel("Bottom right");
		JPanel l2p = new JPanel();
		l2p.add(l2);

		String[] vl2 = { "Select one" };
		v2list = new JComboBox(vl2);
		v2list.setActionCommand("bottomright");
		v2list.addActionListener(manager);
		JPanel v2listp = new JPanel();
		v2listp.add(v2list);

		JPanel vp2 = new JPanel();

		vp2.add(l2p, BorderLayout.NORTH);
		vp2.add(v2listp, BorderLayout.CENTER);

		JLabel l3 = new JLabel("    Top    ");
		JPanel l3p = new JPanel();
		l3p.add(l3);

		String[] vl3 = { "All others" };
		v3col = new JComboBox(vl3);
		JPanel v3colp = new JPanel();
		v3colp.add(v3col);
		v3col.setEnabled(false);

		JPanel vp3 = new JPanel();
		vp3.setLayout(new BorderLayout());
		vp3.add(l3p, BorderLayout.NORTH);
		vp3.add(v3colp, BorderLayout.CENTER);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridLayout(0, 1));
		vp1.setPreferredSize(new Dimension(150, 120));
		vp2.setPreferredSize(new Dimension(150, 120));
		vp3.setPreferredSize(new Dimension(150, 120));
		leftPane.add(vp1);
		leftPane.add(vp2);
		leftPane.add(vp3);
		leftPane.setPreferredSize(new Dimension(170, 400));
		leftPane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		rightPane = new JPanel();
		rightPane.setPreferredSize(new Dimension(430, 400));
		rightPane.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		plotArea = new JPanel();
		plotArea.setBackground(Color.white);
		plotArea.setBorder(new LineBorder(Color.black, 2));
		plotArea.setPreferredSize(new Dimension(420, 380));
		rightPane.add(plotArea);

		palettePane = new JPanel();
		palettePane.setPreferredSize(new Dimension(250, 400));
		palettePane.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JPanel middlePane = new JPanel();
		middlePane.add(palettePane);
		middlePane.add(rightPane);
		middlePane.add(leftPane);

		closeButton = new JButton(" close window");
		saveButton = new JButton(" save image  ");
		closeButton.setActionCommand("close");
		saveButton.setActionCommand("save");
		closeButton.addActionListener(manager);
		saveButton.addActionListener(manager);

		saveButton.setEnabled(false);

		JPanel cbp = new JPanel();
		cbp.add(saveButton);
		cbp.add(closeButton);

		JPanel bottomPane = new JPanel();
		bottomPane.add(cbp);
		bottomPane.setPreferredSize(new Dimension(780, 50));

		getContentPane().add(upPane, BorderLayout.NORTH);
		getContentPane().add(middlePane, BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);

	}

	public void updatePlot(JPanel newplot) {
		if (plotArea != null) {
			rightPane.remove(plotArea);
		}
		plotArea = newplot;
		rightPane.add(plotArea);
		pack();
		saveButton.setEnabled(true);
	}

	public void updateGui(JScrollPane newpalette) {
		if (palette != null) {
			palettePane.remove(palette);
		}
		palettePane.add(newpalette);
		palette = newpalette;
		palette.setPreferredSize(new Dimension(250, 380));
		pack();

	}

	public void resetGui() {
		if (palette != null) {
			palettePane.remove(palette);
		}
		palette = null;
		pack();
		palettePane.repaint();
		v1list.setEnabled(false);
		v2list.setEnabled(false);
		saveButton.setEnabled(false);
	}
}
