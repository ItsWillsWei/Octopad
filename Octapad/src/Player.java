import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Player {

	private Socket sock;
	private InputStream in;
	private BufferedReader br;
	private OutputStream out;
	private Position pos;
	private Color color;
	private PrintWriter pw;
	private boolean alive =true;

	public Player(Socket s, Position p) {

		try {
			sock = s;
			pos = p;
			color = new Color((int) (Math.random() * 256),
					(int) (Math.random() * 256), (int) (Math.random() * 256));
			in = sock.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			out = sock.getOutputStream();
			pw = new PrintWriter(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Position getPos() {
		return pos;
	}

	public Color getColour() {
		return color;
	}

	public boolean alive() {
		return alive;
	}
	
	public void sendCommand(String command)
	{
		System.out.println("send");
		pw.println(command);
		pw.flush();
	}
	
	public void updateSurroundings(Object [] n){
		for(Object o:n)
		{
			// Send it to the player via the printwriter
		}
		
	}

}
