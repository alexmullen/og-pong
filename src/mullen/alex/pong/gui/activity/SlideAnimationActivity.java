package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Objects;

import mullen.alex.jge.TickSchedulerService.Task;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;

/**
 * An animation activity that slides an activity out of view whilst sliding a
 * new one into view.
 * <p>
 * Once the animation finishes, the activity brought into view is automatically
 * set as the main activity in the engine.
 *
 * @author  Alex Mullen
 *
 */
public class SlideAnimationActivity implements PongActivity {
    /** Holds how long the animation lasts/takes in ticks. */
    private static final int ANIMATION_DURATION_TICKS = 20;
    /** The activity that is disappearing. */
    private final PongActivity dispearingActivity;
    /** The activity that is appearing. */
    private final PongActivity appearingActivity;
    /** Holds the direction of the slide. */
    private final SlideDirection slideDir;
    /** The normalised X position of the line that separates activities. */
    private double dividerLineX;
    /** The normalised Y position of the line that separates activities. */
    private double dividerLineY;
    /** Holds the sliding animation task. */
    private Task slideTask;
    /**
     * An enumeration that represents the possible directions to perform the
     * slide animation.
     *
     * @author  Alex Mullen
     *
     */
    public enum SlideDirection {
        /** Slide left. */
        LEFT,
        /** Slide right. */
        RIGHT,
        /** Slide up. */
        UP,
        /** Slide down. */
        DOWN,
    }
    /**
     * Creates an new instance that animates between the two specified
     * activities.
     *
     * @param slideDirection    the direction to slide in
     * @param leavingActivity   the activity that is disappearing from view
     * @param enteringActivity  the activity that is appearing
     */
    public SlideAnimationActivity(final SlideDirection slideDirection,
            final PongActivity leavingActivity,
            final PongActivity enteringActivity) {
        slideDir = Objects.requireNonNull(slideDirection);
        dispearingActivity = Objects.requireNonNull(leavingActivity);
        appearingActivity = Objects.requireNonNull(enteringActivity);
    }
    @Override
    public final void onActivityStarted(final Context context) {
        final PongEngine engine = context.getEngine();
        slideTask = engine.getSchedulerService().scheduleDuration(() -> {
            dividerLineX += 1.0f / ANIMATION_DURATION_TICKS;
            dividerLineY += 1.0f / ANIMATION_DURATION_TICKS;
        }, () -> {
            engine.getActivityService().startActivity(appearingActivity);
        }, 0, 1, ANIMATION_DURATION_TICKS);
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        slideTask.cancel();
    }
    @Override
    public final void update() {
        // Intentionally empty.
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        final double absAmountMovedX =
                (dividerLineX * size.getWidth()) + (dividerLineX * delta);
        final double absAmountMovedY =
                (dividerLineY * size.getHeight()) + (dividerLineY * delta);
        /*
         * Create a copy of the graphics context so we can perform translations
         * whilst keeping the original translation for the appearing activity.
         */
        final Graphics2D disappearingGraphics = (Graphics2D) g.create();
        switch (slideDir) {
            case LEFT:
                disappearingGraphics.translate(0.0 - absAmountMovedX, 0.0);
                break;
            case RIGHT:
                disappearingGraphics.translate(absAmountMovedX, 0.0);
                break;
            case UP:
                disappearingGraphics.translate(0.0, 0.0 - absAmountMovedY);
                break;
            case DOWN:
                disappearingGraphics.translate(0.0, absAmountMovedY);
                break;
            default:
                break;
        }
        dispearingActivity.render(disappearingGraphics, size, delta);
        disappearingGraphics.dispose();
        /*
         * Translate the original graphics context to place where the appearing
         * activity will be in the animation.
         */
        switch (slideDir) {
            case LEFT:
                g.translate(size.getWidth() - absAmountMovedX, 0.0);
                break;
            case RIGHT:
                g.translate(0.0 - size.getWidth() + absAmountMovedX, 0.0);
                break;
            case UP:
                g.translate(0.0, size.getHeight() - absAmountMovedY);
                break;
            case DOWN:
                g.translate(0.0, 0.0 - size.getHeight() + absAmountMovedY);
                break;
            default:
                break;
        }
        appearingActivity.render(g, size, delta);
    }
}
