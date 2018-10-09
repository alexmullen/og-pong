package mullen.alex.pong;

import java.awt.geom.Ellipse2D;
import mullen.alex.pong.BallCollisionDetector.WorldCollision.Edge;

/**
 * Represents a {@link BallCollisionDetector} implementation that makes use of
 * the hit-testing and intersection testing methods available in the awt shape
 * library.
 *
 * @author  Alex Mullen
 *
 */
public class AWTBallCollisionDetector implements BallCollisionDetector {
    /**
     * Creates a new instance.
     */
    public AWTBallCollisionDetector() {
        // Intentionally empty.
    }
    @Override
    public final Collision check(final Game context) {
        final Ball ball = context.getBall();
        final World2D world = context.getWorld();
        final Ellipse2D.Float ballTransform = ball.getTransform();
        ////////////////////////////////////////////////////////////////////////
        // Check for world collisions.
        if (ballTransform.y < 0) {
            // Collided with top of the world.
            return new WorldCollision(ball, world, Edge.TOP);
        } else if (ballTransform.y + ballTransform.height
                > world.getHeight()) {
            // Collided with bottom of the world.
            return new WorldCollision(ball, world, Edge.BOTTOM);
        } else if (ballTransform.x <= 0) {
            // Collided with left side of the world.
            return new WorldCollision(ball, world, Edge.LEFT);
        } else if (ballTransform.x + ballTransform.width >= world.getWidth()) {
            // Collided with right side of the world.
            return new WorldCollision(ball, world, Edge.RIGHT);
        }
        ////////////////////////////////////////////////////////////////////////
        // Check for paddle collisions.
        final Paddle leftPaddle = context.getLeftPaddle();
        if (ballTransform.intersects(leftPaddle.getTransform())) {
            return new PaddleCollision(ball, leftPaddle);
        }
        final Paddle rightPaddle = context.getRightPaddle();
        if (ballTransform.intersects(rightPaddle.getTransform())) {
            return new PaddleCollision(ball, rightPaddle);
        }
        // No collision detected.
        return null;
    }
}
