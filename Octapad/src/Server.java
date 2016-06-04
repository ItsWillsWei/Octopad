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
			players = new ArrayList<Player>();
			threads = new ArrayList<Thread>();
			while (true) {

				// Be nice to the JVM
				Thread.sleep(10);

				System.out.println("Waiting for connection");
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
			System.out.println("nice");
			while (p.alive()) {

				try {
					Thread.sleep(100);
					p.sendCommand("hey");
					p.getPos();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Checks the objects around the player and calls the update
				// surroundings method

				// Checks players around the player

				// Checks to see if anything will damage the player and update
				// health

				// Check to see if the player needs an upgrade
			}
		}
	}

	public void go() {

	}

	/*
	 * Alternatively could just shout it right away
	 */
	public ArrayList<Player> surroundingPlayers(Position pos) {
		ArrayList<Player> p = new ArrayList<Player>();

		// Some constant for the screen size
		for (Player currentPlayer : players) {
			if (Math.abs(currentPlayer.getPos().getX() - pos.getX()) <= 50
					&& Math.abs(currentPlayer.getPos().getY() - pos.getY()) <= 50)
				p.add(currentPlayer);
		}

		return p;

	}

}
