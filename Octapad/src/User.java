import java.awt.event.*;

import javax.swing.*;


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
		
	}
}
