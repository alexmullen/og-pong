package mullen.alex.pong.net.client;

import java.util.Objects;

import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.server.PongClientConnection;
import mullen.alex.pong.net.server.PongServer;

/**
 * A Pong client implementation that communicates with the pong server via
 * directly referencing the server instance through code and triggering the
 * appropriate call back methods.
 *
 * @author  Alex Mullen
 *
 */
public class ReferencePongClient implements PongClient, PongClientConnection {
    /** The server. */
    private final PongServer serverInstance;
    /** Indicates whether this has being closed. */
    private volatile boolean closed;
    private boolean isFramePending;
    private PongFrame pendingClientRecvFrame;
    /**
     * Creates a new instance that communicates locally with the specified
     * server instance and uses the specified handler.
     *
     * @param server   the server instance
     * @param handler  the handler
     */
    public ReferencePongClient(final PongServer server) {
        serverInstance = Objects.requireNonNull(server);
    }
    @Override
    public final void close() {
        if (!closed) {
            serverInstance.onDisconnected(this);
            closed = true;
        }
    }
    @Override
    public final PongFrame recvFrameFromServer() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public final void sendFrameToClient(final PongFrame frame) {
        if (!closed) {
//            clientHandler.onReceivedFrame(this, frame);
        }
    }
    @Override
    public final void sendFrameToServer(final PongFrame frame) {
        serverInstance.onReceivedFrame(this, frame);
    }
    @Override
    public final String toString() {
        return "ReferencePongClient [closed=" + closed + "]";
    }
}
