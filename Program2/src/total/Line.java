package total;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;

public class Line extends PaintingPrimitive{
	private Point p1;
	private Point p2;
	
	public Line(Point p1, Point p2, Color clr) {
		super(clr);
		this.p1 = p1;
		this.p2 = p2;
	}
	
	@Override
	protected void drawGeometry(Graphics g) {
		int x1 = (int)p1.getX();
		int y1 = (int)p1.getY();
		int x2 = (int)p2.getX();
		int y2 = (int)p2.getY();
		
		g.setColor(getColor());
		g.drawLine(x1,y1,x2,y2);
	}
}
