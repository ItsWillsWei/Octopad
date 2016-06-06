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
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();

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

	// TODO make this work
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

				long startTime = System.nanoTime(); // start time plus 10 ms
				// Update objects around it, trying to avoid lag
				p.setSurroundings(surroundingPlayers(p), surroundingShots(p));
				p.updateSurroundings();
				try {
					Thread.sleep(10);
					p.requestInfo();
					if (p.shooting())
						bullets.add(new Bullet(p.getPos(), p.getID())); // TODO
																		// identify
																		// players

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				long endTime = System.nanoTime(); // end time
				double elapsedTime = (endTime - startTime) / 1000000.0;
				System.out.println(elapsedTime);

				// Checks to see if anything will damage the player and update
				// health

				// Check to see if the player needs an upgrade
			}

			System.out.println("Player " + p.getID() + " is dead");
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

	/**
	 * 
	 * @param p
	 * @return
	 */
	// TODO we can add collision detection here and just handle it in here
	// that way we don't have to worry about it later
	public ArrayList<Bullet> surroundingShots(Player p) {
		ArrayList<Bullet> b = new ArrayList<Bullet>();

		// Some constant for the screen size
		for (Bullet currentBullet : bullets) {
			if (Math.abs(currentBullet.getPos().getX() - p.getPos().getX()) <= 50
					&& Math.abs(currentBullet.getPos().getY()
							- p.getPos().getY()) <= 50) {
				// if (p.getSurroundingBullets().contains(currentBullet))
				b.add(currentBullet);
				// if (collision)
				// decrement health, change course of bullet
			}
		}

		return b;
	}

	/*
	 * Alternatively could just shout it right away
	 */
	public ArrayList<Player> surroundingPlayers(Player player) {
		ArrayList<Player> p = new ArrayList<Player>();

		// TODO potentially only update things that are missing and that need to
		// be added

		// Some constant for the screen size
		for (Player currentPlayer : players) {
			// Not the player and not already on screen

			if (!player.equals(currentPlayer))
				// && !player.getSurroundingPlayers().contains(currentPlayer))
				if (Math.abs(currentPlayer.getPos().getX()
						- player.getPos().getX()) <= 50
						&& Math.abs(currentPlayer.getPos().getY()
								- player.getPos().getY()) <= 50)
					p.add(currentPlayer);
		}

		return p;

	}

}
