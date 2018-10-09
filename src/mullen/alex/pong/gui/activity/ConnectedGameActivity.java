package mullen.alex.pong.gui.activity;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Logger;

import mullen.alex.jge.input.KeyboardService;
import mullen.alex.pong.Ball;
import mullen.alex.pong.Game;
import mullen.alex.pong.BallCollisionDetector.Collision;
import mullen.alex.pong.BallCollisionDetector.WorldCollision;
import mullen.alex.pong.BallCollisionDetector.Collision.With;
import mullen.alex.pong.BallCollisionDetector.WorldCollision.Edge;
import mullen.alex.pong.Paddle.Input;
import mullen.alex.pong.StandardGameBuilder;
import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.GameRenderer;
import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.PongFrame.Type;
import mullen.alex.pong.net.PongPlayer;
import mullen.alex.pong.net.Role;
import mullen.alex.pong.net.client.PongClient;

/**
 * Represents the activity where we have connected to a remote server and are
 * able to partake in the game.
 *
 * @author  Alex Mullen
 *
 */
public class ConnectedGameActivity implements PongActivity {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(ConnectedGameActivity.class.getName());
    /** Holds the game context. */
    private final Game gameContext;
    /** Holds the game renderer. */
    private final GameRenderer gameRenderer;
    /** Holds the engine reference. */
    private final PongEngine engine;
    /** Holds the client that communicates with the server. */
    private final PongClient client;
    /** Holds the game role for this client. */
    private final Role role;
    /** Holds data about the players in the game. */
    private final Map<Role, PongPlayer> players;
    /** Queues up received opponent inputs. */
    private final Queue<Input> opponnentPaddleInputs;
    /** Holds a reference to the local player's paddle animator. */
    private final PaddleAnimator paddleAnimator;
    /** Holds a reference to the opponent player's paddle animator. */
    private final PaddleAnimator opponentPaddleAnimator;
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
    public ConnectedGameActivity(final ConstructorArgs args) {
        Objects.requireNonNull(args);
        engine = Objects.requireNonNull(args.engine);
        client = Objects.requireNonNull(args.client);
        role = Objects.requireNonNull(args.clientRole);
        players = Objects.requireNonNull(args.players);
        gameContext = new StandardGameBuilder().createGame();   // TODO: Hardcoded!
        gameRenderer = new GameRenderer(gameContext);
        opponnentPaddleInputs = new ArrayDeque<>();
        if (role == Role.LEFT_PADDLE) {
            paddleAnimator = new PaddleAnimator(gameContext.getLeftPaddle());
            opponentPaddleAnimator =
                    new PaddleAnimator(gameContext.getRightPaddle());
        } else {
            paddleAnimator = new PaddleAnimator(gameContext.getRightPaddle());
            opponentPaddleAnimator =
                    new PaddleAnimator(gameContext.getLeftPaddle());
        }
        paddleAnimator.start(Input.NONE);
        opponentPaddleAnimator.start(Input.NONE);
    }
    @Override
    public final void onActivityStarted(final Context context) {
        final Thread recvThread = new Thread(() -> {
            boolean continueReceiving = true;
            do {
                final PongFrame frame = client.recvFrameFromServer();
                if (frame == null) {
                    continueReceiving = false;
                } else {
                    handleReceivedFrame(frame);
                }
            } while (continueReceiving);
            handleDisconnect();
        });
        recvThread.start();
    }
    /**
     * Handles the event the connection to the server is closed.
     */
    private void handleDisconnect() {
        engine.execute(() -> {
            LOG.info("client disconnected, changing activity");
            engine.getActivityService().startActivity(
                    new MainMenuActivitiy(engine, new World2D(1024, 768)));
        });
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        client.close();
    }
    @Override
    public final void update() {
        performInput();
        moveBall();
        opponentPaddleAnimator.finish();
        if (opponnentPaddleInputs.isEmpty()) {
            opponentPaddleAnimator.start(Input.NONE);
        } else {
            Input nextInput = opponnentPaddleInputs.remove();
            // TODO: hopefully temporary.
            while (nextInput == Input.NONE
                    && !opponnentPaddleInputs.isEmpty()) {
                nextInput = opponnentPaddleInputs.remove();
            }
            opponentPaddleAnimator.start(nextInput);
        }
    }
    /**
     *  Retrieve and apply input.
     */
    private void performInput() {
        paddleAnimator.finish();
        // Send inputs to server every tick.
        final PongFrame inputFrame = new PongFrame(Type.INPUT);
        if (role == Role.LEFT_PADDLE) {
            inputFrame.input = getPaddleInput(KeyEvent.VK_W, KeyEvent.VK_S);
            // Smoothly predict the move.
            paddleAnimator.start(inputFrame.input);
        } else if (role == Role.RIGHT_PADDLE) {
            inputFrame.input = getPaddleInput(KeyEvent.VK_UP, KeyEvent.VK_DOWN);
            // Smoothly predict the move.
            paddleAnimator.start(inputFrame.input);
        } else {
            throw new IllegalStateException("Unhandled role: " + role);
        }
        // Send the frame.
        client.sendFrameToServer(inputFrame);
        // Check if the tab key is currently pressed.
        tabPressed = engine.getKeyboardService().isPressed(KeyEvent.VK_TAB)
                ? true : false;
    }
    /**
     * Move the ball and handle any collisions.
     */
    private void moveBall() {
        gameContext.getBall().move();
        Collision collision =
                gameContext.getCollisionDetector().check(gameContext);
        // TODO: This might be getting stuck within an infinite loop.
        boolean ballCollidedWithPaddle = false;
        while (collision != null) {
            if (collision.getWith() == With.WORLD) {
                final WorldCollision wc = (WorldCollision) collision;
                if (wc.getEdge() != Edge.LEFT && wc.getEdge() != Edge.RIGHT) {
                    /*
                     * We can resolve ceiling and floor collisions but server
                     * should have final say on whether it the ball collided
                     * with sides.
                     */
                    gameContext.getCollisionResolver().resolve(collision);
                } else {
                    // TODO: What do we do in the meantime...
                    break;
                }
            } else {
                // Paddle collision.
                gameContext.getCollisionResolver().resolve(collision);
                ballCollidedWithPaddle = true;
            }
            collision = gameContext.getCollisionDetector().check(gameContext);
        }
        if (ballCollidedWithPaddle) {
            // If it wasn't out paddle then send ball hit event...
            
            
        }
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        paddleAnimator.animate(delta);
        opponentPaddleAnimator.animate(delta);
        gameRenderer.render(g, size);
        if (tabPressed) {
            gameRenderer.renderPaddleInfo(g, players);
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
    /**
     * Handles received frames from the server.
     *
     * @param frame  the frame to handle
     */
    private void handleReceivedFrame(final PongFrame frame) {
        switch (frame.getType()) {
            case EVENT:
                handleEvent(frame);
                break;
            case PING:
                handlePing(frame);
                break;
            case SNAPSHOT:
                break;
            //$CASES-OMITTED$
            default:
//                LOG.severe("Unhandled frame type: " + frame.getType());
        }
    }
    /**
     * Handles a received EVENT frame.
     *
     * @param frame  the received frame
     */
    private void handleEvent(final PongFrame frame) {
        final String eventType = frame.args.get("EVENT");
        if ("PADDLE_MOVE_EVENT".equals(eventType)) {
            handlePaddleMoveEvent(frame);
        } else if ("BALL_HIT_EVENT".equals(eventType)) {
            engine.execute(() ->
                copyBallPosAndVelocityFromFrame(frame)
            );
        } else if ("SCORE_UPDATE_EVENT".equals(eventType)) {
            handleScoreUpdateEvent(frame);
        } else if ("BALL_SPAWN_EVENT".equals(eventType)) {
            handleBallSpawnEvent(frame);
        } else {
            LOG.severe("Unhandled event type: " + eventType);
        }
    }
    /**
     * Handles a received PADDLE_MOVE_EVENT frame.
     *
     * @param frame  the received frame
     */
    private void handlePaddleMoveEvent(final PongFrame frame) {
        if (role != frame.role) {
            engine.execute(() ->
                opponnentPaddleInputs.add(frame.input)
            );
        }
    }
    /**
     * Handles a received SCORE_UPDATE_EVENT frame.
     *
     * @param frame  the received frame
     */
    private void handleScoreUpdateEvent(final PongFrame frame) {
        final int leftSidePaddlePing =
                frame.pings.get(Role.LEFT_PADDLE).intValue();
        final int rightSidePaddlePing =
                frame.pings.get(Role.RIGHT_PADDLE).intValue();
        engine.execute(() -> {
            gameContext.setLeftSideScore(leftSidePaddlePing);
            gameContext.setRightSideScore(rightSidePaddlePing);
        });
    }
    /**
     * Handles a received BALL_SPAWN_EVENT frame.
     *
     * @param frame  the received frame
     */
    private void handleBallSpawnEvent(final PongFrame frame) {
        /*
         * is the X velocity of the ball heading for us?
         * if so then
         *      position the ball in its future position (our latency x 2)
         * else
         *      position the ball in its past position (opponent latency + our latency)
         */
        engine.execute(() -> {

            final Ball ball = gameContext.getBall();
            ball.getTransform().x = frame.destPositionFloat.x;
            ball.getTransform().y = frame.destPositionFloat.y;
            ball.getVelocity().x = frame.velocity.x;
            ball.getVelocity().y = frame.velocity.y;
            
            if (isBallHeadingForUs(frame.velocity.x)) {
                // Position the ball in its future position.
                ball.move(getOurPing());
            } else {
                // Position the ball in its past position.
                long oursPlusOpponentsLatencyMs =
                        (players.get(Role.LEFT_PADDLE).getPing() / 2)
                        + (players.get(Role.RIGHT_PADDLE).getPing() / 2);
                ball.getVelocity().x = -ball.getVelocity().x;
                ball.getVelocity().y = -ball.getVelocity().y;
                ball.move(oursPlusOpponentsLatencyMs);
                ball.getVelocity().x = -ball.getVelocity().x;
                ball.getVelocity().y = -ball.getVelocity().y;
            }
        });
    }
    private boolean isBallHeadingForUs(final float xVol) {
        if (role == Role.LEFT_PADDLE) {
            return xVol < 0;
        } else {
            return xVol > 0;
        }
    }
    private long getOurPing() {
        if (role == Role.LEFT_PADDLE) {
            return players.get(Role.LEFT_PADDLE).getPing();
        } else {
            return players.get(Role.RIGHT_PADDLE).getPing();
        }
    }
    /**
     * Copies the ball transform and velocity from the given frame.
     *
     * @param frame  the frame to copy the ball values from
     */
    private void copyBallPosAndVelocityFromFrame(final PongFrame frame) {
        final Ball ball = gameContext.getBall();
        ball.getTransform().x = frame.destPositionFloat.x;
        ball.getTransform().y = frame.destPositionFloat.y;
        ball.getVelocity().x = frame.velocity.x;
        ball.getVelocity().y = frame.velocity.y;
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
}
