import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Class that keeps track of an AI player
 *
 */
public class AIClient {
	// Server Variables
	private static Socket sock;
	private static DataInputStream in;
	private static DataOutputStream out;
	private static String ip = "localhost";
	private static int port = 421;

	//Game variables
	private int health = 100;
	private int points = 0;
	private boolean alive = true;
	private ArrayList<Position> bullet = new ArrayList<Position>();
	private ArrayList<tempPlayer> players = new ArrayList<tempPlayer>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	
	//Physics Variables
	private Position pos;
	private int angle;
	private int upgrade;
	private boolean shoot = false;
	private int speed = 10;
	private Vector velocity;

	//Creates a new AIClient
	public static void main(String[] args) {
		new AIClient();
	}

	/**
	 * Creates a new pad that tracks the human players and tries to shoot them
	 */
	public AIClient() {
		// Connects to the server
		try {
			sock = new Socket(ip, port);
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			//Reads in the spawning position
			short x = in.readShort();
			short y = in.readShort();
			//Reads in the player's colour
			short r = in.readShort();
			short g = in.readShort();
			short b = in.readShort();
			pos = new Position(x, y);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Begin game
		Thread t = new Thread(new ServerThread());
		t.start();
		angle = (int) (Math.random() * 360);
		randomMovements();
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
	 * Keeps track of the server's input
	 */
	class ServerThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					//Reads in the server's command
					short curr = in.readShort();
					while (curr == 0) {
					}
					switch (curr) {
					// PLace object
					case 1:
						// showTime = false;
						int[][] move = new int[2][2];
						move[0][0] = (int) in.read();
						break;
					// Place player
					case 2:
						break;
					// Update health
					case 3:
						try {
							health = in.readShort();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					// Request upgrade
					case 4:
						break;
					// Awards points
					case 5:
						points += in.readShort();
						break;
					// Timed out or dead
					case 6:
						alive = false;
						break;
					// Requesting information
					case 7:
						out.writeShort(pos.getX());
						out.writeShort(pos.getY());
						out.writeShort(angle);
						out.writeShort(upgrade);
						out.writeBoolean((int) (Math.random() * 10) == 0 ? true
								: false);
						out.writeShort(points);
						out.flush();
						break;
					// Sending any new objects
					case 8:
						
						ArrayList<Position> currBullets = new ArrayList<Position>();
						// any bullets in the area
						short count = in.readShort();

						for (int i = 0; i < count; i++) {
							short x = in.readShort();
							short y = in.readShort();
							currBullets.add(new Position(x, y));
						}

						bullet = currBullets;
						count = in.readShort();

						ArrayList<tempPlayer> currPlayers = new ArrayList<tempPlayer>();
						// sending all players in your area
						for (int i = 0; i < count; i++) {
							short x = in.readShort();
							short y = in.readShort();
							short r = in.readShort();
							short g = in.readShort();
							short b = in.readShort();
							short upgrade = in.readShort();
							short angle = in.readShort();
							currPlayers.add(new tempPlayer(new Position(x, y),
									new Color(r, g, b), upgrade, angle));
						}
						players = currPlayers;
						closestPlayer();
						break;
					case 9:
						alive = false;
						break;
					case 10:
						// Retrieves the blocks' information
						if (in.readShort() == 1){
						short x = in.readShort();
						short y = in.readShort();
						short r = in.readShort();
						short g = in.readShort();
						short b = in.readShort();
						// Add one block
						blocks.add(new Block(new Position(x, y), 20,
								new Color(r, g, b)));
						}else
						{
							short x = in.readShort();
							short y = in.readShort();
							for(Block b: blocks){
								if(b.getPos().getX() == x && b.getPos().getY() == y)
									blocks.remove(b);
							}
						}
						break;
					}
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Determine the closest player to the AI
	 */
	private void closestPlayer() {
		int min = Integer.MAX_VALUE;
		tempPlayer close = null;
		//Check all the players on the server to determine a closest player
		for (short i = 0; i < players.size(); i++) {
			tempPlayer p = players.get(i);
			//If the player and the AI are not at the same position
			if (!(p.getPos().getX() == pos.getX() && p.getPos().getY() == pos
					.getY())
					//If the AI is closer to the player than the current closest distance
					&& (pos.getX() - p.getPos().getX())
							* (pos.getX() - p.getPos().getX())
							+ (pos.getY() - p.getPos().getY())
							* (pos.getY() - p.getPos().getY()) < min) {
				//Set the current player as the closest player
				min = (pos.getX() - p.getPos().getX())
						* (pos.getX() - p.getPos().getX())
						+ (pos.getY() - p.getPos().getY())
						* (pos.getY() - p.getPos().getY());
				close = p;
			}
		}
		//If there are no players, stop moving AI
		if (close == null) {
			angle = 180;
			velocity = new Vector(0, 0);
		}
		//Pursue the closest player
		else {
			//The component distances to the closest player
			double y = pos.getY() - close.getPos().getY();
			double x = pos.getX() - close.getPos().getX();
			
			//Face the AI toward the player
			if (x < 0.5 && x > -0.5) {
				angle = (y <= 0 ? 90 : 270);
			} else
				angle = (int) (180 / Math.PI * Math.atan(y / x));

			if (pos.getX() > close.getPos().getX()) {
				angle += 180;
			} else if (angle != 270 && pos.getY() > close.getPos().getY())
				angle += 360;
			double rad = angle / 180.0 * Math.PI;
			
			//Stop moving if there ar eno players
			if (players.size() == 0)
				velocity = new Vector(0, 0);
			velocity = new Vector(speed * Math.cos(rad), speed * Math.sin(rad));
			pos.setX((short) (pos.getX() + velocity.getX()));
			pos.setY((short) (pos.getY() + velocity.getY()));
		}
	}
}
