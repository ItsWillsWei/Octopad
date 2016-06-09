import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PlayerClient extends JFrame {
	// Final variables for the pane
	private int WIDTH = 1024;
	private int HEIGHT = 700;
	private static GamePanel game;

	/**
	 * Creates a new PlayerClient object
	 */
	public PlayerClient() {
		super("Octopad");
		game = new GamePanel();
		setContentPane(game);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
	}

	public static void main(String[] args) throws Exception {
		PlayerClient p = new PlayerClient();
		p.setVisible(true);
	}

	static class GamePanel extends JPanel implements MouseListener {

		private static int time;
		private static boolean timedOut;

		// Relevant and important stuff
		private static Socket sock;
		private static BufferedReader br;
		private static PrintWriter pw;

		Color c;
		private boolean alive = true;
		private static String ip = "localhost";
		private static int port = 421;
		private Position pos;
		private int angle;
		private int upgrade = 3;
		private boolean shoot = false;
		private int reloadTime = 1000;
		private int points = 0;
		private ArrayList<Position> bullet = new ArrayList<Position>();
		private ArrayList<tempPlayer> players = new ArrayList<tempPlayer>();

		/**
		 * Creates a new GamePanel
		 */
		public GamePanel() {
			// Connects to the server
			try {
				// String ip = JOptionPane.showInputDialog(null,
				// "Please enter the server's IP address: ", "Enter IP Address",
				// JOptionPane.INFORMATION_MESSAGE);
				// int port = Integer.parseInt(JOptionPane.showInputDialog(null,
				// "Please enter the server's port number: ", "Enter Port",
				// JOptionPane.INFORMATION_MESSAGE));
				sock = new Socket(ip, port);
				br = new BufferedReader(new InputStreamReader(
						sock.getInputStream()));
				pw = new PrintWriter(sock.getOutputStream());
				String[] command = br.readLine().split(" ");
				pos = new Position(Integer.parseInt(command[0]),
						Integer.parseInt(command[1]));
				c = new Color(Integer.parseInt(command[2]),
						Integer.parseInt(command[3]),
						Integer.parseInt(command[4]));
				System.out.println(pos.getX() + " " + pos.getY());
				repaint(0);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Begin game
			new Thread(new ServerThread()).start();

			setPreferredSize(new Dimension(1024, 700));
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			if (alive) {
				System.out.println("Repainting" + bullet.size());
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(Color.WHITE);
				g.clearRect(0, 0, getWidth(), getHeight());

				// Draw yourself
				g.setColor(c);
				g.fillOval(pos.getX() - 15, pos.getY() - 15, 30, 30);

				// Bullets
				g.setColor(Color.red);
				for (Position p : bullet) {
					// System.out.println("attempting to draw");
					g.fillOval(p.getX() - 3, p.getY() - 3, 7, 7);
				}

				for (tempPlayer p : players) {
					g.setColor(p.getColor());
					g.fillOval(p.getPos().getX() - 15, p.getPos().getY() - 15,
							30, 30);
				}
			} else
				this.setEnabled(false);

		}

		/**
		 * Keeps track of the time elapsed since a player's turn began
		 */
		class TimerThread implements Runnable {
			public void run() {
				long start = System.currentTimeMillis();
				while (true) {
					// Do not run the timer if it is not the player's turn
					if (shoot) {
						start = System.currentTimeMillis();
					}
					// Keep track of the time elapsed in seconds
					else {
						time = (int) ((System.currentTimeMillis() - start));
						// GamePanel.this.repaint(0);
					}
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
				while (true) {
					// Read in the server's command (if any)
					String[] command = null;

					do {
						try {
							command = br.readLine().split(" ");
						} catch (IOException e) {
							e.printStackTrace();
						}
					} while (command == null);
					System.out.println(command[0]);
					switch (Integer.parseInt(command[0])) {
					// PLace object
					case 1:
						// showTime = false;
						int[][] move = new int[2][2];
						move[0][0] = Integer.parseInt(command[1]);
						move[0][1] = Integer.parseInt(command[2]);
						move[1][0] = Integer.parseInt(command[3]);

						break;
					// Place player
					case 2:
						// colour = Integer.parseInt(command[1]);
						GamePanel.this.repaint(0);
						break;
					// Update health
					case 3:
						int health = Integer.parseInt(command[1]);
						GamePanel.this.repaint(0);
						break;
					// Request upgrade
					case 4:
						// upgrade option =true
						GamePanel.this.repaint(0);
						break;
					// Awards points
					case 5:
						points += Integer.parseInt(command[1]);
						GamePanel.this.repaint(0);
						break;
					// Timed out or dead
					case 6:
						alive = false;
						timedOut = true;
						// TODO end here but just testing right now
						System.exit(0);
						break;
					// Requesting information
					case 7:
						angle++;
						if (shoot)
							pw.println(pos.getX() + " " + pos.getY() + " "
									+ angle + " 1 " + upgrade);
						else
							pw.println(pos.getX() + " " + pos.getY() + " "
									+ angle + " 0 " + upgrade);
						pw.flush();
						shoot = false;
						GamePanel.this.repaint(0);
						// System.exit(0);
						break;
					// Sending any new objects
					case 8:

						// any bullets in the area
						int index = 1;
						int count = Integer.parseInt(command[index]);
						index++;
						for (int i = 0; i < count; i++) {
							int x = Integer.parseInt(command[index]);
							index++;
							int y = Integer.parseInt(command[index]);
							index++;
							bullet.add(new Position(x, y));
						}
						// GamePanel.this.repaint(0);
						count = Integer.parseInt(command[index]);
						index++;

						// sending all players in your area
						for (int i = 0; i < count; i++) {
							int x = Integer.parseInt(command[index]);
							index++;
							int y = Integer.parseInt(command[index]);
							index++;
							int r = Integer.parseInt(command[index]);
							index++;
							int g = Integer.parseInt(command[index]);
							index++;
							int b = Integer.parseInt(command[index]);
							index++;
							int upgrade = Integer.parseInt(command[index]);
							index++;
							Color c = new Color(r, g, b);
							players.add(new tempPlayer(new Position(x, y),
									new Color(r, g, b), upgrade));
							System.out.println(c);
						}
						repaint(0);
						// TODO change the graphics based on this information

						break;

					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		// TODO make sure this works
		@Override
		public void mouseClicked(MouseEvent arg0) {

			angle = (int) Math.atan((arg0.getPoint().y - HEIGHT / 2)
					/ (arg0.getPoint().x - WIDTH / 2));
			System.out.println(time + "ssssssssssss");
			if (time > reloadTime)
				shoot = true;
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
	}
}

class tempPlayer {
	private Position pos;
	private Color c;
	private int up;

	tempPlayer(Position p, Color col, int upgrade) {
		pos = p;
		c = col;
		up = upgrade;
	}

	public Position getPos() {
		return pos;
	}

	public Color getColor() {
		return c;
	}

	public int getUp() {
		return up;
	}
}
