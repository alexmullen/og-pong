package mullen.alex.pong.net;

/**
 * Defines the interface for a connection listener that provides an endpoint for
 * connections to connect to.
 *
 * @author  Alex Mullen
 *
 */
public interface ConnectionListener {
    /**
     * Starts allowing connections.
     */
    void start();
    /**
     * Shuts down this listener so that no more connections can connect.
     */
    void shutdown();
}
