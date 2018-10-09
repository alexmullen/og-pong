package mullen.alex.pong.net;

/**
 * Represents an abstract generic connection between two entities.
 *
 * @author  Alex Mullen
 *
 */
@FunctionalInterface
public interface Connection {
    /**
     * Closes this connection and releases any system resources used.
     */
    void close();
}
