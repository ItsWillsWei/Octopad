import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AIClient {
	// Relevant and important stuff
	private static Socket sock;
	private static DataInputStream in;
	private static DataOutputStream out;
	private ArrayList<Position> bullet = new ArrayList<Position>();
	private ArrayList<tempPlayer> players = new ArrayList<tempPlayer>();
	private Vector velocity;

	private int health = 100;
	private int points = 0;
	private boolean alive = true;
	private static String ip = "localhost";
	private static int port = 421;
	private Position pos;
	private int angle;
	private int upgrade;
	private boolean shoot = false;
	private int speed = 2;

	public static void main(String[] args) {
		new AIClient();
	}

	public AIClient() {
		// Connects to the server
		try {
			sock = new Socket(ip, port);
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			short x = in.readShort();
			short y = in.readShort();
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
		new Thread(new ServerThread()).start();
		angle = (int) (Math.random() * 360);
		randomMovements();
	}

	public void randomMovements() {
		if (pos.getX() < 0 || pos.getX() > 1000 || pos.getY() < 0
				|| pos.getY() > 700)
			angle = (angle + 180) % 360;
		double rad = angle / 360.0 * Math.PI;
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
				// Read in the server's command (if any)
				randomMovements();

				try {

					short curr = in.readShort();
					switch (curr) {
					// PLace object
					case 1:
						// showTime = false;
						int[][] move = new int[2][2];
						move[0][0] = (int) in.read();
						break;
					// Place player
					case 2:
						// colour = Integer.parseInt(command[1]);
						// GamePanel.this.repaint(0);
						break;
					// Update health
					case 3:
						try {
							health = in.readShort();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// GamePanel.this.repaint(0);
						break;
					// Request upgrade
					case 4:
						// upgrade option =true
						// GamePanel.this.repaint(0);
						break;
					// Awards points
					case 5:
						points += (int) in.read();
						// GamePanel.this.repaint(0);
						break;
					// Timed out or dead
					case 6:
						alive = false;
						// timedOut = true;
						// TODO end here but just testing right now
						System.exit(0);
						break;
					// Requesting information
					case 7:
						out.writeShort(pos.getX());
						// System.out.println(pos.getX()+ " "+pos.getY()+
						// " "+angle + " "+upgrade+ " "+shoot);
						out.writeShort(pos.getY());
						out.writeShort(angle);
						out.writeShort(upgrade);
						out.writeBoolean(shoot);
						out.flush();
						shoot = false;
						// GamePanel.this.repaint(0);
						break;
					// Sending any new objects
					case 8:
						// System.out.println("Receiving info");
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
						// System.out.println(count);

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
						// System.out.println("players.size"+players.size());
						players = currPlayers;
						break;
					case 9:
						alive = false;
						break;

					}
				} catch (IOException e) {
				}
			}
		}
	}
}
