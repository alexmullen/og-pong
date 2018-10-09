package mullen.alex.pong.net.server;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.PongFrame.Type;
import mullen.alex.pong.net.PongPlayer;
import mullen.alex.pong.net.Role;
import mullen.alex.pong.net.server.PongServer.ServerState;

/**
 * Represents the state after the server has started and we are waiting for
 * all clients to connect, authorise and be ready.
 *
 * @author  Alex Mullen
 *
 */
public class WaitingForConnectionsToBeReadyState implements ServerState {
    /** How often the server updates the game state per second. */
    private static final int TICK_RATE = 60;
    /** The number of initial ping frames to send during hand-shake. */
    private static final int INITIAL_PINGS_TO_SEND = 10;
    /** The server instance. */
    private final PongServer server;
    /**
     * Creates a new instance that references the fields from the specified
     * server instance.
     *
     * @param serverInstance  the server instance to use
     */
    WaitingForConnectionsToBeReadyState(final PongServer serverInstance) {
        server = Objects.requireNonNull(serverInstance);
    }
    @Override
    public final void shutdown() {
        server.changeState(new ShuttingDownState(server));
        server.connectionListener.shutdown();
        server.connections.keySet().forEach(conn -> conn.close());
    }
    @Override
    public final void onNewConnection(final PongClientConnection connection) {
        PongServer.LOG.info("client connected: " + connection);
        server.connections.put(connection, new PongClientBundle());
    }
    @Override
    public final void onDisconnected(final PongClientConnection client) {
        PongServer.LOG.info("client disconnected: " + client);
        server.connections.remove(client);
    }
    @Override
    public final void onReceivedFrame(final PongClientConnection client,
            final PongFrame frame) {
        final PongClientBundle clientBundle = server.connections.get(client);
        /*
         * Make sure it is an authorisation frame if not authorised as that
         * should be the first frame sent.
         */
        if (clientBundle.authorised) {
            handleFrameFromAuthedClients(client, clientBundle, frame);
        } else {
            // Not currently authorised so it better be an authorisation frame.
            if (frame.getType() == Type.AUTHORISATION) {
                PongServer.LOG.info("client authorised: " + client);
                clientBundle.authorised = true;
                clientBundle.name = frame.args.get("NAME"); // !!! Maybe validate name first!
                sendPing(client);
            } else {
                PongServer.LOG.severe("Received unexpected frame from client: "
                        + client + " of type " + frame.getType()
                        + " when we expected type of " + Type.AUTHORISATION
                        + ". Closing connection because of this.");
                client.close();
            }
        }
    }
    /**
     * Handles frames received from authorised clients.
     *
     * @param client  the client
     * @param bundle  the client's data bundle
     * @param frame   the received frame
     */
    private void handleFrameFromAuthedClients(
            final PongClientConnection client,
            final PongClientBundle bundle, final PongFrame frame) {
        if (frame.getType() == Type.PING_REPLY) {
            final long ping =
                    System.currentTimeMillis() - bundle.lastPingSentTime;
            bundle.ping = ping;
            bundle.pingRepliesReceived++;
            if (bundle.pingRepliesReceived != bundle.pingsSent) {
                PongServer.LOG.severe("Mismatch between pings sent ("
                        + bundle.pingsSent + ") and ping replies"
                                + " received ("
                        + bundle.pingRepliesReceived + "). "
                                + "Closing connection because of "
                                + "this.");
                client.close();
            } else if (bundle.pingRepliesReceived < INITIAL_PINGS_TO_SEND) {
                sendPing(client);
            } else if (isReadyToStart()) {
                
                server.changeState(new GameRunningState(server));
                assignRolesToClients();
                sendStartEventToClients();
                // Start the simulation.
                server.executor.scheduleAtFixedRate(
                        server::tick, 0, 1000 / TICK_RATE, TimeUnit.MILLISECONDS);
            }
        } else {
            PongServer.LOG.severe("Received unexpected frame from client: "
                    + client + " of type " + frame.getType()
                    + ". Closing connection because of this.");
            client.close();
        }
    }
    /**
     * Assigns player roles to clients.
     */
    private void assignRolesToClients() {
        final Queue<Role> roles = new ArrayDeque<>();
        roles.add(Role.LEFT_PADDLE);
        roles.add(Role.RIGHT_PADDLE);
        server.connections.values()
            .stream()
            .limit(roles.size())
            .forEach(clientBundle -> clientBundle.role = roles.remove());
    }
    /**
     * Sends the "STARTED" event frame to the clients.
     */
    private void sendStartEventToClients() {
        server.connections.entrySet().parallelStream().forEach(
                conn -> sendStartEventToClient(conn.getKey(), conn.getValue()));
    }
    /**
     * Sends the "STARTED" event frame to a specific client.
     *
     * @param conn    the client connection
     * @param bundle  the associated bundle for the client
     */
    private void sendStartEventToClient(final PongClientConnection conn,
            final PongClientBundle bundle) {
        final PongFrame startedEventFrame = new PongFrame(Type.EVENT);
        startedEventFrame.args.put("EVENT", "STARTED");
        startedEventFrame.args.put("ROLE", bundle.role.name());
        startedEventFrame.players = new EnumMap<>(Role.class);
        final PongClientBundle leftPaddleBundle = server.connections.entrySet()
                .stream()
                .filter(entry -> entry.getValue().role == Role.LEFT_PADDLE)
                .findFirst().get().getValue(); // !!!check
        startedEventFrame.players.put(Role.LEFT_PADDLE,
                new PongPlayer(leftPaddleBundle.name, Role.LEFT_PADDLE,
                        leftPaddleBundle.ping));
        final PongClientBundle rightPaddleBundle = server.connections.entrySet()
                .stream()
                .filter(entry -> entry.getValue().role == Role.RIGHT_PADDLE)
                .findFirst().get().getValue(); // !!!check
        startedEventFrame.players.put(Role.RIGHT_PADDLE,
                new PongPlayer(rightPaddleBundle.name, Role.RIGHT_PADDLE,
                        rightPaddleBundle.ping));
        conn.sendFrameToClient(startedEventFrame);
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
        client.sendFrameToClient(pingFrame);
        clientBundle.pingsSent++;
        clientBundle.lastPingSentTime = System.currentTimeMillis();
    }
    /**
     * Determines whether all the preconditions for starting the game are
     * true.
     *
     * @return  <code>true</code> if the game is ready to start;
     *          <code>false</code> if not
     */
    private boolean isReadyToStart() {
        /*
         * If we have the required number of connections ready then
         * we can start the game.
         */
        final long connectionsReady = server.connections.values()
                .stream()
                .filter(bundle -> bundle.authorised
                        && bundle.pingRepliesReceived == INITIAL_PINGS_TO_SEND)
                .count();
        return connectionsReady == 2;
    }
    @Override
    public final void start() {
        throw new IllegalStateException(
                "Cannot invoke start on an already started server.");
    }
    @Override
    public final void tick() {
        throw new IllegalStateException(
                "A tick was scheduled when it should not have been.");
    }
}
