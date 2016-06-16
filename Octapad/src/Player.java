import java.awt.Color;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Player {
	//Server variables
	private Socket sock;
	private DataInputStream in;
	private DataOutputStream out;
	private int timeOut = 300;
	
	//Game variables
	private int id;
	private int inactiveCount = 0;
	private short upgrade = 0;
	private short points = 0;
	private Color color;
	private int health = 90;
	private boolean alive = true;
	
	//Physics Variables
	private Position pos;
	private int angle;
	private boolean shoot;
	private double bulletSpeed = 3;

	//Creates a new Player that allows the server to send and receive information
	public Player(Socket s, Position p, int id) {

		try {
			//Connects
			sock = s;
			pos = p;
			color = new Color((int) (Math.random() * 256),
					(int) (Math.random() * 256), (int) (Math.random() * 256));
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			this.id = id;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double getBulletSpeed() {
		return bulletSpeed;
	}

	/**
	 * Inflicts damage onto the player
	 * @param damage the amount of damage
	 */
	public void hit(int damage) {
		health -= damage;

		//Kill the player if they lose all their health
		if (health <= 0) {
			try {
				out.writeShort(6);
				this.alive = false;
			} catch (Exception e) {
			}
		} else {
			try {
				out.writeShort(3);
				out.writeShort(health);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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

	public short getPoints() {
		return points;
	}

	/**
	 * Sets the amount of points for the player
	 * @param points the new amount of points
	 */
	public void setPoints(short points) {
		this.points = points;
		try {
			out.writeShort(5);
			out.writeShort(points);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean shooting() {
		return shoot;
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

	/**
	 * Sends information to a player
	 * @param info the array of shorts
	 */
	public void sendCommand(short[] info) {
		try {
			for (short n : info) {
				out.writeShort(n);
				//System.out.println(n);
			}
			out.flush();
		} catch (IOException r) {
		}
	}

	/**
	 * Updates the player's immediate surroundings with bullets and players
	 * @param bullets the ArrayList of bullets
	 * @param players the arrayList of players
	 */
	public void updateSurroundings(ArrayList<Bullet> bullets, ArrayList<Player> players) {

		try {
			//in.available();
			long time = System.currentTimeMillis();
			out.writeShort(8);
			out.writeShort(bullets.size());
			//Check for bullets
			for (Bullet bull : bullets) {
				out.writeShort(bull.getPos().getX()
						+ (int) (bull.xChange() * (time - bull.time()) / 10));
				out.writeShort(bull.getPos().getY()
						+ (int) (bull.yChange() * (time - bull.time()) / 10));
			}

			out.writeShort(players.size());
			//Check for players
			for (Player player : players) {
				out.writeShort(player.getPos().getX());
				out.writeShort(player.getPos().getY());
				out.writeShort(player.getColour().getRed());
				out.writeShort(player.getColour().getGreen());
				out.writeShort(player.getColour().getBlue());
				out.writeShort(player.getUpgrade());
				out.writeShort(player.getAngle());

			}
			out.flush();
		} catch (Exception e) {
		}

	}

	/**
	 * Asking for an update on the player
	 */
	public void requestInfo() {

		long start = System.currentTimeMillis();
		try {
			//Requesting information
			out.writeShort(7);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		CommunicationThread communicateThread = new CommunicationThread();
		Thread t = new Thread(communicateThread);
		t.start();

		// Query for a move every 10ms until the timeout is reached or the move
		// is received
		while (!communicateThread.updated()
				&& System.currentTimeMillis() - start < timeOut) {

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Tell a player of timeout, otherwise update the position
		if (!communicateThread.updated()) {
			inactiveCount++;
			t.interrupt();
			System.out.println(System.currentTimeMillis()-start);
			System.out.println("taking long");
			if (inactiveCount >= 10) {
				System.out.println("Manual timeout");
				communicateThread.timeout();
				try {
					out.write(6);
					alive = false;
					out.flush();
				} catch (Exception e) {
				}
			}
		} else
			inactiveCount = 0;

	}

	/**
	 * Thread for receiving a move from the client. Can be timed out if the
	 * player takes too long.
	 */
	class CommunicationThread implements Runnable {
		private boolean timeout = false;
		private boolean infoReceived = false;

		public void run() {
			//Read in information as long as the player has not timed out
			try {
				long time = System.currentTimeMillis();
				short x = in.readShort();
				while (x == 0 && !timeout) {
					x = in.readShort();
				}
				if (!timeout) {

					short y = in.readShort();
					angle = in.readShort();
					upgrade = in.readShort();
					shoot = in.readBoolean();
					infoReceived = true;
					pos = new Position(x, y);
				}
				//System.out.println("Info taking :"+(System.currentTimeMillis()-time));
			} catch (Exception e) {

			}
		}

		
		/**
		 * Times out the player and kills the player
		 * @return whether the player information has been received by the server
		 */
		public boolean timeout() {
			timeout = true;
			alive = false;
			return infoReceived;
		}

		public boolean updated() {
			return infoReceived;
		}
	}
}
