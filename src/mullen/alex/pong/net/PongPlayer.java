package mullen.alex.pong.net;

import java.util.Objects;

/**
 * A class to hold data about a player.
 *
 * @author  Alex Mullen
 *
 */
public class PongPlayer {
    /** Holds the name of the player. */
    private final String name;
    /** Holds the player's role. */
    private final Role role;
    /** Holds the current ping of the player. */
    private long ping;
    /**
     * Creates a new instance that is populated with the specified arguments.
     *
     * @param playerName   the name of the player
     * @param playerRole   the role of the player
     * @param initialPing  the initial ping value of the player
     */
    public PongPlayer(final String playerName, final Role playerRole,
            final long initialPing) {
        name = Objects.requireNonNull(playerName);
        role = Objects.requireNonNull(playerRole);
        ping = initialPing;
    }
    /**
     * Gets the player's name.
     *
     * @return  the name
     */
    public final String getName() {
        return name;
    }
    /**
     * Gets the player's role.
     *
     * @return  the role
     */
    public final Role getRole() {
        return role;
    }
    /**
     * Gets the player's current ping.
     *
     * @return  their current ping
     */
    public final long getPing() {
        return ping;
    }
    /**
     * Sets the player's current ping.
     *
     * @param value  the current ping value
     */
    public final void setPing(final long value) {
        ping = value;
    }
    @Override
    public final String toString() {
        return "PongPlayer [name=" + name + ", role=" + role + ", ping=" + ping
                + "]";
    }
}
