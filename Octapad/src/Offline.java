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
	private int WIDTH = 1024;
	private int HEIGHT = 700;
	private int bulletDuration = 5000;
	private int noOfPlayers, noOfBlocks;
	private ArrayList<OfflineAI> players = new ArrayList<OfflineAI>();
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private BufferedImage back;
	private static boolean blocksAccessing = false;
	OfflinePlayer player;

	public static void main(String[] args) {
		new Offline();
	}

	public Offline() {

		player = new OfflinePlayer();
		Thread g = new Thread(new GenerateBlocks());
		g.start();
		run();
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
					}
					noOfBlocks += 10;
					blocksAccessing = false;
				}

			}
		}

	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Shoot a bullet
			if (player.shooting()) {
				bullets.add(new Bullet(player.getPos(), player.getID(),
						new Vector(Math.cos(player.getAngle() * Math.PI / 180)
								* player.getBulletSpeed(), Math.sin(player
								.getAngle() * Math.PI / 180)
								* player.getBulletSpeed())));
			}

			for (OfflineAI a : players) {
				a.closestPlayer();
			}

			// Check each bullet
			for (int i = 0; i < bullets.size(); i++) {
				Bullet currentBullet = bullets.get(i);

				boolean bulletHit = false;
				long time = System.currentTimeMillis();
				// Check to see if a bullet hits a player
				if (Math.abs(currentBullet.getPos().getX()
						+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
								.xChange()) - player.getPos().getX()) <= 30
						&& Math.abs(currentBullet.getPos().getY()
								+ (int) ((time - currentBullet.time()) / 10.0 * currentBullet
										.yChange()) - player.getPos().getY()) <= 30
						&& currentBullet.getID() != player.getID()) {
					player.hit(10);
					// bullets.remove(currentBullet);
					bulletHit = true;
					break;
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
							player.setBlocks(blocks);

							// bullets.remove(currentBullet);
							bulletHit = true;
							for (OfflineAI p : players) {
								if (p.getID() == currentBullet.getID())
									p.setPoints((short) (p.getPoints() + 10));
								// Update upgrades/points
								if (p.getPoints() > 1000)
									p.setUpgrade(1);
								else if (p.getPoints() > 10000)
									p.setUpgrade(2);
								p.setBlocks(blocks);
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
		}

		// if (count % 25 == 0)
		// System.out.println(System.currentTimeMillis() - start);

	}

}
