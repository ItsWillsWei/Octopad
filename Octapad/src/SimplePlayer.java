import java.awt.Color;


public class SimplePlayer {

	private Position pos;
	private Color c;
	private short upgrade, angle;

	SimplePlayer(Position p, Color col, short upgrade, short angle) {
		pos = p;
		c = col;
		this.upgrade = upgrade;
		this.angle = angle;
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