public class Position {
	private short x;
	private short y;

	Position(short x, short y) {
		this.x = x;
		this.y = y;
	}
	
	Position(int x, int y){
		this.x = (short)x;
		this.y = (short)y;
	}

	public short getX() {
		return x;
	}

	public short getY() {
		return y;
	}

	public void setY(short a) {
		y = a;
	}

	public void setX(short a) {
		x = a;
	}

	public void increaseX(short increment) {
		x += increment;
	}

	public void increaseY(short increment) {
		y += increment;
	}

}
