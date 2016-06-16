
abstract public class Piece {
	private Position pos;
	private int health, maxHealth;
	Piece(Position pos){
		this.pos = pos;
	}
	
	public Position getPos(){
		return pos;
	}
	
	public void setX(short x){
		pos.setX(x);
	}
	
	public void setY(short y){
		pos.setY(y);
	}
	
	public void setHealth(int health){
		this.health = health;
	}
	
	public int getHealth(){
		return health;
	}
	
	public void setMaxHealth(int maxHealth){
		this.maxHealth = maxHealth;
	}
	
	public int getMaxHealth(){
		return maxHealth;
	}
	
	public void setPos(Position p){
		pos.setX(p.getX());
		pos.setY(p.getY());
	}
}
