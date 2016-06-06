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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PlayerClient extends JFrame {

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

		// Final variables for drawing the board
		private static final int SPACE = 50;
		private static final int DIAMETER = 40;
		private static final int BORDER = 2;
		private static final int WIDTH = SPACE * 11 + DIAMETER;
		private static final int HEIGHT = (int) (16 * SPACE * Math.sqrt(3) / 2 + DIAMETER);

		private static final Color[] PLAYERS = { Color.BLACK, Color.RED,
				Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE,
				Color.MAGENTA.darker() };

		private static int colour, winner = 0, time;

		private static int[] selectedCoord;
		private static int[][] board;
		private static int[][][] boardCoords;
		private static boolean turn, showTime = false, selected, invalid,
				timedOut;

		// Relevant and important stuff
		private static Socket sock;
		private static BufferedReader br;
		private static PrintWriter pw;

		private static String ip = "localhost";
		private static int port = 421;
		private Position pos;
		private int angle;
		private int upgrade;
		private boolean shoot = false;

		/**
		 * Creates a new GamePanel
		 */
		public GamePanel() {
			board = new int[17][17];
			boardCoords = new int[17][17][2];
			colour = 0;

			// Fills the board with invalid and available spaces
			for (int i = 0; i < 17; i++)
				for (int j = 0; j < 17; j++)
					board[i][j] = -1;
			for (int i = 4; i <= 12; i++)
				for (int j = 4; j <= 12; j++)
					board[i][j] = 0;
			fillTriangle(-1, board, 0, 16, 12);
			fillTriangle(1, board, 0, 9, 13);
			fillTriangle(1, board, 0, 0, 4);
			fillTriangle(-1, board, 0, 7, 3);

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
				System.out.println(pos.getX() + " " + pos.getY());

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Begin game
			new Thread(new ServerThread()).start();

			setPreferredSize(new Dimension(1024, 768));
			addMouseListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.WHITE);
			g.clearRect(0, 0, getWidth(), getHeight());

			// Drawns the board
			for (int i = 0; i < 17; i++) {
				for (int j = 0; j < 17; j++) {
					if (board[i][j] != -1) {
						int x = (int) (getWidth() / 2 - WIDTH / 2 + 4 * SPACE
								/ 2 - DIAMETER / 2)
								+ (j * SPACE - i * SPACE / 2);
						int y = (int) ((getHeight() / 2 - HEIGHT / 2) + (i
								* SPACE * Math.sqrt(3) / 2));
						boardCoords[i][j][0] = x + DIAMETER / 2;
						boardCoords[i][j][1] = y + DIAMETER / 2;
						g.setColor(Color.BLACK);
						g.fillOval(x - BORDER, y - BORDER, DIAMETER + 2
								* BORDER, DIAMETER + 2 * BORDER);
						// 0 = empty position
						if (board[i][j] == 0) {
							g.setColor(Color.LIGHT_GRAY);
							// 1-6 player positions
						} else if (board[i][j] == 1) {
							g.setColor(Color.RED);
						} else if (board[i][j] == 2) {
							g.setColor(Color.ORANGE);
						} else if (board[i][j] == 3) {
							g.setColor(Color.YELLOW);
						} else if (board[i][j] == 4) {
							g.setColor(Color.GREEN);
						} else if (board[i][j] == 5) {
							g.setColor(Color.BLUE);
						} else if (board[i][j] == 6) {
							g.setColor(Color.MAGENTA.darker());
						}

						// Draw a selected piece darker than the other pieces
						if (selected && selectedCoord[0] == i
								&& selectedCoord[1] == j) {
							Color c = g.getColor();
							Color n = c.darker();
							g.setColor(n);
						}
						g.fillOval(x, y, DIAMETER, DIAMETER);
					}
				}
			}

			// Draw the player's colour
			g.setColor(PLAYERS[colour]);
			g.setFont(g.getFont().deriveFont(Font.PLAIN, 24));
			if (colour > 0) {
				g.drawString("You are player: " + colour, 5, 36);
			} else {
				g.drawString("You have not yet been assigned a colour", 5, 36);
			}
			g.setColor(Color.BLACK);

			// Information messages
			if (turn) {
				g.drawString("It is your turn", 5, 60);
			}
			if (showTime) {
				g.drawString("Time: " + time, 170, 60);
			}
			if (invalid) {
				g.drawString("You have made an invalid move", 5, 80);
			}
			if (timedOut) {
				g.drawString("You have timed out", 5, 100);
			}
			if (winner != 0) {
				g.drawString("Player " + winner + " has won!", 5, 120);
			}
		}

		/**
		 * Fills the board array with integers that correspond to each player
		 * 
		 * @param attitude
		 *            whether or not the triangle is upright or inverted
		 * @param board
		 *            the board
		 * @param player
		 *            the number of the player from 1 to 6
		 * @param row
		 *            the row of the tip of the triangle
		 * @param col
		 *            the col of the tip of the triangle
		 */
		public static void fillTriangle(int attitude, int[][] board,
				int player, int row, int col) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j <= i; j++) {
					board[attitude > 0 ? row + i : row - i][attitude > 0 ? col
							+ j : col - j] = player;
				}
			}
		}

		/**
		 * Keeps track of the time elapsed since a player's turn began
		 */
		class TimerThread implements Runnable {
			public void run() {
				long start = System.currentTimeMillis();
				while (true) {
					// Do not run the timer if it is not the player's turn
					if (turn == false)
						start = System.currentTimeMillis();
					// Keep track of the time elapsed in seconds
					else {
						time = (int) ((System.currentTimeMillis() - start) / 1000);
						GamePanel.this.repaint(0);
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
						showTime = false;
						int[][] move = new int[2][2];
						move[0][0] = Integer.parseInt(command[1]);
						move[0][1] = Integer.parseInt(command[2]);
						move[1][0] = Integer.parseInt(command[3]);

						break;
					// Place player
					case 2:
						colour = Integer.parseInt(command[1]);
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
						int points = Integer.parseInt(command[1]);
						GamePanel.this.repaint(0);
						break;
					// Timed out
					case 6:
						turn = false;
						showTime = false;
						timedOut = true;
						selected = false;
						GamePanel.this.repaint(0);
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
						break;
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent arg0) {

			if (turn) {
				for (int i = 0; i < 17; i++) {
					for (int j = 0; j < 17; j++) {
						if (Math.pow(boardCoords[i][j][0] - arg0.getX(), 2)
								+ Math.pow(boardCoords[i][j][1] - arg0.getY(),
										2) < (DIAMETER / 2) * (DIAMETER / 2)) {
							if (!selected) {
								if (board[i][j] == colour) {
									selectedCoord = new int[] { i, j };
									selected = true;
								}
							} else {
								selected = false;
								if (i != selectedCoord[0]
										|| j != selectedCoord[1]) {
									turn = false;
									pw.println("1 " + selectedCoord[0] + " "
											+ selectedCoord[1] + " " + i + " "
											+ j);
									pw.flush();
								}
							}
						}
					}
				}
			}
			repaint(0);
		}

		// TODO make sure this works
		@Override
		public void mouseClicked(MouseEvent arg0) {
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
