package mullen.alex.pong.gui.components;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Objects;

/**
 * Represents a generic way for detecting whether a spatial region was clicked
 * on.
 *
 * @author  Alex Mullen
 *
 */
public class ClickableRegion {
    /** The handler to run when a click is detected. */
    private final Runnable handler;
    /** Holds the current region that is click-able. */
    private final Rectangle region;
    /** Holds the last copy of region so that we can detect resizing. */
    private Rectangle lastRegion;
    private boolean enteredRegionWithoutPress;
    private boolean pressedWithinRegion;
    /**
     * Creates new instance using the specified handler and region.
     *
     * @param h  the handler to invoke when a click is detected
     * @param r  the region
     */
    public ClickableRegion(final Runnable h, final Rectangle r) {
        handler = Objects.requireNonNull(h);
        region = Objects.requireNonNull(r);
        lastRegion = new Rectangle(r);
    }
    public void update(final Point cursorPosition,
            final boolean buttonPressed) {
        // Invalidate if the region has changed.
        if (!region.equals(lastRegion)) {
            lastRegion = new Rectangle(region);
            enteredRegionWithoutPress = false;
            pressedWithinRegion = false;
        }
        if (region.contains(cursorPosition)) {
            if (buttonPressed) {
                if (enteredRegionWithoutPress) {
                    pressedWithinRegion = true;
                }
            } else {
                if (pressedWithinRegion) {
                    // Released.
                    enteredRegionWithoutPress = true;
                    pressedWithinRegion = false;
                    handler.run();
                } else {
                    enteredRegionWithoutPress = true;
                }
            }
        } else {
            enteredRegionWithoutPress = false;
        }
    }
}
