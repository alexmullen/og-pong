package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;

import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.activity.SlideAnimationActivity.SlideDirection;
import mullen.alex.pong.gui.components.LabelMenu;

/**
 * Represents the activity to display when the "Multiplayer" label is chosen
 * on the main menu activity.
 *
 * @author  Alex Mullen
 *
 */
public class MultiplayerMenuActivity implements PongActivity {
    /** Holds the menu. */
    private final LabelMenu menu;
    /**
     * Creates a new instance that uses the specified engine and scales the
     * menu and games to the specified resolution. A back activity needs to be
     * specified so that the previous activity can be returned to.
     *
     * @param engine        the engine
     * @param resolution    the resolution
     * @param backActivity  the previous activity
     */
    public MultiplayerMenuActivity(final PongEngine engine,
            final World2D resolution, final PongActivity backActivity) {
        menu = new LabelMenu(engine, resolution);
        menu.addLabel("Join game", () -> {
            final JoinGameActivity joinActivity =
                    new JoinGameActivity(resolution, this);
            engine.getActivityService().startActivity(new SlideAnimationActivity(
                    SlideDirection.UP, this, joinActivity));
        });
        menu.addLabel("Create game", () -> {
            engine.getActivityService().startActivity(new SlideAnimationActivity(
                    SlideDirection.UP, this,
                    new CreateGameActivity(MultiplayerMenuActivity.this)));
        });
        menu.addLabel("Back", () -> {
            engine.getActivityService().startActivity(new SlideAnimationActivity(
                    SlideDirection.LEFT, this, backActivity));
        });
    }
    @Override
    public final void onActivityStarted(final Context context) {
        // Empty
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        // Empty.
    }
    @Override
    public final void update() {
        menu.update();
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        g.clearRect(0, 0, size.width, size.height);
        menu.render(g, size);
    }
}
