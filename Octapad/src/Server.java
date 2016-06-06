import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 * @author
 * @version Jun 2, 2016
 */
public class Server {
	private static Display display;
	private ServerSocket serverSocket;
	private int noOfPlayers;
	private ArrayList<Player> players;
	private ArrayList<Thread> threads;
	private ArrayList<Bullet> bullets;
	private Piece[][] pieces;

	public static void main(String[] args) {
		display = new Display();
		new Server();
	}

	public Server() {
		connectPlayers();
	}

	/**
	 * Connect the players
	 */
	public void connectPlayers() {
		Socket client;
		Thread currentThread;
		Player currentPlayer;

		try {
			serverSocket = new ServerSocket(421);
			players = new ArrayList<Player>();
			threads = new ArrayList<Thread>();
			while (true) {

				// Be nice to the JVM
				Thread.sleep(1000);

				System.out.println("Waiting for connection");
				// Connect new players
				client = serverSocket.accept();
				noOfPlayers++;
				System.out.printf("Client #%d connected!%n", noOfPlayers);
				Position current = new Position((int) (Math.random() * 1000),
						(int) (Math.random() * 1000));
				while (intersectsAnything(current, 50))
					current = new Position((int) (Math.random() * 1000),
							(int) (Math.random() * 1000));
				currentPlayer = new Player(client, current, noOfPlayers);
				players.add(currentPlayer);
				currentThread = new Thread(new PlayerThread(currentPlayer));
				threads.add(currentThread);
				currentThread.start();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean intersectsAnything(Position p, int radius) {
		return false;
	}

	/**
	 * Keeps track of each player
	 * 
	 * @author
	 * @version May 31, 2016
	 */
	class PlayerThread implements Runnable {
		Player p;

		public PlayerThread(Player player) {
			p = player;
		}

		public void run() {
			p.sendCommand(p.getPos().getX() + " " + p.getPos().getY());
			while (p.alive()) {

				try {
					Thread.sleep(10);
					p.update();
					if (p.shoot)
						bullets.add(new Bullet(p.getPos(), p.getID())); // TODO
																		// identify
																		// players

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Checks the objects around the player and calls the update
				// surroundings method

				// Checks players around the player

				// Checks to see if anything will damage the player and update
				// health

				// Check to see if the player needs an upgrade
			}

			// TODO delete someone graphically when they are dead
			int remove = players.indexOf(p);
			// TODO remove a thread
			threads.remove(remove);
			players.remove(remove);
			for (Bullet b : bullets) {
				if (b.getID() == p.getID())
					bullets.remove(b);
			}

		}
	}

	public ArrayList<Bullet> surroundingShots(Position pos) {
		ArrayList<Bullet> b = new ArrayList<Bullet>();

		// Some constant for the screen size
		for (Bullet currentBullet : bullets) {
			if (Math.abs(currentBullet.getPos().getX() - pos.getX()) <= 50
					&& Math.abs(currentBullet.getPos().getY() - pos.getY()) <= 50)
				b.add(currentBullet);
		}

		return b;
	}

	/*
	 * Alternatively could just shout it right away
	 */
	public ArrayList<Player> surroundingPlayers(Position pos) {
		ArrayList<Player> p = new ArrayList<Player>();

		// Some constant for the screen size
		for (Player currentPlayer : players) {
			if (Math.abs(currentPlayer.getPos().getX() - pos.getX()) <= 50
					&& Math.abs(currentPlayer.getPos().getY() - pos.getY()) <= 50)
				p.add(currentPlayer);
		}

		return p;

	}

}
