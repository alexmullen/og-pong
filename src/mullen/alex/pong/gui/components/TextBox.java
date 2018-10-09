package mullen.alex.pong.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mullen.alex.pong.gui.FontUtilities;

/**
 * Represents a custom text box implementation.
 *
 * @author  Alex Mullen
 *
 */
public class TextBox {
    /** Holds the default font to use. */
    private static final Font DEFAULT_FONT = new Font("Dialog", Font.BOLD, 50);
    /** Holds the rectangle bounds of the text box. */
    private final Rectangle bounds;
    /** Holds the text held within the text box. */
    private final StringBuilder text;
    /** For each character rendered, this holds its pixel advance width. */
    private final List<Integer> charTextAdvanceBounds;
    /** Holds whether this currently has focus. */
    private boolean hasFocus;
    /** Holds the font for the text. */
    private Font font;
    /** Holds the caret position in the text. */
    private int caretPosition;
    /** Holds whether the caret is currently visible. */
    private boolean caretVisibile;
    /**
     * Creates a new instance.
     */
    public TextBox() {
        bounds = new Rectangle();
        charTextAdvanceBounds = new ArrayList<>();
        text = new StringBuilder("");
        caretPosition = text.length();
        font = DEFAULT_FONT;
        caretVisibile = true;
    }
    /**
     * Gets the bounds.
     *
     * @return  the rectangular bounds
     */
    public final Rectangle getBounds() {
        return bounds;
    }
    /**
     * Sets the focus state.
     *
     * @param focus  <code>true</code> or <code>false</code>
     */
    public final void setFocus(final boolean focus) {
        hasFocus = focus;
    }
    /**
     * Sets whether the caret is visible.
     *
     * @param value  <code>true</code> or <code>false</code>
     */
    public final void setCaretVisible(final boolean value) {
        caretVisibile = value;
    }
    /**
     * Gets whether the caret is visible.
     *
     * @return  <code>true</code> if visible; <code>false</code> otherwise
     */
    public final boolean getCaretVisible() {
        return caretVisibile;
    }
    /**
     * Gets the current text held within the text box.
     *
     * @return  the text
     */
    public final String getText() {
        return text.toString();
    }
    /**
     * Sets the current text held within the text box.
     *
     * @param value  the text
     */
    public final void setText(final String value) {
        text.setLength(0);
        text.append(value);
    }
    public void handleMouse(final Point position, final boolean buttonPressed) {
        if (buttonPressed && bounds.contains(position)) {
            hasFocus = true;
            final int xPosInTextBoxSpace = position.x - bounds.x - 5;
            // Move caret.
            caretPosition = findCaretIndexOfXPos(xPosInTextBoxSpace);
        } else if (buttonPressed && !bounds.contains(position)) {
            hasFocus = false;
        }
    }
    public void applyKeyType(final KeyEvent event) {
        if (hasFocus) {
            if (event.getKeyChar() == '\b') {
                applyBackSpace();
            } else {
                if (isPrintableChar(event.getKeyChar())) {
                    text.insert(caretPosition, event.getKeyChar());
                    caretPosition++;
                }
            }
        }
    }
    public void handleKeyPress(final KeyEvent event) {
        if (hasFocus) {
            if (event.isControlDown() && event.getKeyCode() == KeyEvent.VK_V) {
                final String clipboardStr = getClipboardString();
                text.insert(caretPosition, clipboardStr);
                caretPosition += clipboardStr.length();
            }
        }
    }
    public static boolean isPrintableChar(char c) {
        final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c))
                && c != KeyEvent.CHAR_UNDEFINED
                && block != null
                && block != Character.UnicodeBlock.SPECIALS;
    }
    /**
     * Applies a backspace.
     */
    public final void applyBackSpace() {
        if (caretPosition > 0) {
            text.deleteCharAt(caretPosition - 1);
            caretPosition--;
        }
    }
    /**
     * Renders the text box to the specified graphics context.
     *
     * @param g  the graphics context
     */
    public final void render(final Graphics2D g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.WHITE);
        g2.fill(bounds);
        g2.setStroke(new BasicStroke(2));
        if (hasFocus) {
            g2.setColor(Color.YELLOW);
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.draw(bounds);
        g2.setColor(Color.BLACK);
        /*
         * Set a clip region so characters that go past the bounds are not
         * drawn.
         */
        g2.clip(bounds);
        g2.setFont(FontUtilities.scaleFontHeight(bounds, g2, font));
        // Calculate margins.
        final int horizontalMargin = bounds.width / 100;
        final int verticalMargin = bounds.height / 10;
        // Render text.
        g2.drawString(text.toString(), bounds.x + horizontalMargin,
                bounds.y + g2.getFontMetrics().getAscent()
                + (g2.getFontMetrics().getDescent() / 2));
        recalculateTextCharAdvanceBounds(g2);
        if (hasFocus && caretVisibile) {
            // Draw caret.
            final int caretPositionX =
                    bounds.x + horizontalMargin + findCaretAdvance(g2);
            g2.drawLine(caretPositionX,
                        bounds.y + verticalMargin,
                        caretPositionX,
                        bounds.y + bounds.height - verticalMargin);
        }
        // Dispose of created graphics context.
        g2.dispose();
    }
    /**
     * Goes through every character in the held text and calculates and saves
     * the advance of each character. The advance is how far away the right most
     * point on a character is from zero. Zero is the left most point on the
     * first character in the text.
     *
     * @param g  the graphics context
     */
    private void recalculateTextCharAdvanceBounds(final Graphics2D g) {
        charTextAdvanceBounds.clear();
        for (int i = 0; i < text.length(); i++) {
            final String tempStr = text.substring(0, i + 1);
            charTextAdvanceBounds.add(
                    Integer.valueOf(g.getFontMetrics().stringWidth(tempStr)));
        }
    }
    /**
     * Given an X position in text space, return the index to place the caret
     * at. This takes into account a position being closer to a certain edge
     * on a character so that a position to near the right edge will place
     * the caret just after it.
     *
     * @param x  the x position in textbox space
     * @return   the index to place the caret at
     */
    private int findCaretIndexOfXPos(final int x) {
        int previousAdvance = 0;
        for (int i = 0; i < charTextAdvanceBounds.size(); i++) {
            final int currentAdvance = charTextAdvanceBounds.get(i).intValue();
            if (currentAdvance > x) {
                /*
                 * Check which half of the character was selected so that the
                 * character so we can better place the caret.
                 */
                if ((currentAdvance - x) < (x - previousAdvance)) {
                    // Closer to the right.
                    return Math.min(charTextAdvanceBounds.size(), i + 1);
                } else {
                    // Closer to the left.
                    return i;
                }
            } else {
                previousAdvance = currentAdvance;
            }
        }
        // Else return the position right of the last character.
        return charTextAdvanceBounds.size();
    }
    /**
     * Gets the advance position of the caret in textbox space pixels with zero
     * being at the beginning of the text.
     *
     * @param g  the graphics context
     * @return   the X position or advance of the cursor in pixels
     */
    private int findCaretAdvance(final Graphics2D g) {
        final String uptoCaretString = text.substring(0, caretPosition);
        return g.getFontMetrics().stringWidth(uptoCaretString);
    }
    /**
     * Gets the currently stored string within the system clipboard if any.
     *
     * @return  the string value or an empty string ("") if there is no string
     *          currently held there
     */
    private static String getClipboardString() {
        String result = "";
        final Clipboard clipboard =
                Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);
        if (contents != null
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                result = (String)
                        contents.getTransferData(DataFlavor.stringFlavor);
            } catch (final UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
