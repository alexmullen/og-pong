package mullen.alex.pong.net.server;

import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.server.PongServer.ServerState;

/**
 * Represents the state when the server has shutdown.
 *
 * @author  Alex Mullen
 *
 */
public class ShutdownState implements ServerState {
    @Override
    public final void start() {
        throw new IllegalStateException(
                "Cannot invoke start once shutdown.");
    }
    @Override
    public final void shutdown() {
        PongServer.LOG.info("Shutdown invoked when already shutdown.");
    }
    @Override
    public final void onNewConnection(final PongClientConnection connection) {
        throw new IllegalStateException(
                "Cannot accept connections whilst shutdown.");
    }
    @Override
    public final void onDisconnected(final PongClientConnection client) {
        throw new IllegalStateException(
                "Connection disconnected but server is shutdown.");
    }
    @Override
    public final void onReceivedFrame(final PongClientConnection client,
            final PongFrame frame) {
        PongServer.LOG.warning("Received frame [" + frame + "] from client ["
                + client + "] but all clients disconnected so discarding it.");
    }
    @Override
    public final void tick() {
        throw new IllegalStateException(
                "A tick was scheduled when it should not have.");
    }
}
