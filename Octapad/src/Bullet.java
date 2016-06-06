
public class Bullet extends Piece{
	Position pos;
	private int id;
	Bullet(Position p, int id)
	{
		this.id=id;
		pos=p;
	}
	
	public Position getPos(){
		return pos;
	}
	
	public int getID(){
		return id;
	}
}
