import java.awt.Color;


public class Block extends Piece{
	private Color color;
	Block(Position p, int health, Color c){
		super(p);
		super.setHealth(health);
		color = c;
	}
	public Color getColor(){
		return color;
	}
}
