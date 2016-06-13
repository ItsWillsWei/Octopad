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
		Player currentPlayer;
		Thread t = new Thread(new BroadcastThread());
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		try {
			serverSocket = new ServerSocket(421);
			players = new ArrayList<Player>();

			while (true) {

				System.out.println("Waiting for connection");
				// Connect new players
				client = serverSocket.accept();

				noOfPlayers++;
				System.out.printf("Client #%d connected!%n", noOfPlayers);
				Position current = new Position(
						(short) (Math.random() * (WIDTH - 30)) + 20,
						(short) (Math.random() * (HEIGHT - 30)) + 20);
				while (!withinBounds(current, 50))
					current = new Position(
							(short) (Math.random() * (WIDTH - 30)) + 20,
							(short) (Math.random() * (HEIGHT - 30)) + 20);
				currentlyAccessing = true;
				currentPlayer = new Player(client, current, noOfPlayers);
				players.add(currentPlayer);
				// Initial position and colour
				short[] info = { currentPlayer.getPos().getX(),
						currentPlayer.getPos().getY(),
						(short) currentPlayer.getColour().getRed(),
						(short) currentPlayer.getColour().getGreen(),
						(short) currentPlayer.getColour().getBlue() };
				System.out.println(currentPlayer.getPos().getX() + " "
						+ currentPlayer.getPos().getY());
				currentPlayer.sendCommand(info);
				// currentPlayer.sendCommand(currentPlayer.getPos().getX() + " "
				// + currentPlayer.getPos().getY() + " "
				// + currentPlayer.getColour().getRed() + " "
				// + currentPlayer.getColour().getGreen() + " "
				// + currentPlayer.getColour().getBlue());
				// currentThread = new Thread(new PlayerThread(currentPlayer));
				// threads.add(currentThread);
				// currentThread.start();
				currentlyAccessing = false;
				// Be nice to the JVM
				Thread.sleep(100);
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

			while (!Thread.currentThread().isInterrupted()) {
				// TODO maybe check for collisions here then just tell
				// people
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long start = System.currentTimeMillis();

				for (int i = 0; i < players.size(); i++) {
					Player p = players.get(i);
					// System.out.println("hi");
					if (currentlyAccessing)
						break;
					// System.out.println("bye");

					// System.out.print(System.currentTimeMillis()-start+ " ");

					if (!p.alive()) {// Tell them they are dead
						System.out.println("Player " + p.getID() + " is dead");
						// TODO delete someone graphically when they are
						// dead

						// TODO remove a thread
						for (Bullet b : bullets) {
							if (b.getID() == p.getID())
								bullets.remove(b);
						}
						int remove = players.indexOf(p);
						i = 0;
						players.remove(remove);
						break;
					}

					// p.setSurroundings(surroundingPlayers(p),
					// surroundingShots(p));
					p.updateSurroundings(bullets, players);
					// p.updateSurroundings();
					// System.out.print(System.currentTimeMillis()-start+ " ");
					// Requesting info
					long s = System.currentTimeMillis();
					p.requestInfo();
					// System.out.println(System.currentTimeMillis()-s);
					if (p.shooting()) {
						bullets.add(new Bullet(p.getPos(), p.getID(),
								new Vector(Math.cos(p.getAngle() * 1.0 / 180
										* Math.PI), Math.sin(p.getAngle() * 1.0
										/ 180 * Math.PI))));
					}

				}

				long time = System.currentTimeMillis();
				for (int i = 0; i < bullets.size(); i++) {
					Bullet currentBullet = bullets.get(i);
					boolean ok = true;
					for (Player p : players) {
						if (Math.abs(currentBullet.getPos().getX()
								+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
										.xChange()) - p.getPos().getX()) <= 30
								&& Math.abs(currentBullet.getPos().getY()
										+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
												.yChange()) - p.getPos().getY()) <= 30
								&& currentBullet.getID() != p.getID()) {
							p.hit(10);
							bullets.remove(currentBullet);
							ok = false;
							break;
						}

					}
					if (!ok || currentBullet.getPos().getX() > WIDTH
							|| currentBullet.getPos().getY() > HEIGHT
							|| currentBullet.getPos().getY() < 0
							|| currentBullet.getPos().getX() < 0
							|| !currentBullet.alive()
							|| time - currentBullet.time() > 4000) {
						i--;
						// System.out.println("removing bullet");
						bullets.remove(currentBullet);
					}
				} // System.out.println("Server" +
					// (System.currentTimeMillis()-start));
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
