public class Vector {
	private double x;
	private double y;

	/**
	 * Constructor
	 * @param c the horizontal component of the vector
	 * @param b the vertical component of the vector
	 */
	Vector(double c, double b) {
		x = c;
		y = b;
	}

	/**
	 * 
	 * @return the x value
	 */
	public double getX() {
		return x;
	}

	/**
	 * 
	 * @return the y value
	 */
	public double getY() {
		return y;
	}

	public void setX(double b) {
		x = b;
	}

	public void setY(double a) {
		y = a;
	}
	
	/**
	 * 
	 * @return magnitude of the vector
	 */
	double getMagnitude(){
		return Math.sqrt(x*x+y*y);
	}
}
