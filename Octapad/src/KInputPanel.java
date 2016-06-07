import java.awt.*;
import java.awt.event.*;

public class KInputPanel extends Component  {
	private ActionListener actionListener;
	private String title, input;
	private boolean selected;

	KInputPanel(String msg) {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		setFocusable(true);
		title = msg;
		input = "";
		selected = false;
	}
	
	public String getInput(){
		return input;
	}

	public void setSelected(boolean state) {
		selected = state;
		setFocusable(state);
		repaint(0);
	}

	/**
	 * Adds the specified action listener to receive action events from this
	 * InputPanel.
	 *
	 * @param listener
	 *            the action listener
	 */
	public void addActionListener(ActionListener listener) {
		actionListener = AWTEventMulticaster.add(actionListener, listener);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
	}

	public void paint(Graphics g) {
		if (selected) {
			g.setColor(Color.RED);//getBackground());
		} else {
			g.setColor(Color.BLUE);//getBackground().darker());
		}

		// Draws outside box
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

		// Draws input prompt
		g.drawString(title, (int) (0.1 * getWidth()) + 5, 50);

		// Draws the center text field and text
		g.drawRect((int) (0.1 * getWidth()), (int) (0.4 * getHeight()),
				(int) (0.8 * getWidth()), (int) (0.2 * getHeight()));

		String toDisplay = new String(input);
		if (selected)
			toDisplay += "|";
		g.drawString(toDisplay, (int) (0.1 * getWidth()),
				(int) (0.4 * getHeight()) + 15);
	}

	@Override
	public void processMouseEvent(MouseEvent e) {
		switch (e.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			selected = true;
			actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, title));
			setFocusable(true);
			requestFocusInWindow();
			repaint();
		}
	}
	@Override
	public void processKeyEvent(KeyEvent e){
		
		if (selected && e.getID() == KeyEvent.KEY_RELEASED) {
			
			int typed = e.getKeyCode();
			System.out.println(title+" type: " + typed);
			// Numbers 48-57 - 65 - 90
			if (typed >= 48 && typed <= 57)
				input += (typed - 48);
			else if (typed >= 65 && typed <= 90)
				input += (char)('A' + typed - 65);
			else if (typed == KeyEvent.VK_BACK_SPACE && input.length() > 0)
				input = input.substring(0, input.length()-1);
			else if(typed == KeyEvent.VK_SPACE)
				input += " ";
		}
		repaint(0);
	}
}
