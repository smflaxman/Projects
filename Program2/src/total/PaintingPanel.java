package total;

import java.util.ArrayList;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

public class PaintingPanel extends JPanel{
	private ArrayList<PaintingPrimitive> pps;
	
	public PaintingPanel() {
		super.setBackground(Color.WHITE);
		pps = new ArrayList<PaintingPrimitive>();
	}
	
	//Add incoming object to paint to list of painted objects
	public void addPrimitive(PaintingPrimitive pp) {
		pps.add(pp);
		paintComponent(getGraphics());
	}
	
	//Paint objects on painting panel
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(PaintingPrimitive pp : pps) {
			pp.drawGeometry(g);
		}
	}
}
