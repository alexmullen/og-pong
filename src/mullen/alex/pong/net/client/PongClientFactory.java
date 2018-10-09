package mullen.alex.pong.net.client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mullen.alex.pong.net.SocketConnection;
import mullen.alex.pong.net.StreamConnection;
import mullen.alex.pong.net.server.PongServer;

/**
 * A factory for managing and abstracting away the details of creating an
 * instance of a {@link PongClient}.
 *
 * @author  Alex Mullen
 *
 */
public final class PongClientFactory {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(PongClientFactory.class.getName());
    /**
     * Private constructor to prevent instantiation.
     */
    private PongClientFactory() {
        // Intentionally empty.
    }
    /**
     * Creates a new client that communicates with a server directly through
     * a reference to the server instance.
     *
     * @param server   the server instance
     * @return         the created client instance
     */
    public static PongClient createAndConnect(final PongServer server) {
        final ReferencePongClient client =
                new ReferencePongClient(server);
        server.onNewConnection(client);
        return client;
    }
    /**
     * Creates a new client that communicates with a server that resides at the
     * specified host and port.
     *
     * @param hostname  the remote host name or IP address
     * @param port      the remote port
     * @return          the created client instance
     *
     * @throws UnknownHostException  if the IP address of the host could not be
     *                               determined
     *
     * @throws IOException           if an I/O error occurs
     */
    @SuppressWarnings("resource")
    public static PongClient createAndConnect(final String hostname,
            final int port)
                    throws UnknownHostException, IOException {
        final Socket connectSocket = new Socket(hostname, port);
        // Disable Nagle's algorithm on the socket.
        try {
            connectSocket.setTcpNoDelay(true);
        } catch (final SocketException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            // Close socket and re-throw original exception.
            try {
                connectSocket.close();
            } catch (final IOException e1) {
                LOG.log(Level.SEVERE, e1.getMessage(), e1);
            }
            throw e;
        }
        // Wrap the socket into a StreamConnection instance.
        final StreamConnection connection = new SocketConnection(connectSocket);
        try {
            return new JsonPongClient(connection);
        } catch (final IOException e2) {
            LOG.log(Level.SEVERE, e2.getMessage(), e2);
            // Close the connection which in turn closes the socket we created.
            connection.close();
            throw e2;
        }
    }
}
