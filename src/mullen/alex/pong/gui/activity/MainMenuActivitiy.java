package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;

import mullen.alex.pong.World2D;
import mullen.alex.pong.gui.activity.SlideAnimationActivity.SlideDirection;
import mullen.alex.pong.gui.components.LabelMenu;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;

/**
 * Represents the main menu activity that is displayed first.
 *
 * @author  Alex Mullen
 *
 */
public class MainMenuActivitiy implements PongActivity {
    /** Holds the menu. */
    private final LabelMenu menu;
    /**
     * Creates a new instance that uses the specified engine and scales the
     * menu and games to the specified resolution.
     *
     * @param engine      the engine
     * @param resolution  the resolution
     */
    public MainMenuActivitiy(final PongEngine engine, final World2D resolution) {
        menu = new LabelMenu(engine, resolution);
        menu.addLabel("Play", () -> {
//            try {
//                final Game gameContext = new Game(new World2D(1024, 768));
//                final PongActivity gameActivity = new GameActivity(
//                        this, engine);
//                engine.getActivityService().startActivity(new SlideAnimationActivity(engine,
//                        SlideDirection.LEFT, this, gameActivity));
//            } catch (final Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        });
        menu.addLabel("Multiplayer", () -> {
            final PongActivity multipPlayerActivity = new MultiplayerMenuActivity(
                    engine, resolution, this);
            engine.getActivityService().startActivity(new SlideAnimationActivity(
                    SlideDirection.RIGHT, this, multipPlayerActivity));
        });
        menu.addLabel("Options", () -> { /* Empty for now. */ });
        menu.addLabel("Exit", () -> {
            engine.shutdown();
            System.exit(0);
        });
    }
    @Override
    public final void onActivityStarted(final Context context) {
        // Empty.
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
