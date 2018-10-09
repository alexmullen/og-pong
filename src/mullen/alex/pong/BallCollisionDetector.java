package mullen.alex.pong;

import java.util.Objects;

/**
 * Defines an interface for a class that detects when a {@link Ball} collides
 * with the bounds of the world or with a {@link Paddle}.
 *
 * @author  Alex Mullen
 *
 */
@FunctionalInterface
public interface BallCollisionDetector {
    /**
     * Checks for collisions for the ball with either the paddles or world.
     *
     * @param context  the game context to check for collisions on
     *
     * @return  a {@link Collision} instance representing the result,
     *          <code>null</code> if there is no collision
     */
    Collision check(Game context);
    /**
     * A class for holding and describing the result of a collision check.
     *
     * @author  Alex Mullen
     *
     */
    abstract class Collision {
        /** Represents what the ball collided with. */
        private final With with;
        /** The ball involved in the collision. */
        private final Ball ball;
        /**
         * An enumeration to represent the different things a ball can collide
         * with.
         *
         * @author  Alex Mullen
         *
         */
        public enum With {
            /** Ball collided with the world bounds. */
            WORLD,
            /** Ball collided with a paddle. */
            PADDLE
        }
        /**
         * Creates a new instance.
         *
         * @param b  the ball involved in the collision
         * @param w  what the ball collided with
         *
         * @throws   NullPointerException  if <code>b</code> or <code>w</code>
         *                                 is <code>null</code>
         */
        Collision(final Ball b, final With w) {
            ball = Objects.requireNonNull(b);
            with = Objects.requireNonNull(w);
        }
        /**
         * Gets the ball involved in the collision.
         *
         * @return  the ball
         */
        public final Ball getBall() {
            return ball;
        }
        /**
         * Gets what the ball collided with.
         *
         * @return  what the ball collided with
         */
        public final With getWith() {
            return with;
        }
    }
    /**
     * An extension to {@link Collision} that describes a collision with a
     * paddle.
     *
     * @author  Alex Mullen
     *
     */
    class PaddleCollision extends Collision {
        /** The paddle that the ball collided with. */
        private final Paddle paddle;
        /**
         * Creates a new instance.
         *
         * @param b  the ball involved in the collision
         * @param p  the paddle that the ball collided with
         *
         * @throws   NullPointerException  if <code>b</code> or <code>p</code>
         *                                 is <code>null</code>
         */
        PaddleCollision(final Ball b, final Paddle p) {
            super(b, With.PADDLE);
            paddle = Objects.requireNonNull(p);
        }
        /**
         * Gets the paddle that the ball collided with.
         *
         * @return  the paddle
         */
        public final Paddle getPaddle() {
            return paddle;
        }
    }
    /**
     * An extension to {@link Collision} that describes a collision with the
     * world borders.
     *
     * @author  Alex Mullen
     *
     */
    class WorldCollision extends Collision {
        /** The world border dimensions. */
        private final World2D world;
        /** What edge of the world did the collision occur at. */
        private final Edge edge;
        /**
         * An enum to represent the different edges of the world the ball can
         * collide with.
         *
         * @author  Alex Mullen
         *
         */
        public enum Edge {
            /** The top edge of the world. */
            TOP,
            /** The right edge of the world. */
            RIGHT,
            /** The bottom edge of the world. */
            BOTTOM,
            /** The left edge of the world. */
            LEFT
        }
        /**
         * Creates a new instance.
         *
         * @param ball  the ball involved in the collision
         * @param w     the world dimensions that the ball collided with
         * @param e     what edge of the world did the ball collide at
         *
         * @throws   NullPointerException  if <code>ball</code>,
         *                                 <code>w</code> or <code>e</code> is
         *                                 <code>null</code>
         */
        WorldCollision(final Ball ball, final World2D w, final Edge e) {
            super(ball, With.WORLD);
            world = Objects.requireNonNull(w);
            edge = Objects.requireNonNull(e);
        }
        /**
         * Gets the dimensions of the world that the ball collided with.
         *
         * @return  the world dimensions
         */
        public final World2D getWorldDimensions() {
            return world;
        }
        /**
         * Gets which edge of the world the ball collided with.
         *
         * @return  the world edge
         */
        public final Edge getEdge() {
            return edge;
        }
    }
}
