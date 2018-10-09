package mullen.alex.pong.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.components.Label.HorizontalAlignment;

/**
 * Represents a list of label options to choose from that can animate when
 * hovered over and perform specified actions when click on.
 *
 * @author  Alex Mullen
 *
 */
public class LabelMenu {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(LabelMenu.class.getName());
    /** Ratio of the the width of the labels to the width of the surface. */
    private static final float LABEL_WIDTH_TO_SURFACE_WIDTH_RATIO = 0.75f;
    /** Ratio of the the height of the labels to the height of the surface. */
    private static final float LABEL_HEIGHT_TO_SURFACE_HEIGHT_RATIO = 0.10f;
    /** Holds the ideal resolution to render and scale for. */
    private final World2D surface;
    /** Holds a reference to the engine. */
    private final PongEngine engine;
    /** Holds the labels for the menu. */
    private final List<HoveredLabel> menuLabels;
    /** Holds the width of each label. */
    private final int labelWidth;
    /** Holds the height of each label. */
    private final int labelHeight;
    /** Holds the last transform used for rendering. */
    private AffineTransform lastRenderTransform;
    /**
     * Creates a new instance that uses the specified engine for input and
     * virtual surface for scaling.
     *
     * @param eng    the engine
     * @param world  the surface
     */
    public LabelMenu(final PongEngine eng, final World2D world) {
        surface = Objects.requireNonNull(world);
        engine = Objects.requireNonNull(eng);
        menuLabels = new ArrayList<>();
        labelWidth = (int)
                (surface.getWidth() * LABEL_WIDTH_TO_SURFACE_WIDTH_RATIO);
        labelHeight = (int)
                (surface.getHeight() * LABEL_HEIGHT_TO_SURFACE_HEIGHT_RATIO);
    }
    /**
     * Adds a new label to the end of any current labels.
     *
     * @param text     the text to display on the label
     * @param handler  the handler to run when the label is clicked
     */
    public final void addLabel(final String text, final Runnable handler) {
        final HoveredLabel label = new HoveredLabel(text, handler);
        label.getBounds().width = labelWidth;
        label.getBounds().height = labelHeight;
        label.setHorizontalAlignment(HorizontalAlignment.LEFT);
        label.setOpaque(false);
        label.setTextColour(Color.WHITE);
        menuLabels.add(label);
    }
    /**
     * Performs hit and click testing on the labels.
     */
    public final void update() {
// !!! This will be null if this is invoked before render().
if (lastRenderTransform == null) {
    return;
}
        try {
            final Point position = engine.getMouseService().getPosition();
            /*
             * Translate the screen coordinates with the inverse translation
             * we applied to our virtual coordinates so that we can get what
             * they actually clicked on.
             */
            final AffineTransform invertedTransform =
                    lastRenderTransform.createInverse();
            invertedTransform.transform(position, position);
            for (final HoveredLabel label : menuLabels) {
                label.checkForHit(position,
                        engine.getMouseService().isButtonPressed(
                                MouseEvent.BUTTON1));
                if (label.getTextBounds().contains(position)) {
                    label.setHovered(true);
                } else {
                    label.setHovered(false);
                }
            }
        } catch (final NoninvertibleTransformException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    /**
     * Renders the menu on the specified graphics context.
     *
     * @param g      the graphics context
     * @param size   the size of the graphics context
     */
    public final void render(final Graphics2D g, final Dimension size) {
        centreAndScaleSurfaceOnViewport(g, size);
        /*
         * Save the last transform so that the update method knows where
         * things were actually placed on the viewport so that it can detect
         * whether labels where clicked or hovered over.
         */
        lastRenderTransform = g.getTransform();
        // Work out the start position offsets of the menu.
        final int totalMenuHeight = menuLabels.size() * labelHeight;
        final int menuBeginOffsetX = (surface.getWidth() - labelWidth) / 2;
        final int menuBeginOffsetY =
                (surface.getHeight() - totalMenuHeight) / 2;
        g.setColor(Color.WHITE);
        // Render each label.
        for (int i = 0; i < menuLabels.size(); i++) {
            final HoveredLabel currentLabel = menuLabels.get(i);
            if (currentLabel.isHovered()) {
                // Create a copy of the label to render smaller in place.
                final HoveredLabel labelCopy =
                        new HoveredLabel(currentLabel.getText(), null);
                labelCopy.setHorizontalAlignment(HorizontalAlignment.LEFT);
                labelCopy.setOpaque(false);
                labelCopy.setTextColour(Color.WHITE);
                labelCopy.getBounds().x =
                        (int) (menuBeginOffsetX + labelHeight * 0.05);
                labelCopy.getBounds().y = menuBeginOffsetY + (labelHeight * i);
                labelCopy.getBounds().height = (int) (labelHeight * 0.95);
                labelCopy.getBounds().width = currentLabel.getBounds().width;
                labelCopy.render(g);
            } else {
                currentLabel.getBounds().x = menuBeginOffsetX;
                currentLabel.getBounds().y =
                        menuBeginOffsetY + (labelHeight * i);
                currentLabel.getBounds().height = labelHeight;
                currentLabel.render(g);
            }
        }
    }
    /**
     * Centres and scales the virtual surface we use to the available
     * viewport space we have.
     *
     * @param g     the graphics context to perform the transformations on
     * @param size  the size of the graphics context
     */
    private void centreAndScaleSurfaceOnViewport(final Graphics2D g,
            final Dimension size) {
        final World2D world = surface;
        /*
         * Calculate the scale multipliers required to fit the world onto
         * the viewport space we have.
         */
        final double widthScale = size.getWidth() / world.getWidth();
        final double heightScale = size.getHeight() / world.getHeight();
        // Choose the minimum of the scale ratios.
        final double minScale = Math.min(widthScale, heightScale);
        // Calculate the scaled dimensions in view space.
        final int scaledViewWidth = (int) (world.getWidth() * minScale);
        final int scaledViewHeight = (int) (world.getHeight() * minScale);
        /*
         * Calculate how much space will remain on each axis after scaling
         * so that we can centre the frame by translating by half the
         * remaining space on each axis.
         */
        final int widthInset = (size.width - scaledViewWidth) / 2;
        final int heightInset = (size.height - scaledViewHeight) / 2;
        g.translate(widthInset, heightInset);
        g.scale(minScale, minScale);
    }
    /**
     * An extension to {@link Label} that represents a label that dynamically
     * changes its size when hovered over with the system mouse cursor.
     *
     * @author  Alex Mullen
     *
     */
    private static class HoveredLabel extends Label {
        /** Holds the code to run when the label is clicked. */
        private final Runnable clickHandler;
        /** Holds whether the cursor is currently hovering over the label. */
        private boolean hovered;
        /** Holds whether the mouse is currently pressed down on the label. */
        private boolean pressed;
        /**
         * Creates a new instance that has the specified text and will run the
         * specified handler when clicked on.
         *
         * @param text     the label text
         * @param handler  the handler to run when clicked
         */
        HoveredLabel(final String text, final Runnable handler) {
            super(text);
            clickHandler = handler;
        }
        /**
         * Checks if this label has been clicked on or hovered over by the mouse
         * cursor.
         *
         * @param cursorPosition  the position of the cursor
         * @param buttonPressed   whether the mouse button was pressed
         */
        public final void checkForHit(final Point cursorPosition,
                final boolean buttonPressed) {
            if (getTextBounds().contains(cursorPosition)) {
                if (buttonPressed) {
                    pressed = true;
                } else {
                    // Check for release.
                    if (pressed) {
                        pressed = false;
                        clickHandler.run();
                    }
                }
            } else {
                pressed = false;
            }
        }
        /**
         * Gets whether the label is currently in the "hovered" state.
         *
         * @return  <code>true</code> or <code>false</code>
         */
        boolean isHovered() {
            return hovered;
        }
        /**
         * Sets the "hovered" state of the label.
         *
         * @param value  <code>true</code> or <code>false</code>
         */
        void setHovered(final boolean value) {
            hovered = value;
        }
    }
}
