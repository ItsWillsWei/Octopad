import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Class that keeps track of an AI player
 *
 */
public class OfflineAI {
	// Game variables
	//private OfflinePlayer player;
	private int health = 100;
	private int points = 0;
	private boolean alive = true;
	private ArrayList<SimplePlayer> players = new ArrayList<SimplePlayer>();
	private ArrayList<Block> blocks = new ArrayList<Block>();

	// Physics Variables
	private Position pos;
	private int angle;
	private int upgrade;
	private int speed = 10;
	private Vector velocity;

	private static int id;
	private Color color;

	private static boolean shooting;
	private double bulletSpeed = 3;

	/**
	 * Creates a new pad that tracks the human players and tries to shoot them
	 */
	//, OfflinePlayer g
	public OfflineAI(Position p, short id) {
		// Connects to the server
		//player = g;
		pos = p;
		this.id = id;
		angle = (int) (Math.random() * 360);
		randomMovements();
		color = new Color((int) (Math.random() * 256),
				(int) (Math.random() * 256), (int) (Math.random() * 256));
	}

	public double getBulletSpeed() {
		return bulletSpeed;
	}

	public void hit(int damage) {
		health -= damage;
		if (health <= 0)
			alive = false;
	}

	public int getID() {
		return id;
	}

	public Position getPos() {
		return pos;
	}

	public Color getColour() {
		return color;
	}

	public boolean alive() {
		return alive;
	}

	// Starts the player client
	public static void main(String[] args) throws Exception {
		PlayerClient p = new PlayerClient();
		p.setVisible(true);
	}

	public boolean shooting() {
		return ((int) (Math.random() * 25) == 0 ? true : false);
	}

	public int getAngle() {
		return angle;
	}

	public int getUpgrade() {
		return upgrade;
	}

	public void setUpgrade(int upgrade) {
		this.upgrade = (short) upgrade;
	}

	public int getPoints() {
		return points;
	}

	public void setPlayers(ArrayList<SimplePlayer> g) {
		players = g;
	}

	public void setBlocks(ArrayList<Block> b) {
		blocks = (ArrayList<Block>) b.clone();
	}

	/**
	 * Sets the amount of points for the player
	 * 
	 * @param points
	 *            the new amount of points
	 */
	public void setPoints(short points) {
		this.points = points;
	}

	/**
	 * Sets the AI in an opposite direction at a "random" angle
	 */
	public void randomMovements() {
		if (pos.getX() < 0 || pos.getX() > 1000 || pos.getY() < 0
				|| pos.getY() > 700)
			angle = (angle + 180) % 360;
		double rad = angle / 180.0 * Math.PI;
		velocity = new Vector(speed * Math.cos(rad), speed * Math.sin(rad));
		pos.setX((short) (pos.getX() + velocity.getX()));
		pos.setY((short) (pos.getY() + velocity.getY()));
	}

	/**
	 * Determine the closest player to the AI
	 */
	public void closestPlayer() {
		if (!alive)
			System.exit(0);
		int min = Integer.MAX_VALUE;
		SimplePlayer close = null;
		// Check all the players on the server to determine a closest player
		for (short i = 0; i < players.size(); i++) {
			SimplePlayer p = players.get(i);
			// If the player and the AI are not at the same position
			if (!(p.getPos().getX() == pos.getX() && p.getPos().getY() == pos
					.getY())
			// If the AI is closer to the player than the current closest
			// distance
					&& (pos.getX() - p.getPos().getX())
							* (pos.getX() - p.getPos().getX())
							+ (pos.getY() - p.getPos().getY())
							* (pos.getY() - p.getPos().getY()) < min) {
				// Set the current player as the closest player
				min = (pos.getX() - p.getPos().getX())
						* (pos.getX() - p.getPos().getX())
						+ (pos.getY() - p.getPos().getY())
						* (pos.getY() - p.getPos().getY());
				close = p;
			}
		}
		// If there are no players, stop moving AI
		if (close == null) {
			System.out.println("not here");
			angle = 180;
			velocity = new Vector(0, 0);
		}
		// Pursue the closest player
		else {
			// The component distances to the closest player
			double y = pos.getY() - close.getPos().getY();
			double x = pos.getX() - close.getPos().getX();

			// Face the AI toward the player
			// if (x < 0.5 && x > -0.5) {
			// angle = (y <= 0 ? 90 : 270);
			// } else
			angle = (int) (180 / Math.PI * Math.atan(y / x));

			if (pos.getX() > close.getPos().getX()) {
				angle += 180;
			} else if (angle != 270 && pos.getY() > close.getPos().getY())
				angle += 360;
			double rad = angle / 180.0 * Math.PI;

			// Stop moving if there ar eno players
			if (players.size() == 0)
				velocity = new Vector(0, 0);
			velocity = new Vector(speed * Math.cos(rad), speed * Math.sin(rad));
			pos.setX((short) (pos.getX() + velocity.getX()));
			pos.setY((short) (pos.getY() + velocity.getY()));
		}
	}
}
