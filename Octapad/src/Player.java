import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Player {
	private int WIDTH = 1024;
	private int HEIGHT = 700;
	private int id;
	private int activeCount = 0;
	private Socket sock;
	private DataInputStream in;
	private DataOutputStream out;
	private Position pos;
	private Color color;
	private int health = 90;
	private boolean alive = true;
	private int timeOut = 500;
	private int angle;
	private boolean shoot;
	private short upgrade = 0;
	private ArrayList<Bullet> bullet;
	private ArrayList<Player> players;
	private ArrayList<Thread> threads = new ArrayList<Thread>();

	public Player(Socket s, Position p, int id) {

		try {
			sock = s;
			pos = p;
			color = new Color((int) (Math.random() * 256),
					(int) (Math.random() * 256), (int) (Math.random() * 256));
			System.out.println(color);
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			this.id = id;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void hit(int n) {
		health -= n;

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

	public boolean shooting() {
		return shoot;
	}

	public ArrayList<Player> getSurroundingPlayers() {
		return players;
	}

	public void setSurroundings(ArrayList<Player> p, ArrayList<Bullet> b) {
		players = p;
		bullet = b;
	}

	public ArrayList<Bullet> getSurroundingBullets() {
		return bullet;
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

	public void sendCommand(short[] info) {
		// System.out.println("send");

		// System.out.println("sending");
		try {
			for (short n : info) {
				out.writeShort(n);
				out.flush();
			}
		} catch (IOException r) {
		}
	}

	public void updateSurroundings() {
		try {
			out.writeShort(8);
			out.writeShort(bullet.size());
			long time = System.currentTimeMillis();

			for (Bullet bull : bullet) {
				out.writeShort(bull.getPos().getX()
						+ (int) (bull.xChange() * (time - bull.time()) / 10));
				out.writeShort(bull.getPos().getY()
						+ (int) (bull.yChange() * (time - bull.time()) / 10));
			}

			out.writeShort(players.size());
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

	public void updateSurroundings(ArrayList<Bullet> b, ArrayList<Player> p) {
		try {
			long time = System.currentTimeMillis();
			out.writeShort(8);
			out.writeShort(b.size());
			for (Bullet bull : b) {
				out.writeShort(bull.getPos().getX()
						+ (int) (bull.xChange() * (time - bull.time()) / 10));
				out.writeShort(bull.getPos().getY()
						+ (int) (bull.yChange() * (time - bull.time()) / 10));
			}

			out.writeShort(p.size());
			for (Player player : p) {
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
			// while (in.readShort() != 0)
			// System.out.println("Dumping: " + in.read());
			// System.out.println("Requesting info");
			out.writeShort(7);
			out.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
		// System.out.println(System.currentTimeMillis() - start);
		CommunicationThread communistThread = new CommunicationThread();
		Thread t = new Thread(communistThread);
		threads.add(t);
		t.start();

		// Query for a move every 10ms until the timeout is reached or the move
		// is received
		// long t2 = System.currentTimeMillis();
		int count = 0;
		while (!communistThread.updated()
				&& System.currentTimeMillis() - start < timeOut) {
			count++;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// System.out.println("stalin"+(System.currentTimeMillis()-t2));

		// Tell a player of timeout, otherwise update the position
		if (!communistThread.updated()) {
			System.out.println("Manual timeout");
			communistThread.timeout();
			try {
				out.write(6);
				out.flush();
			} catch (Exception e) {
			}
			// } else {
			// activeCount++;

			// System.out.println("info received");
		}
		threads.remove(t);
		System.out.println("Threads active: "+threads.size()+" time "+ (int) (System.currentTimeMillis() - start));

	}

	/**
	 * Thread for receiving a move from the client. Can be timed out if the
	 * player takes too long.
	 */
	class CommunicationThread implements Runnable {
		private boolean timeout = false;
		private boolean infoReceived = false;

		public CommunicationThread() {
			// move = new Position((short) -1, (short) -1);
		}

		public void run() {
			long start = System.currentTimeMillis();
			try {
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
					System.out.println("Server got: " + x + " " + y + " "
							+ angle + " " + upgrade);
				}
			} catch (Exception e) {

			}
		}

		public boolean timeout() {
			timeout = true;
			alive = false;

			// Tell the player that he's dead
			try {
				out.writeShort(9);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return infoReceived;
		}

		public boolean updated() {
			return infoReceived;
		}
	}
}
