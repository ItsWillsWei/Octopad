import java.awt.*;
import java.awt.event.*;

public class KInputPanel extends Component implements MouseListener,
		KeyListener {
	private ActionListener actionListener;
	private String title, input;
	private boolean selected;

	KInputPanel(String msg) {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		title = msg;
		input = "";
		selected = false;
	}
	
	public String getInput(){
		return input;
	}

	public void setSelected(boolean state) {
		selected = state;
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
	}

	public void paint(Graphics g) {
		if (selected) {
			g.setColor(getBackground());
		} else {
			g.setColor(getBackground().darker());
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
			repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("hi");
		if (selected) {
			int typed = e.getKeyCode();
			// Numbers 48-57 - 65 - 90
			if (typed >= 48 && typed <= 57)
				input += (typed - 48);
			else if (typed >= 65 && typed <= 90)
				input += 'A' + typed - 65;
		}
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

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
}
