import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 * @author
 * @version Jun 2, 2016
 */
public class Server {
	private static Display display;
	private ServerSocket serverSocket;
	private int noOfPlayers;
	private ArrayList<Player> players;
	private ArrayList<Thread> threads;
	private Piece[][] pieces;

	public static void main(String[] args) {
		display = new Display();
		new Server().go();
	}

	public Server() {
		connectPlayers();
	}

	/**
	 * Connect the players
	 */
	public void connectPlayers() {
		Socket client;
		Thread currentThread;
		Player currentPlayer;

		try {
			serverSocket = new ServerSocket(421);
			while (true) {

				// Be nice to the JVM
				Thread.sleep(10);

				// Connect new players
				client = serverSocket.accept();
				noOfPlayers++;
				System.out.printf("Client #%d connected!%n", noOfPlayers);
				currentPlayer = new Player(client, new Position(
						(int) (Math.random() * 1000),
						(int) (Math.random() * 1000)));
				players.add(currentPlayer);
				currentThread = new Thread(new PlayerThread(currentPlayer));
				threads.add(currentThread);
				currentThread.start();

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Keeps track of each player
	 * 
	 * @author
	 * @version May 31, 2016
	 */
	class PlayerThread implements Runnable {
		Player p;

		public PlayerThread(Player player) {
			p = player;
		}

		public void run() {
			while (!p.alive()) {
				// Checks the objects around the player

				// Checks players around the player

				// Checks to see if anything will damage the player and update
				// health

				// Check to see if the player needs an upgrade
			}
		}
	}

	public void go() {

	}

}
