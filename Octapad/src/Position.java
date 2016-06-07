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
	
	public void increaseX(){
		x++;
	}
	public void setX(int value)
	{
		x = value;
	}
	
	public void increaseY(){
		y++;
	}
	public void setY(int value){
		y = value;
	}

}
