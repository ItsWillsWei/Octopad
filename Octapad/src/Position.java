public class Position {
	private short x;
	private short y;

	/**
	 * Constructor
	 * @param x the x co-ordinate
	 * @param y the y co-ordinate
	 */
	Position(short x, short y) {
		this.x = x;
		this.y = y;
	}
	
	Position(int x, int y){
		this.x = (short)x;
		this.y = (short)y;
	}

	/**
	 * 
	 * @return x value
	 */
	public short getX() {
		return x;
	}

	/**
	 * 
	 * @return y value
	 */
	public short getY() {
		return y;
	}

	public void setY(short a) {
		y = a;
	}

	public void setX(short a) {
		x = a;
	}
}
