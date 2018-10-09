package mullen.alex.pong.gui.activity;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Objects;

import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;

public class FadeAnimationActivity implements PongActivity {
    /** Holds how long the animation lasts/takes in ticks. */
    private static final int ANIMATION_DURATION_TICKS = 15;
    /** The engine. */
    private final PongEngine engine;
    /** The activity that is disappearing. */
    private final PongActivity dispearingActivity;
    /** The activity that is appearing. */
    private final PongActivity appearingActivity;
    private float fadeInAlpha;
    private float fadeOutAlpha;
    public FadeAnimationActivity(final PongEngine eng,
            final PongActivity leavingActivity, final PongActivity enteringActivity) {
        fadeInAlpha = 0.0f;
        fadeOutAlpha = 1.0f;
        engine = Objects.requireNonNull(eng);
        dispearingActivity = Objects.requireNonNull(leavingActivity);
        appearingActivity = Objects.requireNonNull(enteringActivity);
    }
    @Override
    public final void onActivityStarted(final Context context) {
        engine.getSchedulerService().scheduleDuration(() -> {
            fadeInAlpha += 1.0f / ANIMATION_DURATION_TICKS;
            fadeOutAlpha -= 1.0f / ANIMATION_DURATION_TICKS;
        }, () -> {
            engine.getActivityService().startActivity(appearingActivity);
        }, 0, 1, ANIMATION_DURATION_TICKS);
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        // Empty.
    }
    @Override
    public void update() {
        // Intentionally empty.
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
//        g.clearRect(0, 0, size.width, size.height);
//        final AlphaComposite fadeOutAlphaComp =
//                AlphaComposite.getInstance(
//                        AlphaComposite.DST_ATOP, fadeOutAlpha);
//        g.setComposite(fadeOutAlphaComp);
        dispearingActivity.render(g, size, delta);
        final AlphaComposite fadeinAlphaComp =
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_ATOP, fadeInAlpha);
        g.setComposite(fadeinAlphaComp);
        appearingActivity.render(g, size, delta);
    }
}
