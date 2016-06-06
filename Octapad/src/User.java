import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.io.*;
import java.net.*;

import javax.swing.*;

//import PlayerClient.GamePanel.ServerThread;


public class User extends JFrame{

	private static GamePanel game;
	public User(){
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

	static class GamePanel extends JPanel implements KeyListener, MouseListener{
		private static Socket socket;
		private static InputStream in;
		private static OutputStream out;
		private static GeneralPath button;
		private KButton goButton;
		private KInputPanel nameInput, ipInput, portInput;
		private static final Dimension SCREEN = new Dimension(1024, 768);
		
		private boolean titleScreen;
		private JButton go;
		
		private int playerType;
		private Position speed;
		private double maxSpeed;
		private double accel;
		
		GamePanel()
		{
			titleScreen = true;
			//Connects to the server
			try {
				String ip = "localhost";//JOptionPane.showInputDialog(null, "Please enter the server's IP address: ", "Enter IP Address", JOptionPane.INFORMATION_MESSAGE);
				int port = 421;//Integer.parseInt(JOptionPane.showInputDialog(null, "Please enter the server's port number: ", "Enter Port", JOptionPane.INFORMATION_MESSAGE));
				socket = new Socket(ip, port);
				in = socket.getInputStream();
				out = socket.getOutputStream();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Begin game
			new Thread(new ServerThread()).start();
			
			setLayout(new GridLayout(5,5));
			setUpTitle();
			createPlayer();
			repaint(0);
			
			setPreferredSize(SCREEN);
			addMouseListener(this);
		}
		
		void setUpTitle(){
			nameInput = new KInputPanel("Name: ");
			nameInput.setBackground(Color.red);
			nameInput.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					System.out.println("Joining server");
					
					
				}
				
			});
			
			ipInput = new KInputPanel("IP: ");
			ipInput.setBackground(Color.RED);
			ipInput.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					System.out.println("Joining server");
					
					
				}
				
			});
			
			portInput = new KInputPanel("Port: ");
			portInput.setBackground(Color.RED);
			portInput.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					System.out.println("Joining server");
					
					
				}
				
			});
			
			goButton = new KButton("Go", 300, 100);
			goButton.setBackground(Color.RED);
			goButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					System.out.println("Joining server");
					
					
				}
				
			});
			
			
			add(new JLabel("a"));
			add(new JLabel("b"));
			add(new JLabel("c"));
			add(new JLabel("d"));
			add(nameInput);
			add(ipInput);
			add(portInput);
			add(goButton);
			add(new JLabel("a"));
			add(new JLabel("b"));
			add(new JLabel("c"));
			add(new JLabel("d"));
			
			displayTitle();
		}
		
		void displayTitle(){
			
		}
		
		void createPlayer(){
			playerType = 1;
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			g.fillRect(5, 6, 7, 8);
			int[] xPoints = {50, 50, 100, 200, 250, 250, 250, 250, 200, 100, 50, 50};
			int[] yPoints = {100, 50, 50, 50, 50,   100, 200, 250, 250, 250, 250, 200};
			button = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);
			button.moveTo(xPoints[0], yPoints[0]);
			int i = 0;
			for(; i < 9; i+=3)
			{
				button.curveTo(xPoints[i], yPoints[i],xPoints[i+1], yPoints[i+1], xPoints[i+2], yPoints[i+2]);
				button.lineTo(xPoints[i+3], yPoints[i+3]);
			}
			button.curveTo(xPoints[i], yPoints[i],xPoints[i+1], yPoints[i+1], xPoints[i+2], yPoints[i+2]);
			button.closePath();
			g2.fill(button);
		}
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
			System.out.println("click");
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			System.out.println("Entered");
			if(button.contains(e.getPoint()))
				System.out.println(e.getX() + " " + e.getY());
//			while(true)
//			{
//				if(button.contains(e.getPoint()))
//					System.out.println(e.getX() + " " + e.getY());
//				else
//					System.out.println("no");
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
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
		public void keyPressed(KeyEvent arg0) {
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}
		
	}
	
	static class ServerThread implements Runnable{
		
		ServerThread()
		{
			
		}
		public void run()
		{
			
		}
	}
}
