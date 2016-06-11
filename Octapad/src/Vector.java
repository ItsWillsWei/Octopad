public class Vector {
	private double x;
	private double y;

	Vector(double c, double b) {
		x = c;
		y = b;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double b) {
		x = b;
	}

	public void setY(double a) {
		y = a;
	}
	
	double getMagnitude(){
		return Math.sqrt(x*x+y*y);
	}
}
