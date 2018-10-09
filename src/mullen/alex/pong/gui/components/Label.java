package mullen.alex.pong.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

import mullen.alex.pong.gui.FontUtilities;

/**
 * Represents an option label.
 *
 * @author  Alex Mullen
 *
 */
public class Label {
    /** Holds the default font to use. */
    private static final Font DEFAULT_FONT = new Font("Dialog", Font.BOLD, 50);
    /** Holds the rectangle bounds of the label. */
    private final Rectangle bounds;
    /** Holds the bounds of the text within the label. */
    private final Rectangle textBounds;
    /** Holds the label text. */
    private String labelText;
    /** Holds the horizontal alignment of the label text. */
    private HorizontalAlignment horizAlignment;
    /** Holds the vertical alignment of the label text. */
    private VerticalAlignment vertAlignment;
    /** Holds the font of the text. */
    private Font font;
    /** Holds whether this label is opaque. */
    private boolean opaque;
    /** Holds the background colour. */
    private Color backgroundColour;
    /** Holds the text colour. */
    private Color textColour;
    /**
     * An enum for representing the various horizontal text alignment
     * positions.
     *
     * @author  Alex Mullen
     *
     */
    public enum HorizontalAlignment {
        /** Align text to the left. */
        LEFT,
        /** Align text in the centre. */
        CENTRE,
        /** Align text to the right. */
        RIGHT
    }
    /**
     * An enum for representing the various vertical text alignment
     * positions.
     *
     * @author  Alex Mullen
     *
     */
    public enum VerticalAlignment {
        /** Align text to the top of the label. */
        TOP,
        /** Align text in the middle of the label. */
        MIDDLE,
        /** Align text at the bottom of the label. */
        BOTTOM
    }
    /**
     * Creates a new instance with the specified attributes.
     *
     * @param text       the text on the label
     */
    public Label(final String text) {
        labelText = Objects.requireNonNull(text);
        bounds = new Rectangle();
        textBounds = new Rectangle();
        horizAlignment = HorizontalAlignment.CENTRE;
        vertAlignment = VerticalAlignment.MIDDLE;
        font = DEFAULT_FONT;
        textColour = Color.BLACK;
        backgroundColour = Color.WHITE;
        opaque = false;
    }
    /**
     * Gets the label's text.
     *
     * @return  the text
     */
    public final String getText() {
        return labelText;
    }
    /**
     * Sets the label's text.
     *
     * @param text  the new text
     */
    public final void setText(final String text) {
        labelText = Objects.requireNonNull(text);
    }
    /**
     * Sets whether this label is opaque.
     * <p>
     * Opaque labels do not have their background drawn so that the the text
     * is just rendered over the existing background to give the effect of a
     * transparent background.
     *
     * @param value  whether to make it opaque or not
     */
    public final void setOpaque(final boolean value) {
        opaque = value;
    }
    /**
     * Sets the colour of the text.
     *
     * @param colour  the colour
     */
    public final void setTextColour(final Color colour) {
        textColour = Objects.requireNonNull(colour);
    }
    /**
     * Sets the labels background colour.
     *
     * @param colour  the colour
     */
    public final void setBackgroundColour(final Color colour) {
        backgroundColour = Objects.requireNonNull(colour);
    }
    /**
     * Sets the font for this label.
     *
     * @param newFont  the new font
     */
    public final void setFont(final Font newFont) {
        font = Objects.requireNonNull(newFont);
    }
    /**
     * Sets this label's horizontal text alignment.
     *
     * @param alignment  the horizontal alignment position
     */
    public final void setHorizontalAlignment(
            final HorizontalAlignment alignment) {
        horizAlignment = Objects.requireNonNull(alignment);
    }
    /**
     * Sets this label's vertical text alignment.
     *
     * @param alignment  the vertical alignment position
     */
    public final void setVerticalAlignment(final VerticalAlignment alignment) {
        vertAlignment = Objects.requireNonNull(alignment);
    }
    /**
     * Gets the label's bounds.
     *
     * @return  the bounds
     */
    public final Rectangle getBounds() {
        return bounds;
    }
    /**
     * Gets the bounds of the text within the label.
     *
     * @return  the bounds of the text
     */
    public final Rectangle getTextBounds() {
        return textBounds;
    }
    /**
     * Renders the label to the specified graphics context.
     *
     * @param g  the graphics context
     */
    public final void render(final Graphics2D g) {
        // Scale the height of the font to fit the label.
        Font optionTextFont = FontUtilities.scaleFontWidth(
                labelText, bounds, g, font);
        optionTextFont = FontUtilities.scaleFontHeight(
                bounds, g, optionTextFont);
//        Font optionTextFont = FontUtilities.scaleFontHeight(
//                bounds, g, font);
//        optionTextFont = FontUtilities.scaleFontWidth(
//                labelText, bounds, g, optionTextFont);
        g.setFont(optionTextFont);
        final FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(labelText);
        int textPositionX;
        int textPositionY;
        // Work out the vertical alignment position of the text baseline.
        switch (vertAlignment) {
            case TOP:
                textPositionY = bounds.y + fm.getAscent();
                textBounds.y = textPositionY - fm.getAscent();
                break;
            case MIDDLE:
                textPositionY = bounds.y + fm.getAscent()
                        + (bounds.height - fm.getHeight()) / 2;
                textBounds.y = textPositionY - fm.getAscent()
                        - (bounds.height - fm.getHeight()) / 2;
                break;
            case BOTTOM:
                textPositionY = bounds.y + bounds.height - fm.getDescent();
                textBounds.y = textPositionY - bounds.height + fm.getDescent();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        // Work out the horizontal alignment position of the text.
        switch (horizAlignment) {
            case LEFT:
                textPositionX = bounds.x;
                break;
            case CENTRE:
                textPositionX = bounds.x + (bounds.width - textWidth) / 2;
                break;
            case RIGHT:
                textPositionX = bounds.x
                        + bounds.width - textWidth;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        textBounds.x = textPositionX;
        textBounds.width = textWidth;
        textBounds.height = fm.getHeight();
//        g.setColor(Color.WHITE);
//        g.draw(textBounds);
//        // Draw the debug border.
//        g.setColor(Color.WHITE);
//        g.draw(bounds);
        // Fill in the background if opaque.
        if (opaque) {
            g.setColor(backgroundColour);
            g.fill(bounds);
        }
        // Draw the text.
        g.setColor(textColour);
        g.drawString(labelText, textPositionX, textPositionY);
    }
}
