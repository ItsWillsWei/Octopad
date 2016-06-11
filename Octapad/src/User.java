import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

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
		private static final Position CENTER = new Position((int)SCREEN.getWidth()/2, (int)SCREEN.getHeight()/2);

		private boolean titleScreen;
		private JButton go;
		private String name;

		private PhysicsThread physics;
		boolean changing = false;

		private int playerType;
		private Position pos;
		//private Position movingCenter;
		
		private static Vector speed;
		private static Vector accel;
		private static int maxSpeed;
		private int maxAccel;
		private int keysDown;
		private ArrayList<Integer> directionsPressed;

		GamePanel() {
			titleScreen = true;

			// Begin game

			setLayout(new GridLayout(5, 5));
			setUpTitle();
			createPlayer();
			repaint(0);

			pos = new Position(0, 0);
			//movingCenter =new Position(pos.getX(), pos.getY());
			keysDown = 0;
			speed = new Vector(0, 0);
			accel = new Vector(0, 0);
			directionsPressed = new ArrayList<Integer>();

			// Pixels per second
			maxSpeed = 200;
			// Pixels per second^2
			maxAccel = 500;

			// new Thread(new TimerThread()).start();
			physics = new PhysicsThread(accel, speed, pos, maxSpeed);
			new Thread(physics).start();

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

			int playerWidth = 10;
			int playerHeight = 10;
			
			double velocityRatio = speed.getMagnitude()/maxSpeed;
			int centerX = getWidth() / 2 - playerWidth / 2;
			int centerY = getHeight() / 2 - playerHeight / 2;
			int displayX = (int)(centerX + 50*speed.getX()/maxSpeed);
			int displayY = (int) (centerY + 50*speed.getY()/maxSpeed);
//			int displayX = (int) (getWidth() / 2 - playerWidth / 2 + (pos.getX()));
//			int displayY = (int) (getHeight() / 2 - playerHeight / 2 + (pos.getY()));

			Vector centerDistance = new Vector(displayX-(CENTER.getX()-playerWidth), displayY-(CENTER.getY()-playerHeight));
			Vector correct = new Vector(0,0);
			
			correct.setX((speed.getX() < 0 ? -1 : 1)
					* maxSpeed
					* Math.cos(Math.atan(speed.getY()
							/ speed.getX())));
			correct.setY((speed.getX() < 0 ? -1 : 1)
					* 50* velocityRatio
					* Math.sin(Math.atan(speed.getY()
							/ speed.getX())));
			
//			if(speed.getMagnitude()/maxSpeed < centerDistance.getMagnitude()/50){
//				displayX = (int) (getWidth() / 2 - playerWidth / 2 + (pos.getX()));
//				displayY = (int) (getHeight() / 2 - playerHeight / 2 + (pos.getY()));
//			}
//			else{
//				
//			}
//			
			// Draws player
			g.drawRect(displayX, displayY, playerWidth, playerHeight);

			Graphics2D g2 = (Graphics2D) g;

			int[] xPoints = { 50, 50, 100, 200, 250, 250, 250, 250, 200, 100,
					50, 50 };
			int[] yPoints = { 100, 50, 50, 50, 50, 100, 200, 250, 250, 250,
					250, 200 };
			
			
//			movingCenter.setX((int)(movingCenter.getX() + (displayX - (CENTER.getX()-playerWidth/2))*(1-velocityRatio)));
//			movingCenter.setY((int)(movingCenter.getY() + (displayY - (CENTER.getY()-playerHeight/2))*(1-velocityRatio)));
			
			for (int i = 0; i < xPoints.length; i++) {
				xPoints[i] = xPoints[i] - pos.getX() + displayX;// xPoints[i] -
																// pos.getX()+
																// 50 -
																// playerDisp.getX()/2;
			}
			for (int i = 0; i < xPoints.length; i++) {
				yPoints[i] = yPoints[i] - pos.getY() + displayY;// yPoints[i] -
																// pos.getY()+
																// 50 -
																// playerDisp.getY()/2;
			}
			
			g.fillRect(32 - pos.getX() + displayX, 46 - pos.getY() + displayY,
					55, 10);
			g.fillRect(346 - pos.getX() + displayX,
					689 - pos.getY() + displayY, 7, 8);
			g.setColor(Color.RED);
			g.fillOval(400 - pos.getX() + displayX,
					400 - pos.getY() + displayY, 100, 100);
			
//			g.fillRect(32 - movingCenter.getX() + displayX, 46 - movingCenter.getY() + displayY,
//					55, 10);
//			g.fillRect(346 - movingCenter.getX() + displayX,
//					689 - movingCenter.getY() + displayY, 7, 8);
//			g.setColor(Color.RED);
//			g.fillOval(400 - movingCenter.getX() + displayX,
//					400 - movingCenter.getY() + displayY, 100, 100);
			button = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);
			button.moveTo(xPoints[0], yPoints[0]);
			int i = 0;
			for (; i < 9; i += 3) {
				button.curveTo(xPoints[i], yPoints[i], xPoints[i + 1],
						yPoints[i + 1], xPoints[i + 2], yPoints[i + 2]);
				button.lineTo(xPoints[i + 3], yPoints[i + 3]);
			}
			button.curveTo(xPoints[i], yPoints[i], xPoints[i + 1],
					yPoints[i + 1], xPoints[i + 2], yPoints[i + 2]);
			button.closePath();
			g2.fill(button);
			// System.out.println(button.contains(MouseInfo.getPointerInfo()
			// .getLocation().x,
			// MouseInfo.getPointerInfo().getLocation().y));
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// repaint(0);
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

			// Single Directions
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

		@Override
		public void keyReleased(KeyEvent e) {
			// Stuff in here
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

		void updateAccel() {
			accel.setX(0);
			accel.setY(0);
			System.out.println(directionsPressed);
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
					// Nothing
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
					// Nothing
				} else if (directionsPressed.contains(KeyEvent.VK_DOWN)
						&& directionsPressed.contains(KeyEvent.VK_RIGHT)) {
					accel.setX(diagonal);
					accel.setY(diagonal);
				}
			}
			repaint(0);
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}

	}

	// static class ServerThread implements Runnable {
	//
	// ServerThread() {
	// new Thread(new TimerThread()).start();
	// }
	//
	// public void run() {
	//
	// }
	//
	// }
	//
	// /**
	// * Keeps track of the time elapsed since a player's turn began
	// */
	// static class TimerThread implements Runnable {
	// public void run() {
	// //long start = System.currentTimeMillis();
	// while (true) {
	// // Do not run the timer if it is not the player's turn
	// ///if (turn == false)
	// //start = System.currentTimeMillis();
	// // Keep track of the time elapsed in seconds
	// ///else {
	// //time = (int) ((System.currentTimeMillis() - start) / 1000);
	// game.repaint(0);
	// //}
	// }
	// }
	// }

	// Physics thread?
	static class PhysicsThread implements Runnable {
		private Vector accel, velocity, poso;
		private Position pos;
		private long currTime, maxSpeed;

		PhysicsThread(Vector accel, Vector velocity, Position position,
				int maxSpeed) {
			this.accel = accel;
			this.velocity = velocity;
			this.pos = position;
			poso = new Vector(pos.getX(), pos.getY());
			this.maxSpeed = maxSpeed;
		}

		// public void changeDirection() {
		// currTime = System.currentTimeMillis();
		// }
		
		
		
		
		
		Vector getVelocityRatio() {
			Vector ratio = new Vector(0, 0);
			double vx = velocity.getX();
			double vy = velocity.getY();

			ratio.setX(vx / maxSpeed);
			ratio.setY(vy / maxSpeed);

			return ratio;
		}

		public void run() {
			while (true) {
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
				// System.out.println(accel.getX() + " " + accel.getY());
				System.out.println(velocity.getX() + " " + velocity.getY());
				// System.out.println(pos.getX() + " " + pos.getY());
				pos.setX((int) Math.round(poso.getX()));
				pos.setY((int) Math.round(poso.getY()));
				game.repaint();

				// TODO changing = false;

				// TODO CommunicationsThread in here send to server
				// TODO Message queue (maybe send every other one?
			}
		}
	}
}
