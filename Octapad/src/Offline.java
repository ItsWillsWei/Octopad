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
public class Offline {
	public static boolean accessingAll = false;
	private int bulletDuration = 5000;
	private short noOfPlayers = 0, noOfBlocks = 0;
	private ArrayList<SimplePlayer> all = new ArrayList<SimplePlayer>();
	private ArrayList<OfflineAI> players = new ArrayList<OfflineAI>();
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private BufferedImage back;
	private static boolean blocksAccessing = false;
	OfflinePlayer player;
	private boolean alive = true;

	public static void main(String[] args) {
		new Offline();
	}

	public Offline() {
		try {
			back = ImageIO.read(new File("back-low.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player = new OfflinePlayer(all);
		int x = player.getPos().getX();
		int y = player.getPos().getY();
		Color c = new Color(player.getColour().getRed(), player.getColour()
				.getGreen(), player.getColour().getBlue());
		int upgrade = player.getUpgrade();
		int angle = player.getAngle();
		all.add(new SimplePlayer(x, y, c, upgrade, angle));
		noOfPlayers++;
		player.setVisible(true);
		while (!OfflinePlayer.started) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		Thread g = new Thread(new GenerateBlocks());
		g.start();
		Thread t = new Thread(new AIThread());
		t.start();
		run();
	}

	/**
	 * Runs an AIClient to start the game
	 * 
	 */
	class AIThread implements Runnable {
		public void run() {
			for (int i = 0; i < 1; i++) {
				try {
					Thread.sleep(1400);
				} catch (Exception e) {
				}
				Position current = new Position(
						(short) (Math.random() * (1024 - 30)) + 20,
						(short) (Math.random() * (768 - 30)) + 20);
				OfflineAI ai = new OfflineAI(current, noOfPlayers, false);
				ai.setPlayers(all);
				int x = ai.getPos().getX();
				int y = ai.getPos().getY();
				Color c = new Color(ai.getColour().getRed(), ai.getColour()
						.getGreen(), ai.getColour().getBlue());
				int upgrade = ai.getUpgrade();
				int angle = ai.getAngle();
				accessingAll = true;
				all.add(new SimplePlayer(x, y, c, upgrade, angle));
				players.add(ai);
				noOfPlayers++;
				accessingAll = false;
			}
		}
	}

	/**
	 * Generates 10 blocks every five seconds. For every player, 20 blocks are
	 * generated.
	 * 
	 */
	class GenerateBlocks implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
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
					}
					noOfBlocks += 10;
					blocksAccessing = false;
				}

			}
		}

	}

	public void run() {
		while (alive) {

			try {
				Thread.sleep(30);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			long start = System.currentTimeMillis();
			while (Offline.accessingAll) {
			}
		
			all.get(0).setPos(player.getPos());
			all.get(0).setAngle(player.getAngle());
			for (int i = 1; i < all.size(); i++) {
				all.get(i).setPos(players.get(i - 1).getPos());
				all.get(i).setAngle(players.get(i - 1).getAngle());

			}

			// Shoot a bullet
			if (player.shooting()) {
				bullets.add(new Bullet(player.getPos(), player.getID(),
						new Vector(Math.cos(player.getAngle() * Math.PI / 180)
								* player.getBulletSpeed(), Math.sin(player
								.getAngle() * Math.PI / 180)
								* player.getBulletSpeed())));
			}

			// Display all bullets
			ArrayList<Position> p = new ArrayList<Position>();
			long time = System.currentTimeMillis();
			for (Bullet b : bullets) {
				p.add(new Position(b.getPos().getX()
						+ (int) ((time - b.time()) / 10.0 * b.xChange()), b
						.getPos().getY()
						+ (int) ((time - b.time()) / 10.0 * b.yChange())));

			}
			player.setBullets(p);

			for (OfflineAI ai : players) {
				if (ai.shooting()) {
					bullets.add(new Bullet(ai.getPos(), ai.getID(), new Vector(
							Math.cos(ai.getAngle() * Math.PI / 180)
									* ai.getBulletSpeed(), Math.sin(ai
									.getAngle() * Math.PI / 180)
									* ai.getBulletSpeed())));
				}
			}

			// Display all blocks
			ArrayList<Block> temp = (ArrayList<Block>) blocks.clone();
			player.setBlocks(temp);

			if (player.getPoints() >= 1000 && player.getUpgrade() != 1) {
				player.setUpgrade(1);
			} else if (player.getPoints() > 10000 && player.getUpgrade() != 2)
				player.setUpgrade(2);
			for (OfflineAI a : players) {
				a.closestPlayer();
			}

			// Check each bullet
			for (int i = 0; i < bullets.size(); i++) {
				Bullet currentBullet = bullets.get(i);

				boolean bulletHit = false;
				time = System.currentTimeMillis();
				// Check to see if a bullet hits a player
				if (Math.abs(currentBullet.getPos().getX()
						+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
								.xChange()) - player.getPos().getX()) <= 30
						&& Math.abs(currentBullet.getPos().getY()
								+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
										.yChange()) - player.getPos().getY()) <= 30
						&& currentBullet.getID() != player.getID()) {
					player.hit(10);

					if (!player.alive()) {
						player = new OfflinePlayer(all);
						players.clear();
						all.clear();
						blocks.clear();
						all.add(new SimplePlayer(player.getPos().getX(), player
								.getPos().getY(), player.getColour(), player
								.getUpgrade(), player.getAngle()));
					}
					// bullets.remove(currentBullet);
					bulletHit = true;
				}

				for (int j = 0; j < players.size(); j++) {
					OfflineAI c = players.get(j);
					time = System.currentTimeMillis();
					// Check to see if a bullet hits a player
					if (Math.abs(currentBullet.getPos().getX()
							+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
									.xChange()) - c.getPos().getX()) <= 30
							&& Math.abs(currentBullet.getPos().getY()
									+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
											.yChange()) - c.getPos().getY()) <= 30
							&& currentBullet.getID() != c.getID()) {
						c.hit(10);

						if (!c.alive()) {
							while (Offline.accessingAll) {
							}
							all.remove(players.indexOf(c) + 1);
							players.remove(c);
							j = -1;
						}

						// bullets.remove(currentBullet);
						bulletHit = true;
						break;
					}
				}

				// Check to see if a bullet hits a block
				if (!blocksAccessing) {
					for (int j = 0; j < blocks.size(); j++) {
						Block b = blocks.get(j);
						time = System.currentTimeMillis();

						if (Math.abs(b.getPos().getX()
								+ 7
								- (currentBullet.getPos().getX() + (short) (time - currentBullet
										.time())
										/ 10.0
										* currentBullet.xChange())) <= 13
								&& (Math.abs(b.getPos().getY()
										+ 7
										- (currentBullet.getPos().getY() + (short) (time - currentBullet
												.time())
												/ 10.0
												* currentBullet.yChange())) <= 13))

						{
							// System.out.println("Should be removed");
							blocks.remove(j);

							// bullets.remove(currentBullet);
							bulletHit = true;
							if (currentBullet.getID() == player.getID())
								player.setPoints((player.getPoints() + 100));
							for (OfflineAI playa : players) {
								if (playa.getID() == currentBullet.getID())
									playa.setPoints((short) (playa.getPoints() + 10));
								// Update upgrades/points
								if (playa.getPoints() > 1000
										&& player.getUpgrade() != 1)
									playa.setUpgrade(1);
								else if (playa.getPoints() > 10000
										&& player.getUpgrade() != 2)
									playa.setUpgrade(2);
								playa.setBlocks(blocks);
							}
						}

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
							// System.out.println(bulletHit);
							break;
						}
					}
				}
			}
			//System.out.println((System.currentTimeMillis() - start));
		}

	}
}
