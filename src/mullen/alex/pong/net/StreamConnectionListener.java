package mullen.alex.pong.net;

import java.io.IOException;

/**
 * Defines the interface for a connection listener that provides an endpoint for
 * stream connections to connect to.
 *
 * @author  Alex Mullen
 *
 */
public interface StreamConnectionListener extends ConnectionListener {
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
        void onNewConnection(StreamConnection connection);
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
         * Build a <code>StreamConnectionListener</code> and inject the
         * specified <code>Handler</code> into it.
         *
         * @param handler       the handler to inject
         * @return              the new <code>StreamConnectionListener</code>
         *                      instance
         * @throws IOException  if an I/O error occurs
         */
        StreamConnectionListener build(Handler handler) throws IOException;
    }
}
