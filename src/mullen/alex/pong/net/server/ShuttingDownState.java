package mullen.alex.pong.net.server;

import java.util.Objects;

import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.server.PongServer.ServerState;

/**
 * Represents the state when the server is shutting down so we need to wait for
 * the last frames to be received and clients to disconnect.
 *
 * @author  Alex Mullen
 *
 */
public class ShuttingDownState implements ServerState {
    /** The server instance. */
    private final PongServer server;
    /**
     * Creates a new instance that references the fields from the specified
     * server instance.
     *
     * @param serverInstance  the server instance to use
     */
    ShuttingDownState(final PongServer serverInstance) {
        server = Objects.requireNonNull(serverInstance);
    }
    @Override
    public final void onDisconnected(final PongClientConnection client) {
        server.connections.remove(client);
        // If all connections have disconnected, shutdown.
        if (server.connections.isEmpty()) {
            server.changeState(new ShutdownState());
            server.executor.shutdown();
        }
    }
    @Override
    public final void onReceivedFrame(final PongClientConnection client,
            final PongFrame frame) {
        PongServer.LOG.warning("Received frame [" + frame + "] from client but "
                + "shutting down so discarding it.");
    }
    @Override
    public final void onNewConnection(final PongClientConnection connection) {
        throw new IllegalStateException(
                "Cannot accept connections whilst shutting down.");
    }
    @Override
    public final void start() {
        throw new IllegalStateException(
                "Cannot invoke start once shutting down.");
    }
    @Override
    public final void shutdown() {
        PongServer.LOG.info("Shutdown invoked when already shutting down.");
    }
    @Override
    public final void tick() {
        PongServer.LOG.warning("Ignoring scheduled tick due to shutdown...");
    }
}
