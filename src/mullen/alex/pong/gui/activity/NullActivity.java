package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;

import mullen.alex.pong.engine.PongActivity;

/**
 * An activity that does nothing and exists for convenience so that an
 * anonymous class is not required which takes up slightly more memory due to
 * the implicit reference to the outer class.
 *
 * @author  Alex Mullen
 *
 */
public class NullActivity implements PongActivity {
    /**
     * Create a new instance.
     */
    public NullActivity() {
        // Intentionally empty.
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        // Just clear the screen.
        g.clearRect(0, 0, size.width, size.height);
    }
    @Override
    public void onActivityStarted(final Context context) {
        // Intentionally empty.
    }
    @Override
    public void onActivityStopped(final StoppedReason reason) {
        // Intentionally empty.
    }
    @Override
    public void update() {
        // Intentionally empty.
    }
}
