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
	private int WIDTH = 1024;
	private int HEIGHT = 700;
	private static Display display;
	private ServerSocket serverSocket;
	private int noOfPlayers;
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	private static boolean currentlyAccessing = false;

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

			Thread t = new Thread(new BroadcastThread());
			t.start();
			while (true) {

				System.out.println("Waiting for connection");
				// Connect new players
				client = serverSocket.accept();
				noOfPlayers++;
				System.out.printf("Client #%d connected!%n", noOfPlayers);
				Position current = new Position(
						(int) (Math.random() * (WIDTH - 30)) + 20,
						(int) (Math.random() * (HEIGHT - 30)) + 20);
				while (!withinBounds(current, 50))
					current = new Position(
							(int) (Math.random() * (WIDTH - 30)) + 20,
							(int) (Math.random() * (HEIGHT - 30)) + 20);
				currentPlayer = new Player(client, current, noOfPlayers);
				players.add(currentPlayer);
				currentThread = new Thread(new PlayerThread(currentPlayer));
				threads.add(currentThread);
				currentThread.start();

				// Be nice to the JVM
				Thread.sleep(1000);
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
	public boolean withinBounds(Position p, int radius) {
		return (p.getX() - radius > 0 && p.getX() + radius < WIDTH
				&& p.getY() - radius > 0 && p.getY() + radius < HEIGHT);
	}

	class BroadcastThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				for(Player p: players){
					
					
					
					
				}
			}
		}
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

			// Initial position and colour
			p.sendCommand(p.getPos().getX() + " " + p.getPos().getY() + " "
					+ p.getColour().getRed() + " " + p.getColour().getGreen()
					+ " " + p.getColour().getBlue());
			int counter = 0;
			// Keep track while the player lives
			while (p.alive()) {
				counter++;
				long startTime = System.currentTimeMillis(); // start time plus
																// 10 ms
				// Update objects around it, trying to avoid lag
				p.setSurroundings(surroundingPlayers(p), surroundingShots(p));
				p.updateSurroundings();
				try {
					Thread.sleep(30);
					p.requestInfo();
					if (p.shooting()) {
						bullets.add(new Bullet(p.getPos(), p.getID(),
								new Vector(Math.cos(p.getAngle() * 1.0 / 180
										* Math.PI), -1
										* Math.sin(p.getAngle() * 1.0 / 180
												* Math.PI))));
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				long endTime = System.currentTimeMillis(); // end time
				if (counter % 500 == 0)
					System.out.println(endTime - startTime);

				// Checks to see if anything will damage the player and update
				// health

				// Check to see if the player needs an upgrade
			}

			// Tell them they are dead
			System.out.println("Player " + p.getID() + " is dead");
			p.sendCommand("6");
			// TODO delete someone graphically when they are dead

			// TODO remove a thread
			for (Bullet b : bullets) {
				if (b.getID() == p.getID())
					bullets.remove(b);
			}
			int remove = players.indexOf(p);
			players.remove(remove);
			threads.remove(remove);
		}
	}

	// TODO move this inside so that they won't try to access resources at the
	// same time
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
		while (currentlyAccessing) {
		}
		currentlyAccessing = true;
		for (int i = 0; i < bullets.size(); i++) {
			Bullet currentBullet = bullets.get(i);

			long time = System.currentTimeMillis();
			// Time bullets out here i guess
			if (currentBullet.getPos().getX() > WIDTH
					|| currentBullet.getPos().getY() > HEIGHT
					|| currentBullet.getPos().getY() < 0
					|| currentBullet.getPos().getX() < 0
					|| !currentBullet.alive()
					|| time - currentBullet.time() > 4000) {
				i--;
				// System.out.println("removing bullet");
				bullets.remove(currentBullet);
			}
			int bulletDmg = 10;
			if (Math.abs(currentBullet.getPos().getX()
					+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
							.xChange()) - p.getPos().getX()) <= 30
					&& Math.abs(currentBullet.getPos().getY()
							+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
									.yChange()) - p.getPos().getY()) <= 30
					&& currentBullet.getID() != p.getID())
				p.hit(bulletDmg);

			// Do it all based on changes
			if (Math.abs(currentBullet.getPos().getX()
					+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
							.xChange()) - p.getPos().getX()) <= 5000
					&& Math.abs(currentBullet.getPos().getY()
							+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
									.yChange()) - p.getPos().getY()) <= 5000) {
				// if (p.getSurroundingBullets().contains(currentBullet))
				b.add(currentBullet);
				// if (collision)
				// decrement health, change course of bullet
			}
		}
		currentlyAccessing = false;

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
						- player.getPos().getX()) <= 500
						&& Math.abs(currentPlayer.getPos().getY()
								- player.getPos().getY()) <= 500)
					p.add(currentPlayer);
		}
		return p;
	}

}
