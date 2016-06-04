import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class KButton extends Component implements MouseListener {
	private ActionListener actionListener;
	private boolean within, pressed;
	private String message;

	KButton(String msg, int width, int height) {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		message = msg;
	}

	/**
	 * Adds the specified action listener to receive action events from this
	 * button.
	 *
	 * @param listener
	 *            the action listener
	 */
	public void addActionListener(ActionListener listener) {
		actionListener = AWTEventMulticaster.add(actionListener, listener);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	/**
	 * paints the RoundedButton
	 */
	@Override
	public void paint(Graphics g) {

		// paint the interior of the button
		if (within && pressed) {
			g.setColor(getBackground().darker().darker());
		} else {
			g.setColor(getBackground());
		}
		g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

		// draw the perimeter of the button
		g.setColor(getBackground().darker().darker().darker());
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

		// draw the label centered in the button
		Font f = getFont();
		if (f != null) {
			FontMetrics fm = getFontMetrics(getFont());
			g.setColor(getForeground());
			g.drawString(message, getWidth() / 2 - fm.stringWidth(message) / 2,
					getHeight() / 2 + fm.getMaxDescent());
		}
	}

	/**
	 * Paints the button and distribute an action event to all listeners.
	 */
	@Override
	public void processMouseEvent(MouseEvent e) {
		Graphics g;
		switch (e.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			pressed = true;
			repaint();
			break;
		case MouseEvent.MOUSE_RELEASED:
			// Reset
			if (pressed && within) {
				if (actionListener != null) {
					actionListener.actionPerformed(new ActionEvent(this,
							ActionEvent.ACTION_PERFORMED, message));
				}
			}
			pressed = false;
			repaint();
			break;
		case MouseEvent.MOUSE_ENTERED:
			within = true;
			repaint();
			break;
		case MouseEvent.MOUSE_EXITED:
			within = false;
			repaint();
			break;
		}
		super.processMouseEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
