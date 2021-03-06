public class Bullet extends Piece {
	private Vector truePos;
	private int id;
	private boolean modifying, active=true;
	private Vector velocity;
	private long time;

	Bullet(Position p, int id, Vector v) {
		super(p);
		this.id = id;
		
		// time = System.currentTimeMillis();
		velocity = v;
		truePos = new Vector(p.getX(), p.getY());
		time = System.currentTimeMillis();
		// Thread t = new Thread(new TimerThread());
		// t.start();
	}
	
	public double xChange(){
		return velocity.getX();
	}
	
	public double yChange(){
		return velocity.getY();
	}
	
	public long time(){
		return time;
	}

	public boolean alive() {
		return active;
	}

	public Position getPos() {
		if (modifying)
			return null;
		return super.getPos();
	}

	public int getID() {
		return id;
	}
//
//	public void moveUp(int pixels) {
//		pos = new Position(pos.getX(), pos.getY() - pixels);
//	}
//
//	public void moveDown(int pixels) {
//		pos = new Position(pos.getX(), pos.getY() - pixels);
//	}
}
