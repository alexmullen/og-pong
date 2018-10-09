package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Objects;

import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.net.server.PongServer;

/**
 * A wrapper activity for a {@link ConnectedGameActivity} whose sole
 * responsibility is to manage the local reference to the {@link PongServer}
 * instance whilst delegating all callback's to the wrapped activity.
 * <p>
 * This allows the call to
 * {@link ConnectedGameActivity#onActivityStopped(StoppedReason)}
 * to be intercepted which gives us the signal to shut down the server. Without
 * this, we would be unable to reuse the {@link ConnectedGameActivity} class for
 * both client and server perspectives.
 *
 * @author  Alex Mullen
 *
 */
public class LocallyConnectedGameActivity implements PongActivity {
    /** Holds the server object. */
    private final PongServer server;
    /** Holds the wrapped activity we delegate callback's to. */
    private final ConnectedGameActivity wrappedActivity;
    /**
     * Creates a new instance from the given server and activity.
     *
     * @param ps        the server object that this will manage
     * @param activity  the activity to delegate to
     */
    public LocallyConnectedGameActivity(final PongServer ps,
            final ConnectedGameActivity activity) {
        server = Objects.requireNonNull(ps);
        wrappedActivity = Objects.requireNonNull(activity);
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        wrappedActivity.render(g, size, delta);
    }
    @Override
    public final void update() {
        wrappedActivity.update();
    }
    @Override
    public final void onActivityStarted(final Context context) {
        wrappedActivity.onActivityStarted(context);
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        wrappedActivity.onActivityStopped(reason);
        server.shutdown();
    }
}
