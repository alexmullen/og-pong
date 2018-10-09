package mullen.alex.pong.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Encapsulates a socket connection.
 *
 * @author  Alex Mullen
 *
 */
public class SocketConnection implements StreamConnection {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(SocketConnection.class.getName());
    /** The socket this is encapsulating. */
    private final Socket socket;
    /**
     * Creates a new socket connection that encapsulates the specified socket.
     * <p>
     * The socket must have been connected prior to it being passed into this.
     * If it was not then an <code>IllegalStateException</code> will be thrown.
     *
     * @param s                       the socket
     *
     * @throws IllegalStateException  if <code>s</code> is a socket that has
     *                                never been connected
     */
    public SocketConnection(final Socket s) {
        if (!s.isConnected()) { // This is also a null check first.
            throw new IllegalStateException("socket must be connected prior");
        }
        socket = s;
    }
    /**
     * Provides access to the underlying socket.
     *
     * @return  the underlying socket this wraps around
     */
    public final Socket getSocket() {
        return socket;
    }
    @Override
    public final void close() {
        try {
            socket.close();
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    @Override
    public final InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
    @Override
    public final OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
    @Override
    public final String toString() {
        return "SocketConnection [socket=" + socket + "]";
    }
    /**
     * Creates a <code>StreamConnection.Factory</code> instance for producing
     * socket connections to the specified host.
     *
     * @param hostname  the hostname created connections will connect to
     * @param port      the port created connections will connect on
     * @return          a new <code>StreamConnection.Factory</code> instance
     */
    public static Factory createFactory(final String hostname, final int port) {
//        return () -> new SocketConnection(new Socket(hostname, port));
        return new Factory() {
            @SuppressWarnings("resource")
            @Override
            public StreamConnection newInstance() throws IOException {
                return new SocketConnection(new Socket(hostname, port));
            }
        };
    }
}
