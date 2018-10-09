package mullen.alex.pong.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import mullen.alex.pong.gui.components.Label.HorizontalAlignment;
import mullen.alex.pong.gui.components.Label.VerticalAlignment;

/**
 * Represents a button.
 *
 * @author  Alex Mullen
 *
 */
public class Button {
    /** Holds the width in pixels of a button's border width. */
    private static final float BORDER_WIDTH = 3.0f;
    /** Holds the label of the button. */
    private final Label label;
    /**
     * Creates a new button with the specified text.
     *
     * @param textLabel  the text label on the button
     */
    public Button(final String textLabel) {
        label = new Label(textLabel);
        label.setHorizontalAlignment(HorizontalAlignment.CENTRE);
        label.setVerticalAlignment(VerticalAlignment.MIDDLE);
        label.setBackgroundColour(Color.GRAY);
        label.setOpaque(true);
    }
    /**
     * Gets the button's bounds.
     *
     * @return  the bounds
     */
    public final Rectangle getBounds() {
        return label.getBounds();
    }
    /**
     * Gets the label that acts as the backing for this button.
     *
     * @return  the label
     */
    public final Label getLabel() {
        return label;
    }
    /**
     * Renders the button to the specified graphics context.
     *
     * @param g  the graphics context
     */
    public final void render(final Graphics2D g) {
        label.render(g);
        g.setStroke(new BasicStroke(BORDER_WIDTH));
        g.setColor(Color.LIGHT_GRAY);
        g.draw(getBounds());
    }
}
