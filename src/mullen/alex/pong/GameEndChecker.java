package mullen.alex.pong;

/**
 * Defines an interface for a class that checks whether a game has ended.
 *
 * @author  Alex Mullen
 *
 */
@FunctionalInterface
public interface GameEndChecker {
    /**
     * Gets whether the game has ended or not.
     *
     * @param game  the game to check
     *
     * @return  <code>true</code> if it has ended, <code>false</code> if not
     */
    boolean hasEnded(final Game game);
}
