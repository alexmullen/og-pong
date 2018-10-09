package mullen.alex.pong.net.server;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a pong client listener that essentially passes around object
 * references to simulate an incoming connection as far as the client user
 * is concerned.
 *
 * @author  Alex Mullen
 *
 */
public class ReferencePongClientConnectionListener implements
        PongClientConnectionListener {
    /** Indicates whether this has been started. */
    private boolean started;
    /** Indicates whether this has been shutdown. */
    private boolean shutdown;
    /** Holds the connection handler. */
    private final Handler handler;
    /**
     * Creates a new instance with the specified connection handler.
     *
     * @param connHandler  the handler
     */
    public ReferencePongClientConnectionListener(final Handler connHandler) {
        handler = Objects.requireNonNull(connHandler);
    }
    @Override
    public final void start() {
        if (started) {
            throw new IllegalStateException();
        } else {
            started = true;
        }
    }
    @Override
    public final void shutdown() {
        shutdown = true;
    }
    /**
     * Injects the specified connection into this listener so that an incoming
     * connection is stimulated.
     *
     * @param connection  the connection to inject
     */
    public final void injectConnection(final PongClientConnection connection) {
        if (shutdown || !started) {
            throw new IllegalStateException();
        } else {
            handler.onNewConnection(connection);
        }
    }
    /**
     * A builder for producing connection listeners of type
     * {@link ReferencePongClientConnectionListener}.
     *
     * @author  Alex Mullen
     *
     */
    public static class ReferenceBuilder implements Builder {
        @Override
        public final PongClientConnectionListener build(
                final Handler newConnHandler,
                final PongClientConnection.Handler connEventHandler)
                throws IOException {
            return new ReferencePongClientConnectionListener(newConnHandler);
        }
    }
}
