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
public class PlayerClient extends JFrame {
	// Final variables for the pane
	private static GamePanel game;

	/**
	 * Creates a new PlayerClient object
	 */
	public PlayerClient() {
		super("Octopad");
		game = new GamePanel();
		setContentPane(game);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
	}

	// Starts the player client
	public static void main(String[] args) throws Exception {
		PlayerClient p = new PlayerClient();
		p.setVisible(true);
	}

	/**
	 * The GamePanel class that keeps track of the game and handles graphics
	 */
	static class GamePanel extends JPanel implements MouseListener, KeyListener {
		// Server variables
		private static Socket sock;
		private static DataInputStream in;
		private static DataOutputStream out;
		private static String ip = "localhost";//"10.242.165.88";
		private static int port = 421;

		// Player information
		Color c;
		private short maxHealth;
		private short currHealth;
		private static long start;
		private boolean alive = true;
		public boolean online, accessingBlocks;

		private ArrayList<Block> blocks;

		// Display information variables
		private KButton serverButton, offlineButton;
		private KInputPanel nameInput, ipInput, portInput;
		private static final Dimension SCREEN = new Dimension(1024, 768);
		private BufferedImage back;
		private final static byte HEALTH_HEIGHT = 5;
		private static byte HEALTH_LENGTH = 30;

		// Physics variables
		private Position pos, player;
		private static Vector speed;
		private static Vector accel;
		private int angle;
		private static int maxSpeed;
		private int maxAccel;
		private int keysDown;
		private ArrayList<Integer> directionsPressed;
		private PhysicsThread physics;
		private ServerThread server;
		boolean changing = false;
		private byte upgrade;
		private boolean shoot;
		private short reloadTime;
		private int points;
		private ArrayList<Position> bullet;;
		private ArrayList<tempPlayer> players;
		private static int time;

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
			nameInput = new KInputPanel("Name: ");
			nameInput.setBackground(Color.red);
			nameInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//System.out.println("Name");
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
			offlineButton.setBackground(Color.GREEN);
			offlineButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					online = false;
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
					//System.out.println("Joining server");
					online = true;
					// Connects to the server
					boolean errorConnecting = false;
					try {
						sock = new Socket(ip, port);
						in = new DataInputStream(sock.getInputStream());
						out = new DataOutputStream(sock.getOutputStream());
						currHealth = maxHealth;

						// Send the name
						// out.writeShort(1);

						// Read in the color and position
						short x = in.readShort();
						short y = in.readShort();
						short r = in.readShort();
						short g = in.readShort();
						short b = in.readShort();
						pos = new Position(x, y);
						c = new Color(r, g, b);
						//System.out.println(pos.getX() + " " + pos.getY());
						//System.out.println(c);

						// Starts the communication witht the server and the
						// physics engine
						server = new ServerThread();
						new Thread(server).start();
						physics = (new PhysicsThread(accel, speed, pos,
								maxSpeed, back));
						new Thread(physics).start();

					} catch (Exception execpt) {
						// errorConnecting = true;
						execpt.printStackTrace();
					} finally {
						// Removes all Components if the server connects
						// successfully
						if (!errorConnecting) {
							GamePanel.this.removeAll();
							// GamePanel.this.repaint();
							GamePanel.this.requestFocusInWindow();
							// Display Error message
						} else {
							JOptionPane
									.showMessageDialog(
											null,
											"You have entered an invald IP/Port combination.",
											"Warning",
											JOptionPane.ERROR_MESSAGE);
						}
					}

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
			shoot = false;
			accessingBlocks = false;
			pos = new Position((short) 0, (short) 0);
			keysDown = 0;
			speed = new Vector(0, 0);
			accel = new Vector(0, 0);
			player = new Position(0, 0);
			bullet = new ArrayList<Position>();
			directionsPressed = new ArrayList<Integer>();
			players = new ArrayList<tempPlayer>();
			blocks = new ArrayList<Block>();
			reloadTime = 100;
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
		 * Draws the player's game
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (alive) {
				this.setEnabled(true);
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.WHITE);
				// Draw yourself
				g.setColor(c);
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

				if (points > 500)
					upgrade = 1;
				if (points > 1000)
					upgrade = 2;
				drawPads(g, upgrade, player, angle, c);

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
				for (tempPlayer p : players) {
					if (!p.getColor().equals(c)) {
						drawPads(g, p.getUpgrade(), new Position(player.getX()
								+ p.getPos().getX() - pos.getX(), player.getY()
								+ p.getPos().getY() - pos.getY()),
								p.getAngle(), p.getColor());
					}
				}

				// Bullets
				g.setColor(Color.red);
				for (Position p : bullet) {
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
				}

				for (Block b : blocks) {
					// try{
					accessingBlocks = true;
					g.setColor(b.getColor());
					g.fillRect(b.getPos().getX() - pos.getX() + player.getX(),
							b.getPos().getY() - pos.getY() + player.getY(), 20,
							20);
				}

				g.setColor(Color.BLACK);
				g.drawString("Health: " + currHealth, 950, 200);
				g.drawString("Points: " + points, 950, 250);

				// Draw blocks

				repaint(100);
			} else {
				g.clearRect(0, 0, getWidth(), getHeight());
				//System.out.println("You are dead");
				this.setEnabled(false);
				online = false;
				physics.kill();
				displayTitle();
			}

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

		/**
		 * Keeps track of the server's input
		 */
		class ServerThread implements Runnable {

			@Override
			public void run() {
				// Initialize the timer
				new Thread(new TimerThread()).start();

				// Continuously listening
				while (alive) {

					long time = System.currentTimeMillis();

					try {
						Thread.sleep(3);

						// The command
						short curr = in.readShort();
						while (curr == 0) {
							Thread.sleep(2);
						}

						switch (curr) {
						// PLace object
						// Update health
						case 3:
							currHealth = in.readShort();
							break;
						// Awards points
						case 5:
							points = in.readShort();
							// GamePanel.this.repaint(0);
							break;
						// Timed out or dead
						case 6:
							alive = false;
							break;
						// Requesting information
						case 7:
							time = System.currentTimeMillis();
							out.writeShort(pos.getX());
							out.writeShort(pos.getY());
							out.writeShort(angle);
							out.writeShort(upgrade);
							out.writeBoolean(shoot);
							out.flush();
							shoot = false;
							// System.out.println("7 took: "
							// + (System.currentTimeMillis() - time));
							break;
						// Sending any new objects
						case 8:
							time = System.currentTimeMillis();
							ArrayList<Position> currBullets = new ArrayList<Position>();
							// any bullets in the area
							short count = in.readShort();

							for (int i = 0; i < count; i++) {
								short x = in.readShort();
								short y = in.readShort();
								// if (Math.abs(x - pos.getX()) < SCREEN.width
								// && Math.abs(y - pos.getY()) < SCREEN.width)
								currBullets.add(new Position(x, y));
							}

							bullet = currBullets;
							short count1 = in.readShort();

							ArrayList<tempPlayer> currPlayers = new ArrayList<tempPlayer>();
							// sending all players in your area
							for (int i = 0; i < count1; i++) {
								short x = in.readShort();
								short y = in.readShort();
								short r = in.readShort();
								short g = in.readShort();
								short b = in.readShort();
								short upgrade = in.readShort();
								short angle = in.readShort();
								currPlayers
										.add(new tempPlayer(new Position(x, y),
												new Color(r, g, b), upgrade,
												angle));
							}
							players = currPlayers;

							break;
						case 10:

							// Retrieves the blocks' information
							short addOrRemove = in.readShort();
							// System.out.println("receive 10, sub: "
							// + addOrRemove);
							if (addOrRemove == 1) {
								short x = in.readShort();
								short y = in.readShort();
								short r = in.readShort();
								short g = in.readShort();
								short b = in.readShort();
								// Add one block
								Block nextBlock = new Block(new Position(x, y),
										20, new Color(1, 1, 1));
								ArrayList<Block> tempBlocks = (ArrayList<Block>) blocks
										.clone();
								tempBlocks.add(nextBlock);
								blocks = tempBlocks;

							} else {
								short x = in.readShort();
								short y = in.readShort();
								//System.out.println(x + " " + y);
								ArrayList<Block> tempBlocks = (ArrayList<Block>) blocks
										.clone();
								for (int i = 0; i < tempBlocks.size(); i++) {
									Block b = tempBlocks.get(i);
									if (b.getPos().getX() == x
											&& b.getPos().getY() == y) {
										tempBlocks.remove(b);
										break;
									}
								}
								blocks = tempBlocks;

								accessingBlocks = false;
							}
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
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
				case KeyEvent.VK_LEFT:
					accel.setX(maxAccel * -1);
					break;
				case KeyEvent.VK_DOWN:
					accel.setY(maxAccel);
					break;
				case KeyEvent.VK_RIGHT:
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

	/**
	 * Takes care of the movements, acceleration etc.
	 *
	 */
	static class PhysicsThread implements Runnable {
		// Required variables
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
				pos.setX((short) Math.round(poso.getX()));
				pos.setY((short) Math.round(poso.getY()));
			}
		}
	}

}

class tempPlayer {
	private Position pos;
	private Color c;
	private short upgrade, angle;

	tempPlayer(Position p, Color col, short upgrade, short angle) {
		pos = p;
		c = col;
		this.upgrade = upgrade;
		this.angle = angle;
	}

	public Position getPos() {
		return pos;
	}

	public Color getColor() {
		return c;
	}

	public int getUpgrade() {
		return upgrade;
	}

	public int getAngle() {
		return angle;
	}
}
