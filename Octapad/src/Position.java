public class Position {
	private int x;
	private int y;

	Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setY(int a) {
		y = a;
	}

	public void setX(int a) {
		x = a;
	}

	public void increaseX() {
		x++;
	}

	public void increaseX(int increment) {
		x += increment;
	}

	public void increaseY() {
		y++;
	}

	public void increaseY(int increment) {
		y += increment;
	}

}
