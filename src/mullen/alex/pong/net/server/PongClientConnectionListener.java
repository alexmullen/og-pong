package mullen.alex.pong.net.server;

import java.io.IOException;

import mullen.alex.pong.net.ConnectionListener;

/**
 * Defines an interface for a listener that produces connection instances of
 * type {@link PongClientConnection}.
 *
 * @author  Alex Mullen
 *
 */
public interface PongClientConnectionListener extends ConnectionListener {
    /**
     * Defines the interface for a handler that handles each new connection.
     *
     * @author  Alex Mullen
     */
    @FunctionalInterface
    interface Handler {
        /**
         * Invoked for each new connection.
         *
         * @param connection  the connection
         */
        void onNewConnection(PongClientConnection connection);
    }
    /**
     * Defines the interface for a builder class for constructing types of
     * connection listeners.
     * <p>
     * This allows callers to inject instances of this into objects and allowing
     * them objects to inject their own handlers without knowing what the
     * concrete implementation of listener this is.
     *
     * @author  Alex Mullen
     */
    @FunctionalInterface
    interface Builder {
        /**
         * Build a <code>ConnectionListener</code> and inject the specified
         * <code>Handler</code> into it.
         *
         * @param newConnHandler       the connection handler to be notified of
         *                             the new connection
         * @param connEventHandler     the handler to inject into the connection
         *                             for handling its events
         *
         * @return                     the new
         *                             <code>PongClientConnectionListener</code>
         *                             instance
         *
         * @throws IOException  if an I/O error occurs
         */
        PongClientConnectionListener build(Handler newConnHandler,
                PongClientConnection.Handler connEventHandler)
                        throws IOException;
    }
}
