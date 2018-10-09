package mullen.alex.pong.net.server;

import java.util.ArrayDeque;
import java.util.Queue;

import mullen.alex.pong.Paddle;
import mullen.alex.pong.net.Role;

/**
 * Holds the bundle of data about a Pong client connection.
 *
 * @author  Alex Mullen
 *
 */
public class PongClientBundle {
    /** Whether the client is authorised or not. */
    public boolean authorised;
    /** Number of pings sent to the client. */
    public long pingsSent;
    /** Number of ping replies received. */
    public long pingRepliesReceived;
    /** The timestamp the last ping was sent at. */
    public long lastPingSentTime;
    /** The name of the client. */
    public String name;
    /** The current ping of the client. */
    public long ping;
    /** Holds the role assigned to this client. */
    public Role role;
    /** Holds received and pending inputs for this client. */
    public Queue<Paddle.Input> inputs;
    /**
     * Creates a new instance.
     */
    public PongClientBundle() {
        inputs = new ArrayDeque<>();
    }
}
