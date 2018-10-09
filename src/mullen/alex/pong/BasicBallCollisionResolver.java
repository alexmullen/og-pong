package mullen.alex.pong;

import java.awt.Rectangle;

import mullen.alex.jge.Vector2f;
import mullen.alex.pong.BallCollisionDetector.Collision;
import mullen.alex.pong.BallCollisionDetector.PaddleCollision;
import mullen.alex.pong.BallCollisionDetector.WorldCollision;

/**
 * A basic ball collision resolver.
 *
 * @author  Alex Mullen
 *
 */
public class BasicBallCollisionResolver implements BallCollisionResolver {
    /** Holds the segment reflection angles (in degrees) for the paddles. */
    private static final int[] PADDLE_DEFLECTION_ANGLES =
        {45, 25, 15, 360, 345, 335, 315};
    /**
     * Creates a new instance.
     */
    public BasicBallCollisionResolver() {
        // Intentionally empty.
    }
    @Override
    public final void resolve(final Collision collision) {
        switch (collision.getWith()) {
            case WORLD:
                handleWorldCollision((WorldCollision) collision);
                break;
            case PADDLE:
                handlePaddleCollision((PaddleCollision) collision);
                break;
            default:
                throw new IllegalStateException("Unhandled case: "
                        + collision.getWith());
        }
    }
    /**
     * Handles a world collision.
     *
     * @param wc  the world collision
     */
    private static void handleWorldCollision(final WorldCollision wc) {
        final Ball ball = wc.getBall();
        final World2D world = wc.getWorldDimensions();
        switch (wc.getEdge()) {
            case TOP:
                ball.getTransform().y = 0;
                ball.getVelocity().y = -ball.getVelocity().y;
                break;
            case BOTTOM:
                ball.getTransform().y =
                    world.getHeight() - ball.getTransform().height;
                ball.getVelocity().y = -ball.getVelocity().y;
                break;
            case LEFT:
            case RIGHT:
                // Do not need to resolve these.
                break;
            default:
                throw new IllegalStateException("Unhandled case: "
                        + wc.getEdge());
        }
    }
    /**
     * Handles a paddle collision.
     *
     * @param pc  the paddle collision
     */
    private static void handlePaddleCollision(final PaddleCollision pc) {
        final Ball ball = pc.getBall();
        final Paddle paddle = pc.getPaddle();
        if (ball.getVelocity().x > 0) {
            ball.getTransform().x = paddle.getTransform().x - ball.getTransform().width;
            // Ball was heading right.
            // Convert ball coordinates into "paddle space" coordinates.
            final Rectangle rightPaddleTransform = paddle.getTransform();
            final Vector2f ballPaddleSpaceCoords =
                    new Vector2f(ball.getTransform().x - rightPaddleTransform.x,
                                 ball.getTransform().y
                                 // Use the middle height of the ball as centre.
                                 + (ball.getTransform().height / 2)
                                 - rightPaddleTransform.y);
            final int segmentSpaceing =
                    rightPaddleTransform.height
                            / PADDLE_DEFLECTION_ANGLES.length;
            int reflectionAngleIndex =
                    (int) (ballPaddleSpaceCoords.y / segmentSpaceing);
            /*
             * Constrain the index within valid bounds. Invalid bounds can occur
             * when the ball hits the paddle at the lower or upper bound due to
             * integer division.
             */
            reflectionAngleIndex = Math.max(0, reflectionAngleIndex);
            reflectionAngleIndex = Math.min(
                    PADDLE_DEFLECTION_ANGLES.length - 1,
                    reflectionAngleIndex);
            final int reflectionAngle =
                    PADDLE_DEFLECTION_ANGLES[reflectionAngleIndex];
            // Reflect the ball at the segment's specified angle.
            ball.getVelocity().x =
                    -(float) Math.cos(Math.toRadians(reflectionAngle));
            // Negate since the Y axis increases downwards.
            ball.getVelocity().y =
                    -(float) Math.sin(Math.toRadians(reflectionAngle));
        } else if (ball.getVelocity().x < 0) {
            ball.getTransform().x = paddle.getTransform().x + paddle.getTransform().width;
            // Ball was heading left.
            // Convert ball coordinates into "paddle space" coordinates.
            final Rectangle leftPaddleTransform = paddle.getTransform();
            final Vector2f ballPaddleSpaceCoords =
                    new Vector2f(ball.getTransform().x - leftPaddleTransform.x,
                                 ball.getTransform().y
                                 // Use the middle height of the ball as centre.
                                 + (ball.getTransform().height / 2)
                                 - leftPaddleTransform.y);
            final int segmentSpaceing =
                    leftPaddleTransform.height
                            / PADDLE_DEFLECTION_ANGLES.length;
            int reflectionAngleIndex =
                    (int) (ballPaddleSpaceCoords.y / segmentSpaceing);
            /*
             * Constrain the index within valid bounds. Invalid bounds can occur
             * when the ball hits the paddle at the lower or upper bound due to
             * integer division.
             */
            reflectionAngleIndex = Math.max(0, reflectionAngleIndex);
            reflectionAngleIndex = Math.min(
                    PADDLE_DEFLECTION_ANGLES.length - 1,
                    reflectionAngleIndex);
            final int reflectionAngle =
                    PADDLE_DEFLECTION_ANGLES[reflectionAngleIndex];
            // Reflect the ball at the segment's specified angle.
            ball.getVelocity().x =
                    (float) Math.cos(Math.toRadians(reflectionAngle));
            // Negate since the Y axis increases downwards.
            ball.getVelocity().y =
                    -(float) Math.sin(Math.toRadians(reflectionAngle));
        }
    }
}
