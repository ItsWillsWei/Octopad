import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Player {
	private int WIDTH = 1024;
	private int HEIGHT = 700;
	private int id;
	private int activeCount = 0;
	private Socket sock;
	private InputStream in;
	private BufferedReader br;
	private OutputStream out;
	private Position pos;
	private Color color;
	private PrintWriter pw;
	private int health = 90;
	private boolean alive = true;
	private int timeOut = 20000;
	private int angle;
	private boolean shoot;
	private int upgrade = 0;
	private ArrayList<Bullet> bullet;
	private ArrayList<Player> players;

	public Player(Socket s, Position p, int id) {

		try {
			sock = s;
			pos = p;
			color = new Color((int) (Math.random() * 256),
					(int) (Math.random() * 256), (int) (Math.random() * 256));
			System.out.println(color);
			in = sock.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			out = sock.getOutputStream();
			pw = new PrintWriter(out);
			this.id = id;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void hit(int n) {
		health -= n;
		pw.println("3 " + health);
	}

	public int getAngle() {
		//System.out.println("angle is " + angle);
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

	public void sendCommand(String command) {
		//System.out.println("send");
		pw.println(command);
		pw.flush();
	}

	public void updateSurroundings() {
		// Removed /10
		pw.print("8 ");
		pw.print(bullet.size() + " ");
		long time = System.currentTimeMillis();
		for (Bullet bull : bullet) {
			pw.print((bull.getPos().getX() + (int) (bull.xChange()
					* (time - bull.time()) / 10))
					+ " "
					+ (bull.getPos().getY() + (int) (bull.yChange()
							* (time - bull.time()) / 10)) + " ");
		}
		pw.print(players.size() + " ");
		for (Player player : players) {
			pw.print(player.getPos().getX() + " " + player.getPos().getY()
					+ " " + player.getColour().getRed() + " "
					+ player.getColour().getGreen() + " "
					+ player.getColour().getBlue() + " " + player.getUpgrade()
					+ " ");
		}
		pw.println();
		pw.flush();

	}

	/**
	 * Asking for an update on the player
	 */
	public void requestInfo() {
		try {
			while (br.ready())
				System.out.println("Dumping: " + br.readLine());

			pw.println("7"); // Requesting all information
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		long start = System.currentTimeMillis();
		CommunicationThread communistThread = new CommunicationThread();
		Thread t = new Thread(communistThread);

		t.start();

		// Query for a move every 10ms until the timeout is reached or the move
		// is received
		while (!communistThread.updated()
				&& System.currentTimeMillis() - start < timeOut) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Tell a player of timeout, otherwise update the position
		if (!communistThread.updated()) {
			System.out.println("Manual timeout");
			communistThread.timeout();
			pw.println("6");
			pw.flush();
		} else
			activeCount++;

		if (activeCount > 10000) {
			alive = false;
			System.out.println("Timed out too much");
		}

	}

	/**
	 * Thread for receiving a move from the client. Can be timed out if the
	 * player takes too long.
	 */
	class CommunicationThread implements Runnable {
		private boolean timeout = false;
		private Position move;
		private boolean infoReceived = false;

		public CommunicationThread() {
			move = new Position(-1, -1);
		}

		public void run() {
			try {
				while (!br.ready() && !timeout) {
				}
				;

				// System.out.println(timeout);
				if (!timeout) {
					// System.out.println("Communcation Thread active");
					// If the first number is 1 (indicating a player wants to
					// move)
					String[] command = br.readLine().split(" ");
					move = new Position(Integer.parseInt(command[0]),
							Integer.parseInt(command[1]));

					angle = Integer.parseInt(command[2]);
					shoot = command[3].equals("1"); // True or false
					upgrade = Integer.parseInt(command[4]);
					infoReceived = true;
//					System.out.println("Received: " + move.getX() + " "
//							+ move.getY() + " " + angle + " " + shoot + " "
//							+ upgrade);
				}
			} catch (Exception e) {

			}
		}

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
