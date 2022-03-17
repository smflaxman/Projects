package total;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;

public class Circle extends PaintingPrimitive{
	private Point p1;
	private Point center;
	
	public Circle(Point center, Point p1, Color clr) {
		super(clr);
		this.p1 = p1;
		this.center = center;
	}

	@Override
	protected void drawGeometry(Graphics g) {
		g.setColor(getColor());
		int radius = (int) Math.abs(center.distance(p1));
		g.drawOval((int)p1.getX() - radius, (int)p1.getY() - radius, radius*2, radius*2);
	}
}
