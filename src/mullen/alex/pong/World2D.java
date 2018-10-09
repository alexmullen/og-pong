package mullen.alex.pong;

import java.awt.Point;

/**
 * Represents a virtual 2D world coordinate space that is independent of
 * the viewport resolution.
 * <p>
 * Coordinates start in the top-left corner which is always <code>(0,0)</code>
 * and extends to the bottom right which ends in
 * <code>(width-1,height-1)</code>.
 * <p>
 * Coordinates are always positive integers so make sure a world of sufficient
 * size is specified for the precision required.
 *
 * @author  Alex Mullen
 *
 */
public class World2D {
    /** Holds the minimum width a world can be. */
    public static final int MINIMUM_WORLD_WIDTH = 100;
    /** Holds the minimum height a world can be. */
    public static final int MINIMUM_WORLD_HEIGHT = 100;
    /** Holds the width of the world. */
    private final int width;
    /** Holds the height of the world. */
    private final int height;
    /**
     * Creates a new 2D world representation of the specified dimensions.
     *
     * @param w  the width
     * @param h  the height
     *
     * @throws IllegalArgumentException  if <code>w</code> is less than
     *                                   {@link #MINIMUM_WORLD_WIDTH} or
     *                                    <code>h</code> is less than
     *                                    {@link #MINIMUM_WORLD_HEIGHT}
     */
    public World2D(final int w, final int h) {
        width = validateWidth(w);
        height = validateHeight(h);
    }
    /**
     * Validates a width specified for the constructor.
     *
     * @param w  the specified width
     * @return   <code>w</code> if it is valid otherwise an exception is thrown
     *
     * @throws IllegalArgumentException  if <code>w</code> is less than
     *                                   {@link #MINIMUM_WORLD_WIDTH}
     */
    private static int validateWidth(final int w) {
        if (w < MINIMUM_WORLD_WIDTH) {
            throw new IllegalArgumentException(
                    "Width of world is less than minimum allowed.");
        }
        return w;
    }
    /**
     * Validates a height specified for the constructor.
     *
     * @param h  the specified height
     * @return   <code>h</code> if it is valid otherwise an exception is thrown
     *
     * @throws IllegalArgumentException  if <code>h</code> is less than
     *                                   {@link #MINIMUM_WORLD_HEIGHT}
     */
    private static int validateHeight(final int h) {
        if (h < MINIMUM_WORLD_HEIGHT) {
            throw new IllegalArgumentException(
                    "Height of world is less than minimum allowed.");
        }
        return h;
    }
    /**
     * Gets the width of this world.
     *
     * @return  the width
     */
    public final int getWidth() {
        return width;
    }
    /**
     * Gets the height of this world.
     *
     * @return  the height
     */
    public final int getHeight() {
        return height;
    }
    /**
     * Gets the top-left most coordinates of the world.
     * <p>
     * These are always <code>(0,0)</code>.
     *
     * @return  the coordinates
     */
    @SuppressWarnings("static-method") // !!!
    public final Point getTopLeft() {
        return new Point(0, 0);
    }
    /**
     * Gets the top-right most coordinates of the world.
     *
     * @return  the coordinates
     */
    public final Point getTopRight() {
        return new Point(width - 1, 0);
    }
    /**
     * Gets the bottom-left most coordinates of the world.
     *
     * @return  the coordinates
     */
    public final Point getBottomLeft() {
        return new Point(0, height - 1);
    }
    /**
     * Gets the bottom-right most coordinates of the world.
     *
     * @return  the coordinates
     */
    public final Point getBottomRight() {
        return new Point(width - 1, height - 1);
    }
    /**
     * Gets the centre coordinates of the world.
     * <p>
     * If the world is not of even width or height, the centre is the resulting
     * value when the dimension is divided using integer division.
     *
     * @return  the centre coordinates
     */
    public final Point getCentre() {
        return new Point(width / 2, height / 2);
    }
}
