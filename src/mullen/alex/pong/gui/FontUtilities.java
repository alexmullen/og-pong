package mullen.alex.pong.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Static utility methods relating to fonts.
 *
 * @author  Alex Mullen
 *
 */
public final class FontUtilities {
    /**
     * Private constructor to prevent this being instantiated.
     */
    private FontUtilities() {
        // Constructor is intentionally empty.
    }
    /**
     * Scales the font width size to best fit the specified text horizontally
     * within the specified rectangle bounds.
     *
     * @param text   the text string
     * @param rect   the rectangle bounds
     * @param g      the graphics context
     * @param pFont  the initial font to derive from
     * @return       a font that is of best vertical fit
     */
    public static Font scaleFontWidth(final String text, final Rectangle rect,
            final Graphics2D g, final Font pFont) {
        Font font = pFont.deriveFont(100.0f);
        int width = g.getFontMetrics(font).stringWidth(text);
        while (width >= rect.width) {
            font = font.deriveFont(font.getSize() - 1.0f);
            width = g.getFontMetrics(font).stringWidth(text);
        }
        return font;

//        float fontSize = 10.0f;
//        Font font = pFont;
//        font = font.deriveFont(fontSize);
//        int width = g.getFontMetrics(font).stringWidth(text);
//        fontSize = (rect.width / (float) width ) * fontSize;
//        return font.deriveFont(fontSize);
    }
    /**
     * Scales the font height size to best fit the specified text vertically
     * within the specified rectangle bounds.
     *
     * @param rect   the rectangle bounds
     * @param g      the graphics context
     * @param pFont  the initial font to derive from
     * @return       a font that is of best vertical fit
     */
    public static Font scaleFontHeight(final Rectangle rect, final Graphics2D g,
            final Font pFont) {
        
        Font font = pFont.deriveFont(100);
        int height = g.getFontMetrics(font).getHeight();
        while (height >= rect.height && font.getSize() > 0) {
            font = font.deriveFont(font.getSize() - 1.0f);
            height = g.getFontMetrics(font).getHeight();
        }
        return font;
        
        
//        float fontSize = 10.0f;
//        Font font = pFont;
//
//        font = font.deriveFont(fontSize);
//        int height = g.getFontMetrics(font).getHeight();
//        fontSize = (rect.height / (float) height ) * fontSize; // DBZ possible!
//        return font.deriveFont(fontSize);
    }
}
