package mullen.alex.pong.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A server for accepting incoming socket connection requests then wrapping them
 * into a {@link SocketConnection}.
 *
 * @author  Alex Mullen
 *
 */
public class SocketConnectionListener implements StreamConnectionListener {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(SocketConnectionListener.class.getName());
    /** The maximum backlog length of the queue of incoming connections. */
    private static final int SOCKET_BACKLOG_LENGTH = 10;
    /** The handler for handling each accepted connection. */
    private final Handler handler;
    /** The socket instance for this listener. */
    private final ServerSocket listenerSocket;
    /** The socket configure operation. */
    private final Consumer<Socket> socketConfigurer;
    /** The thread that waits for connection requests. */
    private final Thread acceptThread;
    /** A status variable to indicate if this is listener is shutdown. */
    private volatile boolean shutdown;
    /**
     * Instantiates a new instance that will be bound to the specified port
     * number and configures each socket using the given consumer operation.
     *
     * @param port                the local port number be bound to
     * @param configureOperation  the operation to perform on each accepted
     *                            socket
     * @param serverHandler       the handler
     *
     * @throws IOException   if an I/O error occurs whilst binding the socket
     */
    public SocketConnectionListener(final int port,
            final Consumer<Socket> configureOperation,
            final Handler serverHandler) throws IOException {
        handler = Objects.requireNonNull(serverHandler);
        socketConfigurer = Objects.requireNonNull(configureOperation);
        listenerSocket = new ServerSocket(port, SOCKET_BACKLOG_LENGTH);
        acceptThread = new Thread(this::acceptConnectionsThread,
                "SocketConnectionListener::acceptConnectionsThread");
    }
    @Override
    public final void start() {
        if (shutdown) {
            throw new IllegalStateException("Listener is shutdown");
        }
        acceptThread.start();
    }
    @Override
    public final void shutdown() {
        shutdown = true;
        try {
            listenerSocket.close();
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        // Wait for the accept thread to finish.
        try {
            acceptThread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    /**
     * Waits for, then accepts incoming connection requests.
     */
    private void acceptConnectionsThread() {
        while (!shutdown) {
            try {
                @SuppressWarnings("resource")
                final Socket acceptedSocket = listenerSocket.accept();
                // Configure the socket.
                socketConfigurer.accept(acceptedSocket);
                // Let the handler deal with it.
                handler.onNewConnection(new SocketConnection(acceptedSocket));
            } catch (final IOException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }
    /**
     * A builder class that builds instances of this.
     *
     * @author  Alex Mullen
     */
    public static final class Builder implements
            StreamConnectionListener.Builder {
        /** The highest value the port number for the listener can be. */
        private static final int MAX_VALID_PORT = 65535; // 0xFF
        /** The local port number for the listener to be bound to. */
        private final int port;
        /** The socket configure operation. */
        private final Consumer<Socket> configOperation;
        /**
         * Creates a new instance for constructing a listener that will be bound
         * to the specified port and perform the given configure operation on
         * each accepted socket.
         *
         * @param listenPort                 the local port number
         * @param configureOperation         the socket configure operation
         * @throws IllegalArgumentException  if the port parameter is outside
         *                                   the specified range of valid port
         *                                   values, which is between 0 and
         *                                   65535, inclusive
         */
        public Builder(final int listenPort,
                final Consumer<Socket> configureOperation) {
            if (listenPort < 0 || listenPort > MAX_VALID_PORT) {
                throw new IllegalArgumentException();
            }
            port = listenPort;
            configOperation = Objects.requireNonNull(configureOperation);
        }
        @Override
        public StreamConnectionListener build(final Handler h)
                throws IOException {
            return new SocketConnectionListener(port, configOperation, h);
        }
    }
}
