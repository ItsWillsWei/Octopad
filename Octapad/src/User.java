import java.awt.Dimension;
import java.awt.event.*;
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
		
		GamePanel()
		{
			//Connects to the server
			try {
				String ip = JOptionPane.showInputDialog(null, "Please enter the server's IP address: ", "Enter IP Address", JOptionPane.INFORMATION_MESSAGE);
				int port = Integer.parseInt(JOptionPane.showInputDialog(null, "Please enter the server's port number: ", "Enter Port", JOptionPane.INFORMATION_MESSAGE));
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
			
			setPreferredSize(new Dimension(1024, 768));
			addMouseListener(this);
		}
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
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
