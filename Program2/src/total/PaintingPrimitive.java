package total;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

public abstract class PaintingPrimitive implements Serializable {
	private Color objColor;
	
	public PaintingPrimitive(Color objColor) {
		this.objColor = objColor;
	}
	
	public final void draw(Graphics g) {
		g.setColor(objColor);
		drawGeometry(g);
	}
	
	public Color getColor() {
		return objColor;
	}
	
	protected abstract void drawGeometry(Graphics g);
}
