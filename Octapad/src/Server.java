import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * 
 * @author
 * @version Jun 10, 2016
 */
public class Server {
	private int WIDTH = 1024;
	private int HEIGHT = 700;
	private int bulletDuration = 5000;
	private ServerSocket serverSocket;
	private int noOfPlayers, noOfBlocks;
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private static boolean currentlyAccessing = false;
	private BufferedImage back;
	private static boolean blocksAccessing = false;

	public static void main(String[] args) {
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
		Thread g = new Thread(new GenerateBlocks());
		g.start();
		try {
			serverSocket = new ServerSocket(421);
			players = new ArrayList<Player>();
			noOfPlayers = 0;
			noOfBlocks = 0;

			back = ImageIO.read(new File("back-low.jpg"));
			Thread ai = new Thread(new AIThread());
			// ai.start();
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
				currentPlayer.sendCommand(info);
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

	/**
	 * Runs an AIClient to start the game
	 *
	 */
	class AIThread implements Runnable {
		public void run() {
			try {
				Thread.sleep(1400);
			} catch (Exception e) {
			}
			new AIClient();
		}
	}

	/**
	 * Generates 10 blocks every five seconds. For every player, 20 blocks are
	 * generated.
	 *
	 */
	class GenerateBlocks implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// If blocks should be generated
				if (!blocksAccessing && noOfBlocks < noOfPlayers * 20) {
					blocksAccessing = true;
					// Generate each block
					for (int block = 0; block < 10; block++) {
						Position spawn = new Position((short) (Math.random()
								* back.getWidth() / 20)
								* 20 - back.getWidth() / 2,
								(short) (Math.random() * back.getHeight() / 20)
										* 20 - back.getWidth() / 2);
						Color c = new Color((int) (Math.random() * 256),
								(int) (Math.random() * 256),
								(int) (Math.random() * 256));
						blocks.add(new Block(spawn, 20, c));
						// Send the blocks to the player
						short[] info = new short[7];
						info[0] = 10;
						info[1] = 1;
						info[2] = spawn.getX();
						info[3] = spawn.getY();
						info[4] = (short) (c.getRed());
						info[5] = (short) (c.getGreen());
						info[6] = (short) (c.getBlue());

						for (Player p : players)
							p.sendCommand(info);
					}
					noOfBlocks += 10;
					blocksAccessing = false;
				}

			}
		}

	}

	/**
	 * Communicates with the PlayerClient via the Player class
	 *
	 */
	class BroadcastThread implements Runnable {

		@Override
		public void run() {
			int count = 0;
			while (!Thread.currentThread().isInterrupted()) {
				count++;
				try {
					Thread.sleep(30);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long start = System.currentTimeMillis();

				// Update each player
				for (int i = 0; i < players.size(); i++) {
					Player p = players.get(i);
					if (currentlyAccessing)
						break;

					if (!p.alive()) {// Tell them they are dead
						System.out.println("Player " + p.getID() + " is dead");
						int remove = players.indexOf(p);
						i = 0;
						players.remove(remove);
						noOfPlayers--;
						break;
					}

					// p.updateSurroundings(surroundingShots(p),
					// surroundingPlayers(p));
					p.updateSurroundings(bullets, players);
					// Requesting info from the player
					long s = System.currentTimeMillis();
					p.requestInfo();

					// Shoot a bullet
					if (p.shooting()) {
						bullets.add(new Bullet(p.getPos(), p.getID(),
								new Vector(Math.cos(p.getAngle() * Math.PI
										/ 180)
										* p.getBulletSpeed(), Math.sin(p
										.getAngle() * Math.PI / 180)
										* p.getBulletSpeed())));
					}

				}
				long time = System.currentTimeMillis();
				// Check each bullet
				for (int i = 0; i < bullets.size(); i++) {
					Bullet currentBullet = bullets.get(i);

					boolean bulletHit = false;
					// Check to see if a bullet hits a player
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
							bulletHit = true;
							break;
						}

					}

					// Check to see if a bullet hits a block
					if (!blocksAccessing) {
						for (int j = 0; j < blocks.size(); j++) {
							Block b = blocks.get(j);
							if (new Rectangle(b.getPos().getX(), b.getPos()
									.getY(), 20, 20).intersects(new Rectangle(
									currentBullet.getPos().getX(), 3,
									currentBullet.getPos().getY(), 3))) {

								blocks.remove(b);
								// bullets.remove(currentBullet);
								bulletHit = true;
								for (Player p : players) {
									if (p.getID() == currentBullet.getID())
										p.setPoints((short) (p.getPoints() + 10));
									// Update upgrades/points
									if (p.getPoints() > 1000)
										p.setUpgrade(1);
									else if (p.getPoints() > 10000)
										p.setUpgrade(2);
									else
										p.setUpgrade(0);
									p.sendCommand(new short[] { 10, 2,
											b.getPos().getX(),
											b.getPos().getY() });
								}
							}
							// System.out.println("S Player " + i+" "
							// + (System.currentTimeMillis() - start));

							if (bulletHit
									|| currentBullet.getPos().getX() > back
											.getWidth() / 2
									|| currentBullet.getPos().getY() > back
											.getHeight() / 2
									|| currentBullet.getPos().getY() < -1
											* back.getHeight() / 2
									|| currentBullet.getPos().getX() < -1
											* back.getWidth() / 2
									|| !currentBullet.alive()
									|| time - currentBullet.time() > bulletDuration) {
								j = 0;
								bullets.remove(currentBullet);
								break;
							}
						}
					}
					
				}System.out.println("Time for check: "
						+ (System.currentTimeMillis() - time));
			}

			// if (count % 25 == 0)
			// System.out.println(System.currentTimeMillis() - start);

		}

	}

	/*
	 * Checks the surrounding area for players
	 */
	// TODO we can add collision detection here and just handle it in here
	// that way we don't have to worry about it later
	public ArrayList<Bullet> surroundingShots(Player p) {
		ArrayList<Bullet> b = new ArrayList<Bullet>();

		for (Bullet currentBullet : bullets) {
			long time = System.currentTimeMillis();

			// Do it all based on changes
			if (2 * Math
					.abs(currentBullet.getPos().getX()
							+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
									.xChange()) - p.getPos().getX()) <= WIDTH
					&& 2 * Math
							.abs(currentBullet.getPos().getY()
									+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
											.yChange()) - p.getPos().getY()) <= HEIGHT) {
				// if (p.getSurroundingBullets().contains(currentBullet))
				b.add(currentBullet);
				// if (collision)
				// decrement health, change course of bullet
			}
		}

		for (int i = 0; i < b.size(); i++) {
			Bullet currentBullet = b.get(i);
			long time = System.currentTimeMillis();

			// Checks whether it hits the current player
			if (Math.abs(currentBullet.getPos().getX()
					+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
							.xChange()) - p.getPos().getX()) <= 30
					&& Math.abs(currentBullet.getPos().getY()
							+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
									.yChange()) - p.getPos().getY()) <= 30
					&& currentBullet.getID() != p.getID())
				p.hit(10);
			if (!blocksAccessing) {
				blocksAccessing = true;
				for (Block block : blocks) {
					// System.out.println(block.getPos().getX() + " "
					// + block.getPos().getY() + " "
					// + currentBullet.getPos().getX() + " "
					// + currentBullet.getPos().getY());
					if (new Rectangle(block.getPos().getX(), block.getPos()
							.getY(), 20, 20).contains(new Point(currentBullet
							.getPos().getX() + 3,
							currentBullet.getPos().getY() + 3))) {
						for (Player playa : players) {
							if (playa.getID() == currentBullet.getID())
								playa.setPoints((short) (playa.getPoints() + 10));
							// Update upgrades/points
							if (playa.getPoints() > 1000)
								playa.setUpgrade(1);
							else if (playa.getPoints() > 10000)
								playa.setUpgrade(2);
							playa.sendCommand(new short[] { 10, 2,
									block.getPos().getX(),
									block.getPos().getY() });
						}
						blocks.remove(block);
						bullets.remove(currentBullet);
						i = 0;
					}
				}
				blocksAccessing = false;
			}
		}
		return b;

		// Some constant for the screen size
		// for (int i = 0; i < bullets.size(); i++) {
		// Bullet currentBullet = bullets.get(i);
		// long time = System.currentTimeMillis();
		// // Time bullets out here i guess
		// if (currentBullet.getPos().getX() > back.getWidth() / 2
		// || currentBullet.getPos().getY() > back.getHeight() / 2
		// || currentBullet.getPos().getY() < -1 * back.getHeight()
		// / 2
		// || currentBullet.getPos().getX() < -1 * back.getWidth() / 2
		// || !currentBullet.alive()
		// || time - currentBullet.time() > 4000) {
		// i--;
		// bullets.remove(currentBullet);
		// }
		// int bulletDmg = 10;
		// if (Math.abs(currentBullet.getPos().getX()
		// + (int) ((time - currentBullet.time()) / 10.0 * currentBullet
		// .xChange()) - p.getPos().getX()) <= 30
		// && Math.abs(currentBullet.getPos().getY()
		// + (int) ((time - currentBullet.time()) / 10.0 * currentBullet
		// .yChange()) - p.getPos().getY()) <= 30
		// && currentBullet.getID() != p.getID())
		// p.hit(bulletDmg);
		//
		// // Do it all based on changes
		// if (Math.abs(currentBullet.getPos().getX()
		// + (int) ((time - currentBullet.time()) / 10.0 * currentBullet
		// .xChange()) - p.getPos().getX()) <= 5000
		// && Math.abs(currentBullet.getPos().getY()
		// + (int) ((time - currentBullet.time()) / 10.0 * currentBullet
		// .yChange()) - p.getPos().getY()) <= bulletDuration) {
		// // if (p.getSurroundingBullets().contains(currentBullet))
		// b.add(currentBullet);
		// // if (collision)
		// // decrement health, change course of bullet
		// }
		// }
		// return b;
	}

	/*
	 * Alternatively could just shout it right away
	 */
	public ArrayList<Player> surroundingPlayers(Player player) {
		ArrayList<Player> p = new ArrayList<Player>();
		// Some constant for the screen size
		for (Player currentPlayer : players) {
			// Not the player and not already on screen

			if (!player.equals(currentPlayer))
				// &&
				// !player.getSurroundingPlayers().contains(currentPlayer))
				if (2 * Math.abs(currentPlayer.getPos().getX()
						- player.getPos().getX()) <= WIDTH
						&& 2 * Math.abs(currentPlayer.getPos().getY()
								- player.getPos().getY()) <= HEIGHT)
					p.add(currentPlayer);
		}
		return p;
	}

}
