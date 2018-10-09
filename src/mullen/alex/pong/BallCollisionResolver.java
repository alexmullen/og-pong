package mullen.alex.pong;

import mullen.alex.pong.BallCollisionDetector.Collision;

/**
 * Defines an interface for a class that resolves collisions with a
 * {@link Ball}.
 *
 * @author  Alex Mullen
 *
 */
@FunctionalInterface
public interface BallCollisionResolver {
    /**
     * Resolves the given collision.
     *
     * @param collision  the collision
     *
     * @throws  NullPointerException  if <code>collision</code> is
     *                                <code>null</code>
     */
    void resolve(Collision collision);
}
