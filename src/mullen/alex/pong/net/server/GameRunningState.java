package mullen.alex.pong.net.server;

import java.awt.Point;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import mullen.alex.jge.Vector2f;
import mullen.alex.pong.Ball;
import mullen.alex.pong.BallCollisionDetector;
import mullen.alex.pong.BallCollisionDetector.Collision;
import mullen.alex.pong.BallCollisionDetector.Collision.With;
import mullen.alex.pong.BallCollisionDetector.WorldCollision;
import mullen.alex.pong.BallCollisionDetector.WorldCollision.Edge;
import mullen.alex.pong.BallCollisionResolver;
import mullen.alex.pong.Game;
import mullen.alex.pong.Paddle;
import mullen.alex.pong.World2D;
import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.Role;
import mullen.alex.pong.net.PongFrame.Type;
import mullen.alex.pong.net.server.PongServer.ServerState;

/**
 * Represents the state where the game simulation is running and we are
 * actively communicating with the clients.
 *
 * @author  Alex Mullen
 *
 */
public class GameRunningState implements ServerState {
    /** The frequency in seconds to send ping clients. */
    private static final int PING_FREQUENCY_SECONDS = 1;
    /** The server instance. */
    private final PongServer server;
    /** The repeating ping task. */
    private final ScheduledFuture<?> pingTask;
    /** Holds the game simulation. */
    private final Game game;
    /**
     * Creates a new instance that references the fields from the specified
     * server instance.
     *
     * @param serverInstance  the server instance to use
     */
    GameRunningState(final PongServer serverInstance) {
        server = Objects.requireNonNull(serverInstance);
        game = server.gameBuilder.createGame();
        pingTask = server.executor.scheduleWithFixedDelay(this::pingAllClients,
                0, PING_FREQUENCY_SECONDS, TimeUnit.SECONDS);
    }
    @Override
    public final void shutdown() {
        server.changeState(new ShuttingDownState(server));
        pingTask.cancel(true);
        server.connectionListener.shutdown();
        server.connections.keySet().forEach(conn -> conn.close());
    }
    @Override
    public final void onNewConnection(final PongClientConnection connection) {
        // Reject anyone else connecting.
        // TODO: Could shutdown listener?
        connection.close();
    }
    @Override
    public final void onDisconnected(final PongClientConnection client) {
        server.connections.remove(client);
        /*
         * We need to check if we still have the minimum number of
         * authorised connections to play the game.
         */
        if (server.connections.values().stream()
                .filter(bundle -> bundle.authorised)
                .count() < 2) {
            shutdown();
        }
    }
    @Override
    public final void onReceivedFrame(final PongClientConnection client,
            final PongFrame frame) {
        final PongClientBundle clientBundle = server.connections.get(client);
        if (frame.getType() == Type.INPUT) {
            clientBundle.inputs.add(frame.input);
        } else if (frame.getType() == Type.PING_REPLY) {
            final long ping = System.currentTimeMillis()
                    - clientBundle.lastPingSentTime;
            PongServer.LOG.info("Received ping reply from client "
                    + client + " which took: " + ping + "ms");
            clientBundle.ping = ping;
            clientBundle.pingRepliesReceived++;
            if (clientBundle.pingRepliesReceived != clientBundle.pingsSent) {
                PongServer.LOG.severe("Mismatch between pings sent ("
                        + clientBundle.pingsSent + ") and ping replies"
                                + " received ("
                        + clientBundle.pingRepliesReceived + "). "
                                + "Closing connection because of "
                                + "this.");
                client.close();
            }
        } else {
            PongServer.LOG.warning("Received unexpected frame: " + frame);
        }
    }
    @Override
    public final void tick() {
        applyQueuedInputs();
        game.getBall().move();
        checkForAndHandleAnyCollisions();
    }
    private void checkForAndHandleAnyCollisions() {
        final BallCollisionDetector collDetector = game.getCollisionDetector();
        final BallCollisionResolver collResolver = game.getCollisionResolver();
        Collision collision = collDetector.check(game);
        while (collision != null) {
            if (collision.getWith() == With.WORLD) {
                final WorldCollision wc = (WorldCollision) collision;
                if (wc.getEdge() == Edge.LEFT) {
                    // Right side has scored.
                    game.setRightSideScore(game.getRightSideScore() + 1);
                    broadcastScoreUpdateEvent();
                    centreBallWithRandomVelocity();
                    broadcastBallSpawnEvent();
                } else if (wc.getEdge() == Edge.RIGHT) {
                    // Left side has scored.
                    game.setLeftSideScore(game.getLeftSideScore() + 1);
                    broadcastScoreUpdateEvent();
                    centreBallWithRandomVelocity();
                    broadcastBallSpawnEvent();
                } else {
                    // We hit the top or bottom edge so resolve.
                    collResolver.resolve(collision);
                }
            } else {
                collResolver.resolve(collision);
                if (collision.getWith() == With.PADDLE) {
                    broadcastBallHitEvent();
                }
            }
            // Check if collisions have been resolved.
            collision = collDetector.check(game);
        }
    }
    /**
     * Centres the ball and gives it a random direction velocity.
     */
    private void centreBallWithRandomVelocity() {
        final Ball ball = game.getBall();
        final World2D world = game.getWorld();
        final Random rng = game.getRng();
        // Centre the ball.
        final Point centre = world.getCentre();
        ball.getTransform().x = centre.x - ball.getTransform().width / 2;
        ball.getTransform().y = centre.y - ball.getTransform().width / 2;
        final Vector2f startVector =
                new Vector2f(ball.getTransform().x, ball.getTransform().y);
        Vector2f endVector;
        // Randomly decide to initially direct the ball left or right.
        if (rng.nextBoolean()) {
            // Left. Multiply the world height by two to allow initial bounces.
            endVector = new Vector2f(0.0f, rng.nextInt(world.getHeight() * 2));
        } else {
            // Right. Multiply the world height by two to allow initial bounces.
            endVector = new Vector2f(
                    world.getWidth(), rng.nextInt(world.getHeight() * 2));
        }
        final Vector2f dirVector = endVector.subtract(startVector);
        ball.getVelocity().x = dirVector.x;
        ball.getVelocity().y = dirVector.y;
    }
    /**
     * Applies all queued inputs into our simulation of the game for the
     * current tick.
     */
    private void applyQueuedInputs() {
        server.connections.entrySet()
        .stream()
        .filter(entry -> entry.getValue().role != null)
        .forEach(entry -> {
            if (Role.LEFT_PADDLE == entry.getValue().role) {
                while (!entry.getValue().inputs.isEmpty()) {
                    game.getLeftPaddle().move(entry.getValue().inputs.peek());
                    broadcastPaddleMoveEvent(Role.LEFT_PADDLE, entry.getValue().inputs.poll());
                }
            } else if (Role.RIGHT_PADDLE == entry.getValue().role) {
                while (!entry.getValue().inputs.isEmpty()) {
                    game.getRightPaddle().move(entry.getValue().inputs.peek());
                    broadcastPaddleMoveEvent(Role.RIGHT_PADDLE, entry.getValue().inputs.poll());
                }
            } else {
                PongServer.LOG.severe("Unknown role: " + entry.getValue().role);
            }
        });
    }
    private void broadcastPaddleMoveEvent(final Role role,
            final Paddle.Input input) {
        final PongFrame frame = new PongFrame(Type.EVENT);
        frame.args.put("EVENT", "PADDLE_MOVE_EVENT");
        frame.role = role;
        frame.input = input;
        server.connections.keySet().parallelStream().forEach(
                c -> c.sendFrameToClient(frame));
    }
    private void broadcastBallHitEvent() {
        final PongFrame frame = new PongFrame(Type.EVENT);
        frame.args.put("EVENT", "BALL_HIT_EVENT");
        frame.destPositionFloat = game.getBall().getTransform();
        frame.velocity = game.getBall().getVelocity();
        server.connections.keySet().parallelStream().forEach(
                c -> c.sendFrameToClient(frame));
    }
    private void broadcastScoreUpdateEvent() {
        final PongFrame frame = new PongFrame(Type.EVENT);
        frame.args.put("EVENT", "SCORE_UPDATE_EVENT");
        frame.pings = new EnumMap<>(Role.class);
        frame.pings.put(Role.LEFT_PADDLE, Integer.valueOf(game.getLeftSideScore()));
        frame.pings.put(Role.RIGHT_PADDLE, Integer.valueOf(game.getRightSideScore()));
        server.connections.keySet().parallelStream().forEach(
                c -> c.sendFrameToClient(frame));
    }
    private void broadcastBallSpawnEvent() {
        final PongFrame frame = new PongFrame(Type.EVENT);
        frame.args.put("EVENT", "BALL_SPAWN_EVENT");
        frame.destPositionFloat = game.getBall().getTransform();
        frame.velocity = game.getBall().getVelocity();
        server.connections.keySet().parallelStream().forEach(
                c -> c.sendFrameToClient(frame));
    }
    @Override
    public final void start() {
        throw new IllegalStateException(
                "Cannot invoke start on an already started server.");
    }
    /**
     * Sends a ping frame to every connected client.
     */
    private void pingAllClients() {
        server.connections.keySet().parallelStream().forEach(this::sendPing);
    }
    /**
     * Sends a ping to the specified client and updates the bundle data
     * relating to the time the ping was sent and how many ping frames sent.
     *
     * @param client  the client to ping
     */
    private void sendPing(final PongClientConnection client) {
        final PongFrame pingFrame = new PongFrame(Type.PING);
        final PongClientBundle clientBundle = server.connections.get(client);
        final int leftPlayerPing = (int) server.connections.entrySet()
              .stream()
              .filter(entry -> entry.getValue().role == Role.LEFT_PADDLE)
              .findFirst().get().getValue().ping;
        final int rightPlayerPing = (int) server.connections.entrySet()
                .stream()
                .filter(entry -> entry.getValue().role == Role.RIGHT_PADDLE)
                .findFirst().get().getValue().ping;
        // Transfer the ping for each player role within the frame.
        pingFrame.pings = new EnumMap<>(Role.class);
        pingFrame.pings.put(
                Role.LEFT_PADDLE, Integer.valueOf(leftPlayerPing));
        pingFrame.pings.put(
                Role.RIGHT_PADDLE, Integer.valueOf(rightPlayerPing));
        // Send and update bundle data.
        client.sendFrameToClient(pingFrame);
        clientBundle.pingsSent++;
        clientBundle.lastPingSentTime = System.currentTimeMillis();
    }
}
