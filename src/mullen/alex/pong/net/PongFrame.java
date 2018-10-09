package mullen.alex.pong.net;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import mullen.alex.jge.Vector2f;
import mullen.alex.pong.Paddle;

/**
 * Represents the standard frame of data to transmit between client and server.
 *
 * @author  Alex Mullen
 *
 */
public class PongFrame {
    /**
     * An enum to represent the types of frames there are.
     *
     * @author  Alex Mullen
     *
     */
    public enum Type {
        /** Represents an authorisation frame. */
        AUTHORISATION,
        /** Represents a snapshot frame. */
        SNAPSHOT,
        /** Represents an input frame. */
        INPUT,
        /** Represents an event frame. */
        EVENT,
        /** Represents a ping frame. */
        PING,
        /** Represents a ping reply frame. */
        PING_REPLY,
    }
    /** The snapshot field of the frame. */
    public GameSnapshot snapshot;
    /** The input field of the frame. */
    public Paddle.Input input;
    /** The role field of the frame. */
    public Role role;
    /** The field used for transferring a source position. */
    public Rectangle srcPosition;
    /** The field used for transferring a destination position. */
    public Rectangle destPosition;
    /** The field used for transferring a vector velocity. */
    public Ellipse2D.Float srcPositionFloat;
    public Ellipse2D.Float destPositionFloat;
    public Vector2f velocity;
    /** The player data map. */
    public Map<Role, PongPlayer> players;
    /** The players ping data map. */
    public Map<Role, Integer> pings;
    /** Holds the argument map. */
    public final Map<String, String> args;
    /** Holds the type of frame this is. */
    private final Type type;
    /**
     * Creates a new frame that is of the specified type.
     *
     * @param frameType  the type of frame
     */
    public PongFrame(final Type frameType) {
        type = Objects.requireNonNull(frameType);
        args = new HashMap<>();
    }
    /**
     * Gets the type of frame.
     *
     * @return  the type
     */
    public final Type getType() {
        return type;
    }
    @Override
    public final String toString() {
        return "PongFrame [snapshot=" + snapshot + ", input=" + input
                + ", players=" + players + ", pings=" + pings + ", args="
                + args + ", type=" + type + "]";
    }
}
