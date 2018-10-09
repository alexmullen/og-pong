package mullen.alex.pong;

import java.awt.geom.Ellipse2D;

import mullen.alex.jge.Vector2f;

/**
 * Represents a ball.
 *
 * @author  Alex Mullen
 *
 */
public class Ball {
    /** Holds the transform for this ball. */
    private final Ellipse2D.Float transform;
    /** Holds the ball's velocity vector. */
    private final Vector2f velocity;
    /** Holds the speed of the ball. */
    private int speed;
    /**
     * Creates a deep copy of the specified ball.
     *
     * @param srcBall  the ball to copy
     */
    public Ball(final Ball srcBall) {
        final Ellipse2D.Float srcTransform = srcBall.transform;
        transform  = new Ellipse2D.Float(srcTransform.x,
                srcTransform.y, srcTransform.width, srcTransform.height);
        velocity = new Vector2f(srcBall.velocity);
        speed = srcBall.speed;
    }
    /**
     * Creates a new instance that has the initial X and Y velocities and speed.
     *
     * @param initialVelocityX  the initial X axis velocity
     * @param initialVelocityY  the initial Y axis velocity
     * @param initialSpeed      the initial speed
     */
    public Ball(final float initialVelocityX, final float initialVelocityY,
            final int initialSpeed) {
        transform = new Ellipse2D.Float();
        velocity = new Vector2f(initialVelocityX, initialVelocityY);
        speed = initialSpeed;
    }
    /**
     * Gets the transform for this ball.
     *
     * @return  the transform
     */
    public final Ellipse2D.Float getTransform() {
        return transform;
    }
    /**
     * Gets the ball's velocity.
     *
     * @return  the velocity vector
     */
    public final Vector2f getVelocity() {
        return velocity;
    }
    /**
     * Gets the ball's speed.
     *
     * @return  the speed
     */
    public final int getSpeed() {
        return speed;
    }
    /**
     * Sets the ball's speed.
     *
     * @param newSpeed  the new speed
     */
    public final void setSpeed(final int newSpeed) {
        speed = newSpeed;
    }
    /**
     * Moves the ball one step.
     */
    public final void move() {
        final Vector2f normVelocity = velocity.normalize();
        transform.x += normVelocity.x * speed;
        transform.y += normVelocity.y * speed;
    }
    public final void move(final double scale) {
        final Vector2f normVelocity = velocity.normalize();
        transform.x += normVelocity.x * speed * scale;
        transform.y += normVelocity.y * speed * scale; 
    }
}
