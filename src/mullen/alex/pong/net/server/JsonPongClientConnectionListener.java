package mullen.alex.pong.net.server;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import mullen.alex.pong.net.StreamConnection;
import mullen.alex.pong.net.StreamConnectionListener;

/**
 * A {@link PongClientConnectionListener} implementation that listens for and
 * produces {@link JsonPongClientConnection} type connections.
 *
 * @author  Alex Mullen
 *
 */
public class JsonPongClientConnectionListener implements
        PongClientConnectionListener, StreamConnectionListener.Handler {
    /** The logger instance for this class. */
    static final Logger LOG =
            Logger.getLogger(JsonPongClientConnectionListener.class.getName());
    /** Holds the raw stream connection listener. */
    private final StreamConnectionListener connectionListener;
    /** Holds the connection handler. */
    private final PongClientConnectionListener.Handler newConnectionHandler;
    /** Holds the connection event handler. */
    private final PongClientConnection.Handler connectionEventHandler;
    /**
     * Creates a new instance using the specified listener builder, connection
     * listener event handler and the connection event handler.
     *
     * @param listenerBuilder   the listener implementation builder
     * @param connHandler       the listener connection handler
     * @param connEventHandler  the connection event handler to inject into new
     *                          connections
     *
     * @throws IOException  if an exception occurs whilst building the listener
     */
    public JsonPongClientConnectionListener(
            final StreamConnectionListener.Builder listenerBuilder,
            final PongClientConnectionListener.Handler connHandler,
            final PongClientConnection.Handler connEventHandler)
                    throws IOException {
        connectionListener = listenerBuilder.build(this);
        newConnectionHandler = Objects.requireNonNull(connHandler);
        connectionEventHandler = Objects.requireNonNull(connEventHandler);
    }
    @Override
    public final void start() {
        connectionListener.start();
    }
    @Override
    public final void shutdown() {
        connectionListener.shutdown();
    }
    @Override
    public final void onNewConnection(final StreamConnection connection) {
        try {
            final JsonPongClientConnection jsonConnection =
                    new JsonPongClientConnection(connection,
                            connectionEventHandler);
            newConnectionHandler.onNewConnection(jsonConnection);
            jsonConnection.initialise();
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    /**
     * A builder class for deferring the construction of this client listener to
     * another class without that class knowing the concrete details about the
     * listener.
     *
     * @author  Alex Mullen
     *
     */
    public static class Builder implements
                PongClientConnectionListener.Builder {
        /** Holds the stream connection listener builder we use. */
        private final StreamConnectionListener.Builder streamListenerBuilder;
        /**
         * Creates a new instance that will use the specified builder.
         *
         * @param listenerBuilder  the stream connection listener builder
         */
        public Builder(final StreamConnectionListener.Builder listenerBuilder) {
            streamListenerBuilder = listenerBuilder;
        }
        @Override
        public final PongClientConnectionListener build(
                final PongClientConnectionListener.Handler newConnHandler,
                final PongClientConnection.Handler connEventHandler)
                throws IOException {
            return new JsonPongClientConnectionListener(streamListenerBuilder,
                    newConnHandler, connEventHandler);
        }
    }
}
