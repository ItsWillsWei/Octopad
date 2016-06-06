import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Player {

	private int id;
	private int inactiveCount = 0;
	private Socket sock;
	private InputStream in;
	private BufferedReader br;
	private OutputStream out;
	private Position pos;
	private Color color;
	private PrintWriter pw;
	private boolean alive = true;
	private int timeOut=1000;
	private int angle;
	boolean shoot;
	int upgrade;

	public Player(Socket s, Position p, int id) {

		try {
			sock = s;
			pos = p;
			color = new Color((int) (Math.random() * 256),
					(int) (Math.random() * 256), (int) (Math.random() * 256));
			in = sock.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			out = sock.getOutputStream();
			pw = new PrintWriter(out);
			this.id=id;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getID()
	{
		return id;
	}

	public void update() {
		this.requestInfo();
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
		System.out.println("send");
		pw.println(command);
		pw.flush();
	}

	public void updateSurroundings(Object[] n) {
		for (Object o : n) {
			// Send it to the player via the printwriter
		}

	}

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

		communistThread.timeout();

		// Tell a player of timeout, otherwise update the position
		if (!communistThread.updated()) {
			pw.println("6");
			pw.flush();
		} else
			inactiveCount++;

		if (inactiveCount > 100) {
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

				//System.out.println(timeout);
				if (!timeout) {
					System.out.println("Communcation Thread active");
					// If the first number is 1 (indicating a player wants to
					// move)
					String[] command = br.readLine().split(" ");
					move = new Position(Integer.parseInt(command[0]),
							Integer.parseInt(command[1]));
					infoReceived = true;
					angle = Integer.parseInt(command[2]);
					shoot = command[3].equals("1"); // True or false
					upgrade = Integer.parseInt(command[4]);
					System.out.println("Received: " + move.getX() + " "
							+ move.getY() + " " + angle + " " + shoot + " "
							+ upgrade);
				}
			} catch (Exception e) {

			}
		}

		public boolean timeout() {
			timeout = true;
			return infoReceived;
		}

		public boolean updated() {
			if (infoReceived) {
				return true;
			} else {
				return false;
			}
		}
	}
}
