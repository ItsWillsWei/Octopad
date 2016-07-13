//Import the necessary classes
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

/**
 * This is a client that must be run and connected to the server
 * 
 * @author Tilman and Will
 * @date June 16, 2016
 * 
 */
public class OfflinePlayer extends JFrame {
	// Final variables for the pane
	private static GamePanel game;
	public static boolean started = false;

	// Visual variables
	private static ArrayList<Block> blocks;

	// Game variables
	private static int damage = 10;
	private static int id;
	private static int upgrade = 0;
	private static ArrayList<SimplePlayer> players = new ArrayList<SimplePlayer>();
	private static int points = 0;
	private static Color color;
	private static int currHealth = 100;
	private static int maxHealth = 100;
	private static boolean alive = true;

	// Physics Variables
	private static Position pos;
	private static int angle;
	private static double bulletSpeed = 3;

	// All from below
	private static long start;
	// Physics variables
	private static Position player;
	private static Vector speed;
	private static Vector accel;
	private static int maxSpeed;
	private static int maxAccel;
	private static int keysDown;
	private static ArrayList<Integer> directionsPressed;
	private static PhysicsThread physics;
	private static boolean changing = false;
	private static boolean shoot = false;
	private static short reloadTime;
	private static ArrayList<Position> bullets, blocksToRemove;
	private static int time;
	private static boolean timedOut;

	/**
	 * Creates a new PlayerClient object
	 */
	public OfflinePlayer(ArrayList<SimplePlayer> p) {
		super("Octopad");
		players = p;
		Thread t = new Thread(new TimerThread());
		t.start();
		color = new Color((int) (Math.random() * 256),
				(int) (Math.random() * 256), (int) (Math.random() * 256));
		game = new GamePanel();
		setContentPane(game);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		this.setVisible(true);
	}

	public int getDamage() {
		return damage;
	}

	/**
	 * Sets the amount of points for the player
	 * 
	 * @param i
	 *            the new amount of points
	 */
	public void setPoints(int i) {
		this.points = i;
	}

	public double getBulletSpeed() {
		return bulletSpeed;
	}

	public void setBlocks(ArrayList<Block> b) {
		blocks = (ArrayList<Block>) b.clone();
	}

	public void hit(int damage) {
		currHealth -= damage;
		if (currHealth <= 0)
			alive = false;
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

	// Starts the player client
	// public static void main(String[] args) throws Exception {
	// PlayerClient p = new PlayerClient();
	// p.setVisible(true);
	// }

	public boolean shooting() {
		if (shoot) {

			shoot = false;
			return true;
		}
		return false;
	}

	public int getAngle() {
		return angle;
	}

	public int getUpgrade() {
		return upgrade;
	}

	public void setUpgrade(int upgrade) {
		this.upgrade = upgrade;
		if (upgrade == 0)
			maxHealth = 100;
		else if (upgrade == 1) {
			currHealth = 200;
			maxHealth = 200;
			reloadTime = 200;
			damage+=10;
			System.out.println("next upgrade");
		} else {
			currHealth = 300;
			maxHealth = 3000;
		}

	}

	public int getPoints() {
		return points;
	}

	/**
	 * The GamePanel class that keeps track of the game and handles graphics
	 */
	static class GamePanel extends JPanel implements MouseListener, KeyListener {
		// Player information

		private boolean alive = true;
		public boolean online, accessingBlocks;

		// Display information variables
		private KButton serverButton, offlineButton;
		private KInputPanel nameInput, ipInput, portInput;
		private static final Dimension SCREEN = new Dimension(1024, 768);
		private BufferedImage back;
		private final static byte HEALTH_HEIGHT = 5;
		private static byte HEALTH_LENGTH = 30;

		/**
		 * Creates a new GamePanel
		 */
		public GamePanel() {
			setLayout(new GridLayout(5, 5));
			setUpTitle();

			try {
				back = ImageIO.read(new File("back-low.jpg"));// new
																// ImageIcon("back-low.jpg").getImage();//
																// ImageIO.read(getClass().getResource("back.jpg"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Begin game
			// physics = (new PhysicsThread(accel, speed, pos, maxSpeed));
			// new Thread(physics).start();
			setPreferredSize(new Dimension(1024, 700));
			addMouseListener(this);
			addKeyListener(this);
		}

		// Initialize all the components for the title screen
		void setUpTitle() {
			int g = id;
			nameInput = new KInputPanel("Name: ");
			nameInput.setBackground(Color.red);
			nameInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Name");
					ipInput.setSelected(false);
					portInput.setSelected(false);

				}

			});

			ipInput = new KInputPanel("IP: ");
			ipInput.setBackground(Color.RED);
			ipInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					nameInput.setSelected(false);
					portInput.setSelected(false);

				}

			});

			portInput = new KInputPanel("Port: ");
			portInput.setBackground(Color.RED);
			portInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					nameInput.setSelected(false);
					ipInput.setSelected(false);

				}

			});

			offlineButton = new KButton("Play Offline", 300, 100);
			offlineButton.setFont(new Font("Sans-Serif", 18, 18));
			offlineButton.setBackground(Color.GREEN);
			offlineButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					started = true;
					online = false;
					pos = new Position(
							(short) (Math.random() * (WIDTH - 30)) + 20,
							(short) (Math.random() * (HEIGHT - 30)) + 20);
					physics = (new PhysicsThread(accel, speed, pos, maxSpeed,
							back));
					new Thread(physics).start();
					GamePanel.this.removeAll();
					GamePanel.this.repaint();
					GamePanel.this.requestFocusInWindow();
				}
			});

			// Listens to actions from the go button
			serverButton = new KButton("Play on Server", 300, 100);
			serverButton.setBackground(Color.RED);
			serverButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Joining server");
					online = true;
					// Connects to the server
					boolean errorConnecting = false;

				}
			});

			displayTitle();
		}

		// Sets the display to the title screen
		void displayTitle() {
			// Add labels
			alive = true;
			currHealth = 100;
			maxHealth = 100;
			upgrade = 0;
			// shoot = false;
			accessingBlocks = false;
			pos = new Position((short) 0, (short) 0);
			keysDown = 0;
			speed = new Vector(0, 0);
			accel = new Vector(0, 0);
			player = new Position(0, 0);
			bullets = new ArrayList<Position>();
			directionsPressed = new ArrayList<Integer>();
			blocks = new ArrayList<Block>();
			reloadTime = 300;
			points = 0;
			maxSpeed = 800;
			maxAccel = 2000;
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			nameInput.requestFocusInWindow();
			add(nameInput);
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(offlineButton);
			add(new JLabel(""));
			add(new JLabel(""));
			add(serverButton);
			add(new JLabel(""));

		}

		/**
		 * Ensures proper placement
		 * 
		 * @param p
		 * @param radius
		 * @return
		 */
		public boolean withinBounds(Position p, int radius) {
			return (p.getX() - radius > 0 && p.getX() + radius < WIDTH
					&& p.getY() - radius > 0 && p.getY() + radius < HEIGHT);
		}

		public void setBullets(ArrayList<Position> p) {
			bullets = p;
		}

		/**
		 * Draws the player's game
		 */
		@Override
		public void paintComponent(Graphics g) {
			// System.out.println("paint component started");
			super.paintComponent(g);
			if (alive) {
				this.setEnabled(true);
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.WHITE);
				// Draw yourself
				g.setColor(color);
				// Update the center's angle
				try {
					double y = player.getY() - this.getMousePosition().y;
					double x = player.getX() - getMousePosition().x;
					if (x < 0.5 && x > -0.5) {
						angle = (y <= 0 ? 90 : 270);
					} else
						angle = (int) (180 / Math.PI * Math.atan(y / x));

					if (player.getX() > getMousePosition().x)
						angle += 180;
					else if (player.getY() > getMousePosition().y)
						angle += 360;
				} catch (Exception e) {
				}

				// You are drawn as one of the players

				int playerWidth = 10;
				int playerHeight = 10;

				int centerX = getWidth() / 2 - playerWidth / 2;
				int centerY = getHeight() / 2 - playerHeight / 2;
				int displayX = (int) (centerX + 50 * speed.getX() / maxSpeed);
				int displayY = (int) (centerY + 50 * speed.getY() / maxSpeed);

				player.setX((short) displayX);
				player.setY((short) displayY);

				// Draw the background picture
				g.drawImage(back, -1 * back.getWidth() / 2 - pos.getX()
						+ displayX, -1 * back.getHeight() / 2 - pos.getY()
						+ displayY, this);

//				if (points > 1000)
//					upgrade = 1;
//				if (points > 10000)
//					upgrade = 2;
				drawPads(g, upgrade, player, angle, color);

				// Draw your health bar
				g.setColor(Color.GREEN);
				g.fillRect(displayX - 15, displayY - 35,
						(int) (30 * (currHealth * 1.0 / maxHealth)),
						HEALTH_HEIGHT);
				g.setColor(Color.RED);
				g.fillRect(
						displayX
								+ (int) (HEALTH_LENGTH * (currHealth * 1.0 / maxHealth))
								- 15,
						displayY - 35,
						(int) (HEALTH_LENGTH * ((maxHealth - currHealth * 1.0) / maxHealth)),
						HEALTH_HEIGHT);
				Rectangle spawn = new Rectangle(-50 - pos.getX() + displayX,
						-50 - pos.getY() + displayY, 100, 100);
				((Graphics2D) g).fill(spawn);

				// Other players
				Offline.accessingAll = true;
				for (SimplePlayer p : players) {
					if (!p.getColor().equals(color)) {
						drawPads(g, p.getUpgrade(), new Position(player.getX()
								+ p.getPos().getX() - pos.getX(), player.getY()
								+ p.getPos().getY() - pos.getY()),
								p.getAngle(), p.getColor());
						g.fillRect(
								p.getPos().getX()
										+ (int) (HEALTH_LENGTH * (p.getPercentHealth()))
										- 15,
										p.getPos().getY() - 35,
								(int) (HEALTH_LENGTH * (p.getPercentHealth())),
								HEALTH_HEIGHT);
					}
				}
				Offline.accessingAll = false;
				long t1 = System.currentTimeMillis();
				// Bullets
				g.setColor(Color.red);
				for (Position p : bullets) {
					// g.fillsOval(p.getX() - 3 - pos.getX() + displayX,
					// p.getY()
					// - 3 - pos.getY() + displayY, 7, 7);
					Ellipse2D.Double tempBullet = new Ellipse2D.Double(p.getX()
							- 3 - pos.getX() + displayX, p.getY() - 3
							- pos.getY() + displayY, 7, 7);
					((Graphics2D) g).fill(tempBullet);
					if (spawn.contains(new Point((int) tempBullet.getCenterX(),
							(int) tempBullet.getCenterY())))
						points += 1;

					// TODO if (!accessingBlocks)
					// for (int block = 0; block < blocks.size(); block++) {
					// Position blockPos = blocks.get(block).getPos();
					// if (new Rectangle(blockPos.getX() - pos.getX()
					// + player.getX(), blockPos.getY()
					// - pos.getY() + player.getY(), 20, 20)
					// .contains(tempBullet.getCenterX(),
					// tempBullet.getCenterY())) {
					// blocks.remove(block);
					// blocksToRemove.add(blockPos);
					// points += 10;
					// }
					// }
					// g.fillOval(p.getX() - 3 - pos.getX() + displayX, p.getY()
					// - 3 - pos.getY() + displayY, 7, 7);

				}

				for (Block b : blocks) {
					// try{
					accessingBlocks = true;
					g.setColor(b.getColor());
					g.fillRect(b.getPos().getX() - pos.getX() + player.getX(),
							b.getPos().getY() - pos.getY() + player.getY(), 20,
							20);
					// if (new Rectangle(b.getPos().getX(), b.getPos()
					// .getY(), 20, 20).contains(new Point(
					// (int) tempBullet.getCenterX(),
					// (int) tempBullet.getCenterY()))) {
					// points += 10;
					// }
					// blocks.remove(b);
				}

				g.setColor(Color.MAGENTA);
				g.drawString("Health: " + currHealth, 950, 200);
				g.drawString("Points: " + points, 950, 250);

				// Draw blocks

				repaint(40);
			} else {
				g.clearRect(0, 0, getWidth(), getHeight());
				System.out.println("You are dead");
				this.setEnabled(false);
				online = false;
				physics.kill();
				displayTitle();
			}
			// System.out.println("paint component ended");
		}

		void drawPads(Graphics g, int padType, Position center, int angle,
				Color colour) {
			Position[][] pads = {
					{ new Position((short) 10, (short) 30),
							new Position((short) 20, (short) 10),
							new Position((short) 10, (short) 0),
							new Position((short) 0, (short) 10),
							new Position((short) 0, (short) 50),
							new Position((short) 10, (short) 60),
							new Position((short) 20, (short) 50) },
					{ new Position((short) 30, (short) 45),
							new Position((short) 50, (short) 0),
							new Position((short) 60, (short) 10),
							new Position((short) 50, (short) 20),
							new Position((short) 30, (short) 20),
							new Position((short) 20, (short) 20),
							new Position((short) 20, (short) 30),
							new Position((short) 20, (short) 60),
							new Position((short) 20, (short) 70),
							new Position((short) 30, (short) 70),
							new Position((short) 50, (short) 70),
							new Position((short) 60, (short) 80),
							new Position((short) 50, (short) 90),
							new Position((short) 20, (short) 90),
							new Position((short) 0, (short) 90),
							new Position((short) 0, (short) 70),
							new Position((short) 0, (short) 20),
							new Position((short) 0, (short) 0),
							new Position((short) 20, (short) 0) },
					{ new Position((short) 30, (short) 35),
							new Position((short) 60, (short) 20),
							new Position((short) 60, (short) 30),
							new Position((short) 50, (short) 30),
							new Position((short) 50, (short) 20),
							new Position((short) 30, (short) 10),
							new Position((short) 10, (short) 20),
							new Position((short) 10, (short) 50),
							new Position((short) 30, (short) 60),
							new Position((short) 50, (short) 50),
							new Position((short) 50, (short) 40),
							new Position((short) 60, (short) 40),
							new Position((short) 60, (short) 50),
							new Position((short) 60, (short) 50),
							new Position((short) 60, (short) 70),
							new Position((short) 40, (short) 70),
							new Position((short) 30, (short) 70),
							new Position((short) 0, (short) 70),
							new Position((short) 0, (short) 40),
							new Position((short) 0, (short) 30),
							new Position((short) 0, (short) 0),
							new Position((short) 30, (short) 0),
							new Position((short) 40, (short) 0),
							new Position((short) 60, (short) 0),
							new Position((short) 60, (short) 20) } };

			g.setColor(colour);

			GeneralPath padShape;
			// Go through each shape

			padShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
					pads[padType].length);
			// Go through each point and update the points
			for (int point = 0; point < pads[padType].length; point++) {
				// If the point is not the center point
				if (point != 0) {
					// Place the point relative to the player

					pads[padType][point].setX((short) (pads[padType][point]
							.getX() + center.getX() - pads[padType][0].getX()));
					pads[padType][point].setY((short) (pads[padType][point]
							.getY() + center.getY() - pads[padType][0].getY()));
					// Angle the direction of the paddle
					double x1 = pads[padType][point].getX() - center.getX();
					double y1 = pads[padType][point].getY() - center.getY();

					double x2 = x1 * Math.cos(angle * Math.PI / 180) - y1
							* Math.sin(angle * Math.PI / 180);
					double y2 = x1 * Math.sin(angle * Math.PI / 180) + y1
							* Math.cos(angle * Math.PI / 180);

					pads[padType][point].setX((short) (x2 + center.getX()));
					pads[padType][point].setY((short) (y2 + center.getY()));
				}
			}

			padShape.moveTo(pads[padType][1].getX(), pads[padType][1].getY());
			// First point is the center
			int i = 1;
			for (; i < pads[padType].length - 3; i += 3) {
				padShape.curveTo(pads[padType][i].getX(),
						pads[padType][i].getY(), pads[padType][i + 1].getX(),
						pads[padType][i + 1].getY(),
						pads[padType][i + 2].getX(),
						pads[padType][i + 2].getY());
				padShape.lineTo(pads[padType][i + 3].getX(),
						pads[padType][i + 3].getY());
			}
			padShape.curveTo(pads[padType][i].getX(), pads[padType][i].getY(),
					pads[padType][i + 1].getX(), pads[padType][i + 1].getY(),
					pads[padType][i + 2].getX(), pads[padType][i + 2].getY());
			padShape.closePath();

			((Graphics2D) g).fill(padShape);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			this.requestFocusInWindow();
			int key = e.getKeyCode();

			if (!directionsPressed.contains(key)) {
				if (key == KeyEvent.VK_UP) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_LEFT) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_DOWN) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_RIGHT) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_W) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_A) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_S) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				} else if (key == KeyEvent.VK_D) {
					keysDown++;
					directionsPressed.add(key);
					updateAccel();
				}
			}

		}

		/**
		 * Updates the acceleration based on key events
		 */
		void updateAccel() {
			accel.setX(0);
			accel.setY(0);
			if (keysDown == 1) {
				switch (directionsPressed.get(0)) {
				case KeyEvent.VK_UP:
					accel.setY(maxAccel * -1);
					break;
				case KeyEvent.VK_W:
					accel.setY(maxAccel * -1);
					break;
				case KeyEvent.VK_LEFT:
					accel.setX(maxAccel * -1);
					break;
				case KeyEvent.VK_A:
					accel.setX(maxAccel * -1);
					break;
				case KeyEvent.VK_DOWN:
					accel.setY(maxAccel);
					break;
				case KeyEvent.VK_S:
					accel.setY(maxAccel);
					break;
				case KeyEvent.VK_RIGHT:
					accel.setX(maxAccel);
					break;
				case KeyEvent.VK_D:
					accel.setX(maxAccel);
					break;
				}
			} else if (keysDown == 2) {
				int diagonal = (int) (maxAccel / Math.sqrt(2));
				if (directionsPressed.contains(KeyEvent.VK_UP)
						&& directionsPressed.contains(KeyEvent.VK_LEFT)) {
					accel.setX(diagonal * -1);
					accel.setY(diagonal * -1);
				} else if (directionsPressed.contains(KeyEvent.VK_UP)
						&& directionsPressed.contains(KeyEvent.VK_DOWN)) {
				} else if (directionsPressed.contains(KeyEvent.VK_UP)
						&& directionsPressed.contains(KeyEvent.VK_RIGHT)) {
					accel.setX(diagonal);
					accel.setY(diagonal * -1);
				} else if (directionsPressed.contains(KeyEvent.VK_LEFT)
						&& directionsPressed.contains(KeyEvent.VK_DOWN)) {
					accel.setX(diagonal * -1);
					accel.setY(diagonal);
				} else if (directionsPressed.contains(KeyEvent.VK_LEFT)
						&& directionsPressed.contains(KeyEvent.VK_RIGHT)) {
				} else if (directionsPressed.contains(KeyEvent.VK_DOWN)
						&& directionsPressed.contains(KeyEvent.VK_RIGHT)) {
					accel.setX(diagonal);
					accel.setY(diagonal);
				} else if (directionsPressed.contains(KeyEvent.VK_W)
						&& directionsPressed.contains(KeyEvent.VK_A)) {
					accel.setX(diagonal * -1);
					accel.setY(diagonal * -1);
				} else if (directionsPressed.contains(KeyEvent.VK_W)
						&& directionsPressed.contains(KeyEvent.VK_S)) {
				} else if (directionsPressed.contains(KeyEvent.VK_W)
						&& directionsPressed.contains(KeyEvent.VK_D)) {
					accel.setX(diagonal);
					accel.setY(diagonal * -1);
				} else if (directionsPressed.contains(KeyEvent.VK_A)
						&& directionsPressed.contains(KeyEvent.VK_S)) {
					accel.setX(diagonal * -1);
					accel.setY(diagonal);
				} else if (directionsPressed.contains(KeyEvent.VK_A)
						&& directionsPressed.contains(KeyEvent.VK_D)) {
				} else if (directionsPressed.contains(KeyEvent.VK_S)
						&& directionsPressed.contains(KeyEvent.VK_D)) {
					accel.setX(diagonal);
					accel.setY(diagonal);
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

			// Depending on where it is pressed, the directions will be tracked
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_UP) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_LEFT) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_DOWN) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_RIGHT) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_W) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_A) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_S) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			} else if (key == KeyEvent.VK_D) {
				keysDown--;
				directionsPressed.remove((Object) key);
				updateAccel();
			}
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// This is equivalent to shooting a bullet
			this.requestFocusInWindow();
			if (time > reloadTime)
				shoot = true;

		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}
	}

	static class PhysicsThread implements Runnable {
		private Vector accel, velocity, poso;
		private Position pos;
		private long currTime, maxSpeed;
		private BufferedImage back;
		private boolean isRunning;

		PhysicsThread(Vector accel, Vector velocity, Position position,
				int maxSpeed, BufferedImage back) {
			this.accel = accel;
			this.velocity = velocity;
			this.pos = position;
			poso = new Vector(pos.getX(), pos.getY());
			this.maxSpeed = maxSpeed;
			this.back = back;
			isRunning = true;
		}

		public void kill() {
			isRunning = false;
		}

		Vector getVelocityRatio() {
			Vector ratio = new Vector(0, 0);
			double vx = velocity.getX();
			double vy = velocity.getY();

			ratio.setX(vx / maxSpeed);
			ratio.setY(vy / maxSpeed);

			return ratio;
		}

		public void run() {
			while (isRunning) {
				long t1 = System.currentTimeMillis();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long t2 = System.currentTimeMillis();
				int change = (int) (t2 - t1);
				// TODO changing = true;
				// (velocity.getMagnitude() < maxSpeed/2?
				// accel.getX()/2:accel.getX())
				velocity.setX(.96 * (velocity.getX() + accel.getX()
						* (change / 1000.0)));

				velocity.setY(.96 * (velocity.getY() + accel.getY()
						* (change / 1000.0)));

				if (velocity.getMagnitude() > maxSpeed) {
					velocity.setX((velocity.getX() < 0 ? -1 : 1)
							* maxSpeed
							* Math.cos(Math.atan(velocity.getY()
									/ velocity.getX())));
					velocity.setY((velocity.getX() < 0 ? -1 : 1)
							* maxSpeed
							* Math.sin(Math.atan(velocity.getY()
									/ velocity.getX())));
				}

				poso.setX((pos.getX() + velocity.getX() * (change / 1000.0)));
				poso.setY((pos.getY() + velocity.getY() * (change / 1000.0)));
				if (poso.getX() > back.getWidth() / 2 - 30)
					poso.setX(back.getWidth() / 2 - 30);
				else if (poso.getX() < -1 * back.getWidth() / 2 + 30)
					poso.setX(-1 * back.getWidth() / 2 + 30);
				if (poso.getY() > back.getHeight() / 2 - 30)
					poso.setY(back.getHeight() / 2 - 30);
				else if (poso.getY() < -1 * back.getHeight() / 2 + 30)
					poso.setY(-1 * back.getHeight() / 2 + 30);
				// System.out.println(accel.getX() + " " + accel.getY());
				// System.out.println(velocity.getX() + " " + velocity.getY());
				// System.out.println(pos.getX() + " " + pos.getY());
				pos.setX((short) Math.round(poso.getX()));
				pos.setY((short) Math.round(poso.getY()));
				// game.repaint();

				// TODO changing = false;

				// TODO CommunicationsThread in here send to server
				// TODO Message queue (maybe send every other one?
			}
		}
	}

	// TODO make this work
	public boolean withinBounds(Position p, int radius) {
		return (p.getX() - radius > 0 && p.getX() + radius < WIDTH
				&& p.getY() - radius > 0 && p.getY() + radius < HEIGHT);
	}

	public void setBullets(ArrayList<Position> p) {
		bullets = p;
	}

	/**
	 * Keeps track of the elapsed time since a shot was fired
	 */
	class TimerThread implements Runnable {
		public void run() {
			start = System.currentTimeMillis();
			while (true) {

				// restart
				if (shoot)
					start = System.currentTimeMillis();

				// Keep track of the time elapsed
				else
					time = (int) ((System.currentTimeMillis() - start));
			}
		}
	}
}

