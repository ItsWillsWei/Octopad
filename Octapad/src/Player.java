import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

public class Player {

	private InputStream in; // Shouldn't this be in the thread???
	private OutputStream out;
	private Position pos;
	private Color c;
	private boolean alive;

	public Player(Position p) {
		pos = p;
		c = new Color((int) (Math.random() * 256), (int) (Math.random() * 256),
				(int) (Math.random() * 256));
	}

	public Position getPos() {
		return pos;
	}

	public Color getColour() {
		return c;
	}

	public boolean alive() {
		return alive;
	}

}
