package mullen.alex.pong;

import java.awt.Rectangle;
import java.util.Objects;

import mullen.alex.jge.Vector2f;

/**
 * Represents a paddle.
 *
 * @author  Alex Mullen
 *
 */
public class Paddle {
    /** Holds the world dimensions this paddle is part of. */
    private final transient World2D world;
    /** Holds the transform for this paddle. */
    private final Rectangle transform;
    /** Holds the paddle's velocity vector. */
    private final Vector2f velocity;
    /** Holds the speed the paddle moves. */
    private int speed;
    /**
     * Creates a new instance that uses the specified world dimensions for
     * scaling and world edge collision detection.
     *
     * @param w2d  the world dimensions
     */
    public Paddle(final World2D w2d) {
        world = Objects.requireNonNull(w2d);
        transform = new Rectangle();
        velocity = new Vector2f();
    }
    /**
     * Creates a new paddle instance that is a deep copy of the given paddle.
     *
     * @param srcPaddle  the source paddle to copy
     */
    public Paddle(final Paddle srcPaddle) {
        world = srcPaddle.world;
        transform = new Rectangle(srcPaddle.transform);
        velocity = new Vector2f(srcPaddle.velocity);
        speed = srcPaddle.speed;
    }
    /**
     * Copies the field values from the specified paddle into this paddle
     * instance.
     *
     * @param srcPaddle  the source paddle
     */
    // TODO This is smelly.
    public void copyFrom(final Paddle srcPaddle) {
        transform.x = srcPaddle.getTransform().x;
        transform.y = srcPaddle.getTransform().y;
        velocity.x = srcPaddle.getTransform().x;
        velocity.y = srcPaddle.getTransform().y;
        speed = srcPaddle.getSpeed();
    }
    /**
     * Gets the world dimensions this paddle exists in.
     *
     * @return  the world dimensions
     */
    public final World2D getWorld() {
        return world;
    }
    /**
     * Gets the transform for this paddle.
     *
     * @return  the transform
     */
    public final Rectangle getTransform() {
        return transform;
    }
    /**
     * Gets the paddle's velocity.
     *
     * @return  the velocity vector
     */
    public final Vector2f getVelocity() {
        return velocity;
    }
    /**
     * Gets the paddle's speed.
     *
     * @return  the speed
     */
    public final int getSpeed() {
        return speed;
    }
    /**
     * Sets the paddle's speed.
     *
     * @param newSpeed  the new speed
     */
    public final void setSpeed(final int newSpeed) {
        speed = newSpeed;
    }
    /**
     * Moves the paddle in the direction of the specified input.
     *
     * @param paddleInput  the input
     */
    public final void move(final Input paddleInput) {
        if (paddleInput == Input.MOVE_DOWN) {
            velocity.y = 1.0f;
            transform.y += velocity.y * speed;
        } else if (paddleInput == Input.MOVE_UP) {
            velocity.y = -1.0f;
            transform.y += velocity.y * speed;
        } else {
            velocity.y = 0.0f;
        }
        // Clamp the paddle within the world bounds.
        if (transform.y < 0) {
            transform.y = 0;
        }
        if (transform.y + transform.height >= world.getHeight()) {
            transform.y = world.getHeight() - transform.height;
        }
    }
    /**
     * An enumeration to represent the valid inputs that can be applied to a
     * {@link Paddle}.
     *
     * @author  Alex Mullen
     *
     */
    public enum Input {
        /** No input. Paddle should not move.*/
        NONE,
        /** Move the paddle up. */
        MOVE_UP,
        /** Move the paddle down. */
        MOVE_DOWN
    }
}
