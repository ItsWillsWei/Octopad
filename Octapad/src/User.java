import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.io.*;
import java.net.*;

import javax.swing.*;

//import PlayerClient.GamePanel.ServerThread;

public class User extends JFrame {

	private static GamePanel game;

	public User() {
		super("Octopad");
		game = new GamePanel();
		setContentPane(game);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
	}

	public static void main(String[] args) {
		new User().setVisible(true);

	}

	static class GamePanel extends JPanel implements KeyListener, MouseListener {
		private static Socket socket;
		private static InputStream in;
		private static OutputStream out;
		private static GeneralPath button;
		private KButton goButton;
		private KInputPanel nameInput, ipInput, portInput;
		private static final Dimension SCREEN = new Dimension(1024, 768);

		private boolean titleScreen;
		private JButton go;
		private String name;
		private Position pos;

		private int playerType;
		private Position speed;
		private Position accel;
		private double maxSpeed;
		private double maxAccel;

		GamePanel() {
			titleScreen = true;

			// Begin game
			new Thread(new ServerThread()).start();

			setLayout(new GridLayout(5, 5));
			setUpTitle();
			createPlayer();
			repaint(0);

			pos = new Position(0, 0);

			setPreferredSize(SCREEN);
			addMouseListener(this);
			addKeyListener(this);
		}

		void setUpTitle() {
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

			// Listens to actions from the go button
			goButton = new KButton("Go", 300, 100);
			goButton.setBackground(Color.RED);
			goButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Joining server");
					// Connects to the server
					boolean errorConnecting = false;
					try {
						name = nameInput.getInput();
						String ip = ipInput.getInput();
						int port = Integer.parseInt(portInput.getInput());
						socket = new Socket(ip, port);
						in = socket.getInputStream();
						out = socket.getOutputStream();
					} catch (Exception execpt) {
						errorConnecting = true;
						execpt.printStackTrace();
					} finally {
						// Removes all Components if the server connects
						// successfully
						if (errorConnecting) {
							GamePanel.this.removeAll();
							GamePanel.this.repaint();
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

			// Add labels
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(new JLabel(""));
			add(nameInput);
			add(new JLabel(""));
			add(new JLabel(""));
			add(ipInput);
			add(new JLabel(""));
			add(new JLabel(""));
			add(portInput);
			add(new JLabel(""));
			add(new JLabel(""));
			add(goButton);
			add(new JLabel(""));

			displayTitle();
		}

		void displayTitle() {

		}

		void createPlayer() {
			playerType = 1;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.drawRect(pos.getX(), pos.getY(), 10, 10);
			// Graphics2D g2 = (Graphics2D)g; g.fillRect(5, 6, 7, 8); int[]
			// xPoints = {50, 50, 100, 200, 250, 250, 250, 250, 200, 100, 50,
			// 50}; int[] yPoints = {100, 50, 50, 50, 50, 100, 200, 250, 250,
			// 250, 250, 200}; button = new
			// GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);
			// button.moveTo(xPoints[0], yPoints[0]); int i = 0; for(; i < 9;
			// i+=3) { button.curveTo(xPoints[i], yPoints[i],xPoints[i+1],
			// yPoints[i+1], xPoints[i+2], yPoints[i+2]);
			// button.lineTo(xPoints[i+3], yPoints[i+3]); }
			// button.curveTo(xPoints[i], yPoints[i],xPoints[i+1], yPoints[i+1],
			// xPoints[i+2], yPoints[i+2]); button.closePath(); g2.fill(button);

		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// if(button.contains(e.getPoint()))
			// System.out.println(e.getX() + " " + e.getY());
			// while(true)
			// {
			// if(button.contains(e.getPoint()))
			// System.out.println(e.getX() + " " + e.getY());
			// else
			// System.out.println("no");
			// try {
			// Thread.sleep(500);
			// } catch (InterruptedException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			// }
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyPressed(KeyEvent e) {
			System.out.println("key in user");
			int key = e.getKeyCode();
			//TODO this is wrong but i'll fix it
			/*
			 * Have two variables to keep track of both keys.
			 * Both pressed and released
			 * Keep track of number of keys held down? counter
			 * 
			 */
			if (key == KeyEvent.VK_UP && key == KeyEvent.VK_LEFT) {

			} else if (key == KeyEvent.VK_UP && key == KeyEvent.VK_DOWN) {

			} else if (key == KeyEvent.VK_UP && key == KeyEvent.VK_RIGHT) {

			} else if (key == KeyEvent.VK_LEFT && key == KeyEvent.VK_DOWN) {

			} else if (key == KeyEvent.VK_LEFT && key == KeyEvent.VK_RIGHT) {

			} else if (key == KeyEvent.VK_DOWN && key == KeyEvent.VK_RIGHT) {

			}
			// Single Directions
			else if (key == KeyEvent.VK_UP) {

			} else if (key == KeyEvent.VK_LEFT) {

			} else if (key == KeyEvent.VK_DOWN) {

			} else if (key == KeyEvent.VK_RIGHT) {

			}

		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			//Stuff in here
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}

	}

	static class ServerThread implements Runnable {

		ServerThread() {

		}

		public void run() {

		}
	}
	
	//Physics thread?
}
