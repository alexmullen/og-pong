package mullen.alex.pong;

import java.awt.Point;
import java.util.Random;

import mullen.alex.pong.Game.Builder;
import mullen.alex.pong.Game.ConstructorArgs;

/**
 * A game builder implementation for constructing a "standard" 1v1 pong game
 * as most people know it.
 *
 * @author  Alex Mullen
 *
 */
public class StandardGameBuilder implements Builder {
    /**
     * Creates a new instance.
     */
    public StandardGameBuilder() {
        // Intentionally empty.
    }
    @Override
    public final Game createGame() {
        final Game.ConstructorArgs args = new ConstructorArgs();
        args.rng = new Random();
        args.worldDimensions = new World2D(1024, 768);
        args.collisionDetector = new AWTBallCollisionDetector();
        args.collisionResolver = new BasicBallCollisionResolver();
        args.endChecker = g -> false;   // TODO: Replace this with actual.
        args.ball = initBall(args.worldDimensions);
        args.leftPaddle = initLeftPaddle(args.worldDimensions);
        args.rightPaddle = initRightPaddle(args.worldDimensions);
        return new Game(args);
    }
    /**
     * Initialises the ball.
     *
     * @param w2d  the virtual world coordinate space
     * @return     the created ball instance
     */
    private static Ball initBall(final World2D w2d) {
        final float ballDiameter = w2d.getHeight() / 40.0f;
        final Ball newBall = new Ball(-2.5f, 2.0f, w2d.getHeight() / 400);
        newBall.getTransform().width = ballDiameter;
        newBall.getTransform().height = ballDiameter;
        final Point worldCentre = w2d.getCentre();
        newBall.getTransform().x = worldCentre.x - ballDiameter / 2;
        newBall.getTransform().y = worldCentre.y - ballDiameter / 2;
        return newBall;
    }
    /**
     * Initialises the left paddle.
     *
     * @param w2d  the virtual world coordinate space
     * @return     the created left paddle instance
     */
    private static Paddle initLeftPaddle(final World2D w2d) {
        final Paddle lPaddle = new Paddle(w2d);
        final int paddleWidth = w2d.getWidth() / 40;
        final int paddleHeight = w2d.getHeight() / 5;
        final int paddleLeftRightMargin = paddleWidth * 2;
        lPaddle.getTransform().width = paddleWidth;
        lPaddle.getTransform().height = paddleHeight;
        lPaddle.getTransform().x = paddleLeftRightMargin;
        lPaddle.getTransform().y = w2d.getHeight() / 2 - paddleHeight / 2;
        lPaddle.setSpeed(w2d.getHeight() / 50);
        return lPaddle;
    }
    /**
     * Initialises the right paddle.
     *
     * @param w2d  the virtual world coordinate space
     * @return     the created right paddle instance
     */
    private static Paddle initRightPaddle(final World2D w2d) {
        final Paddle rPaddle = new Paddle(w2d);
        final int paddleWidth = w2d.getWidth() / 40;
        final int paddleHeight = w2d.getHeight() / 5;
        final int paddleLeftRightMargin = paddleWidth * 2;
        rPaddle.getTransform().width = paddleWidth;
        rPaddle.getTransform().height = paddleHeight;
        rPaddle.getTransform().x =
                w2d.getWidth() - paddleWidth - paddleLeftRightMargin;
        rPaddle.getTransform().y = w2d.getHeight() / 2 - paddleHeight / 2;
        rPaddle.setSpeed(w2d.getHeight() / 50);
        return rPaddle;
    }
}
