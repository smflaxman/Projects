import java.util.ArrayList;
import java.awt.geom.Point2D;

public class ClosestPair {
	public static void main(String[] args) {
		ArrayList<Point2D> pnts = new ArrayList<Point2D>();

		for (int i = 0; i < 100; i++) {
			pnts.add(new Point2D.Double(Math.random() * 100, Math.random() * 100));
		}

		StdDraw.setCanvasSize();
		StdDraw.setCanvasSize(500, 500);
		StdDraw.setScale(-10, 110);
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.setPenRadius(.01);
		StdDraw.clear();
		StdDraw.show();

		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.setPenRadius(.015);

		ArrayList<Point2D> brut = brute(pnts);
		ArrayList<Point2D> recur = brute(pnts);

		for (Point2D pnt : pnts) {
			StdDraw.point(pnt.getX(), pnt.getY());
		}

		//proof of brute force implementation
		StdDraw.setPenColor(StdDraw.RED);
		for (Point2D pnt : brut) {
			StdDraw.point(pnt.getX(), pnt.getY());
		}

		//proof of recursive implementation
		StdDraw.setPenColor(StdDraw.BLUE);
		StdDraw.setPenRadius(0.005);
		for (Point2D pnt : recur) {
			StdDraw.point(pnt.getX(), pnt.getY());
		}
		
		System.out.println("Red dots represent brute force and blue dots represent recursive");
	}

	public static ArrayList<Point2D> brute(ArrayList<Point2D> pnts) {
		ArrayList<Point2D> ret = new ArrayList<Point2D>();
		ret.add(pnts.get(0));
		ret.add(pnts.get(1));
		double min = distance(ret.get(0), ret.get(1));
		for (Point2D pnt1 : pnts) {
			for (Point2D pnt2 : pnts) {
				if (!pnt1.equals(pnt2)) {
					double dist = distance(pnt1, pnt2);
					if (dist < min) {
						ret.set(0, pnt1);
						ret.set(1, pnt2);
						min = dist;
					}
				}
			}
		}
		return ret;
	}

	public static ArrayList<Point2D> spec(ArrayList<Point2D> pnts) {
		pnts = xsort(pnts);
		ArrayList<Point2D> delt = recur(pnts, xsort(pnts), ysort(pnts));
		return delt;
	}

	public static ArrayList<Point2D> recur(ArrayList<Point2D> pnts, ArrayList<Point2D> pntsX,
			ArrayList<Point2D> pntsY) {
		if (pnts.size() <= 3) {
			return brute(pnts);
		}
		ArrayList<Point2D> left = new ArrayList<Point2D>();
		for (int i = 0; i < pnts.size() / 2; i++) {
			left.add(pnts.get(i));
		}

		ArrayList<Point2D> right = new ArrayList<Point2D>();
		for (int i = pnts.size() / 2; i < pnts.size(); i++) {
			right.add(pnts.get(i));
		}

		ArrayList<Point2D> rdelt = recur(left, xsort(left), ysort(left));
		ArrayList<Point2D> ldelt = recur(right, xsort(right), ysort(right));

		if (distance(rdelt.get(0), rdelt.get(1)) < distance(ldelt.get(0), ldelt.get(1))) {
			return rdelt;
		} else {
			return ldelt;
		}

	}

	public static ArrayList<Point2D> ysort(ArrayList<Point2D> pnts) {
		for (int i = 1; i < pnts.size(); i++) {
			int b = i;
			while (pnts.get(i).getY() < pnts.get(b).getY() && b != 0) {
				b--;
			}
			Point2D temp = pnts.get(b);
			pnts.set(b, pnts.get(i));
			pnts.set(i, temp);
		}
		return pnts;
	}

	public static ArrayList<Point2D> xsort(ArrayList<Point2D> pnts) {
		for (int i = 1; i < pnts.size(); i++) {
			int b = i;
			while (pnts.get(i).getX() < pnts.get(b).getX() && b != 0) {
				b--;
			}
			Point2D temp = pnts.get(b);
			pnts.set(b, pnts.get(i));
			pnts.set(i, temp);
		}
		return pnts;
	}

	public static double distance(Point2D a, Point2D b) {
		double temp1 = Math.pow(a.getX() - b.getX(), 2);
		double temp2 = Math.pow(a.getY() - b.getY(), 2);
		return Math.sqrt(temp1 + temp2);
	}
}
