import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class AIClient {
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
	private int speed = 2;

	public static void main(String[]args){
		new AIClient();
	}
	
	public AIClient() {
		// Connects to the server
		try {
			sock = new Socket(ip, port);
			br = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
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
		randomMovements();
	}

	public void randomMovements(){
		angle = (int) (Math.random()*360);
		double rad = angle/360.0*Math.PI;
		Point velocity = new Point();
		velocity.setLocation(speed*Math.cos(rad), speed*Math.sin(rad));
	}
	
	/**
	 * Keeps track of the server's input
	 */
	class ServerThread implements Runnable {

		@Override
		public void run() {
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
					int[][] move = new int[2][2];
					move[0][0] = Integer.parseInt(command[1]);
					move[0][1] = Integer.parseInt(command[2]);
					move[1][0] = Integer.parseInt(command[3]);

					break;
				// Place player
				case 2:

					break;
				// Update health
				case 3:
					int health = Integer.parseInt(command[1]);
					break;
				// Request upgrade
				case 4:
					// upgrade option =true
					break;
				// Awards points
				case 5:
					int points = Integer.parseInt(command[1]);

					break;
				// Timed out
				case 6:
					// turn = false;
					// showTime = false;
					// timedOut = true;
					// selected = false;
					// GamePanel.this.repaint(0);
					break;
				// Requesting information
				case 7:
					System.out.println("Getting here");
					angle++;
					if (shoot)
						pw.println(pos.getX() + " " + pos.getY() + " " + angle
								+ " 1 " + upgrade);
					else
						pw.println(pos.getX() + " " + pos.getY() + " " + angle
								+ " 0 " + upgrade);
					pw.flush();
					shoot = false;
					break;
				}
			}
		}
	}
}
