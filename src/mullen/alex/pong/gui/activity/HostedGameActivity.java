package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import mullen.alex.jge.input.KeyboardService;
import mullen.alex.pong.Game;
import mullen.alex.pong.Paddle.Input;
import mullen.alex.pong.StandardGameBuilder;
import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.GameRenderer;
import mullen.alex.pong.net.GameSnapshot;
import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.PongPlayer;
import mullen.alex.pong.net.PongFrame.Type;
import mullen.alex.pong.net.Role;
import mullen.alex.pong.net.client.PongClient;
import mullen.alex.pong.net.server.PongServer;

/**
 * Represents the activity where we have connected to a server we are hosting
 * and we are able to partake in the game.
 *
 * @author  Alex Mullen
 *
 */
public class HostedGameActivity implements PongActivity {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(HostedGameActivity.class.getName());
    /** Holds the engine reference. */
    private final PongEngine engine;
    /** Holds the server instance we are hosting. */
    private final PongServer server;
    /** Holds the client that communicates with the server. */
    private final PongClient client;
    /** Holds the game context. */
    private final Game gameContext;
    /** Holds the game renderer. */
    private final GameRenderer gameRenderer;
    /** Holds the game role for this client. */
    private final Role role;
    /** Holds data about the players in the game. */
    private final Map<Role, PongPlayer> players;
    /** Holds whether the tab key was pressed last tick. */
    private boolean tabPressed;
    /**
     * A class for holding the required constructor arguments required for
     * instantiating the outer class.
     * <p>
     * Using an object like this helps improve readability and maintainability
     * when a constructor requires many arguments.
     *
     * @author  Alex Mullen
     *
     */
    public static class ConstructorArgs {
        /** The engine instance. */
        public PongEngine engine;
        /** The server instance. */
        public PongServer server;
        /** The connected client instance. */
        public PongClient client;
        /** Holds the assigned role for this player. */
        public Role clientRole;
        /** Holds data about the players in the game. */
        public Map<Role, PongPlayer> players;
    }
    /**
     * Creates a new instance using the specified arguments.
     *
     * @param args  the supplied arguments
     */
    public HostedGameActivity(final ConstructorArgs args) {
        Objects.requireNonNull(args);
        engine = Objects.requireNonNull(args.engine);
        server = Objects.requireNonNull(args.server);
        client = Objects.requireNonNull(args.client);
        role = Objects.requireNonNull(args.clientRole);
        players = Objects.requireNonNull(args.players);
        gameContext = new StandardGameBuilder().createGame();   // TODO: Hardcoded!
        gameRenderer = new GameRenderer(gameContext);
    }
    @Override
    public final void onActivityStarted(final Context context) {
        final Thread recvThread = new Thread(() -> {
            boolean continueReceiving = true;
            do {
                final PongFrame frame = client.recvFrameFromServer();
                if (frame == null) {
                    client.close();
                    continueReceiving = false;
                } else {
                    handleReceivedFrame(frame);
                }
            } while (continueReceiving);
            handleDisconnect();
        });
        recvThread.start();
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        server.shutdown();
    }
    @Override
    public final void update() {
        // Send inputs to server.
        final PongFrame inputFrame = new PongFrame(Type.INPUT);
        if (role == Role.LEFT_PADDLE) {
            inputFrame.input = getPaddleInput(KeyEvent.VK_W, KeyEvent.VK_S);
            // Prediction.
            gameContext.getLeftPaddle().move(inputFrame.input);
        } else if (role == Role.RIGHT_PADDLE) {
            inputFrame.input = getPaddleInput(KeyEvent.VK_UP, KeyEvent.VK_DOWN);
            // Prediction.
            gameContext.getRightPaddle().move(inputFrame.input);
        } else {
            throw new IllegalStateException("Unhandled role: " + role);
        }
        client.sendFrameToServer(inputFrame);
        if (engine.getKeyboardService().isPressed(KeyEvent.VK_TAB)) {
            tabPressed = true;
        } else {
            tabPressed = false;
        }
    }
    /**
     * Gets the paddle input for the specified key codes if they are pressed.
     *
     * @param upKey    the key code the for upwards key
     * @param downKey  the key code for the downwards key
     * @return         the input
     */
    private Input getPaddleInput(final int upKey, final int downKey) {
        final KeyboardService kbs = engine.getKeyboardService();
        Input input = Input.NONE;
        if (kbs.isPressed(upKey) && !kbs.isPressed(downKey)) {
            input = Input.MOVE_UP;
        } else if (kbs.isPressed(downKey) && !kbs.isPressed(upKey)) {
            input = Input.MOVE_DOWN;
        }
        return input;
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        gameRenderer.render(g, size);
        if (tabPressed) {
            gameRenderer.renderPaddleInfo(g, players);
        }
    }
    /**
     * Handles received frames from the server.
     *
     * @param frame  the frame to handle
     */
    private void handleReceivedFrame(final PongFrame frame) {
        if (frame.getType() == Type.SNAPSHOT) {
            handleSnapshot(frame.snapshot);
        } else if (frame.getType() == Type.PING) {
            handlePing(frame);
        } else {
            LOG.warning("Received unexpected frame: " + frame);
        }
    }
    /**
     * Handles received snapshots.
     *
     * @param snapshot  the snapshot to process
     */
    private void handleSnapshot(final GameSnapshot snapshot) {
        engine.execute(() -> {
            // Update score.
            gameContext.setLeftSideScore(snapshot.getLeftSideScore());
            gameContext.setRightSideScore(snapshot.getRightSideScore());
            // Update ball position.
            gameContext.getBall().getTransform().x = snapshot.getBall().getTransform().x;
            gameContext.getBall().getTransform().y = snapshot.getBall().getTransform().y;
            gameContext.getBall().getVelocity().x = snapshot.getBall().getVelocity().x;
            gameContext.getBall().getVelocity().y = snapshot.getBall().getVelocity().y;
            // Don't reposition our own paddle since it is currently ahead of the server.
            if (role == Role.LEFT_PADDLE) {
                gameContext.getRightPaddle().copyFrom(snapshot.getRightPaddle());
            } else if (role == Role.RIGHT_PADDLE) {
                gameContext.getLeftPaddle().copyFrom(snapshot.getLeftPaddle());
            } else {
                throw new IllegalStateException("Unhandled role: " + role);
            }
        });
    }
    /**
     * Handles received ping frames.
     *
     * @param frame  the received ping frame
     */
    private void handlePing(final PongFrame frame) {
        /*
         * Update player ping values. Remember to update it within the
         * engine thread.
         */
        engine.execute(() -> {
            players.get(Role.LEFT_PADDLE).setPing(
                    frame.pings.get(Role.LEFT_PADDLE).longValue());
            players.get(Role.RIGHT_PADDLE).setPing(
                    frame.pings.get(Role.RIGHT_PADDLE).longValue());
        });
        /*
         * Reply back to server to acknowledge the ping. This is fine being
         * ran outside of the engine thread as this will be ran on the client
         * receive thread.
         */
        client.sendFrameToServer(new PongFrame(Type.PING_REPLY));
    }
    /**
     * Handles the event the connection to the server is closed.
     */
    private void handleDisconnect() {
        engine.execute(() ->
            engine.getActivityService().startActivity(
                    new MainMenuActivitiy(engine, new World2D(1024, 768)))
        );
    }
}
