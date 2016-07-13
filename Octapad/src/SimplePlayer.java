import java.awt.Color;

public class SimplePlayer {

	private Position pos;
	private Color c;
	private int upgrade, angle;
	private double percentHealth;

	SimplePlayer(int x, int y, Color col, int upgrade, int angle, double ph) {
		pos = new Position(x, y);
		c = col;
		this.upgrade = upgrade;
		this.angle = angle;
		percentHealth = ph;
	}

	public double getPercentHealth() {
		return percentHealth;
	}
	
	public void setPH(double d){
		percentHealth=d;
	}

	public void setAngle(int i) {
		angle = i;
	}

	public void setPos(Position p) {
		pos = p;
	}

	public Position getPos() {
		return pos;
	}

	public Color getColor() {
		return c;
	}

	public int getUpgrade() {
		return upgrade;
	}

	public int getAngle() {
		return angle;
	}
}