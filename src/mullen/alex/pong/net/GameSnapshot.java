package mullen.alex.pong.net;

import mullen.alex.pong.Ball;
import mullen.alex.pong.Game;
import mullen.alex.pong.Paddle;

/**
 * Represents the state of a game at a given tick.
 *
 * @author  Alex Mullen
 *
 */
public class GameSnapshot {
//    /** The current tick. */
//    private final long tick;
    /** The ball transform. */
    private final Ball ball;
    /** The left paddle transform. */
    private final Paddle leftPaddle;
    /** The right paddle transform. */
    private final Paddle rightPaddle;
    /** Holds the score for the left side. */
    private int leftSideScore;
    /** Holds the score for the right side. */
    private int rightSideScore;
    /**
     * Creates a new snapshot of the specified game at this point in time.
     *
     * @param game  the game
     */
    public GameSnapshot(final Game game) {
        ball = new Ball(game.getBall());
        leftPaddle = new Paddle(game.getLeftPaddle());
        rightPaddle = new Paddle(game.getRightPaddle());
//        tick = game.getTick();
        leftSideScore = game.getLeftSideScore();
        rightSideScore = game.getRightSideScore();
    }
//    /**
//     * Gets how many ticks have been performed.
//     *
//     * @return  the number of ticks
//     */
//    public final long getTick() {
//        return tick;
//    }
    /**
     * Gets the ball.
     *
     * @return  the ball
     */
    public final Ball getBall() {
        return ball;
    }
    /**
     * Gets the left paddle.
     *
     * @return  the left paddle
     */
    public final Paddle getLeftPaddle() {
        return leftPaddle;
    }
    /**
     * Gets the right paddle.
     *
     * @return  the right paddle
     */
    public final Paddle getRightPaddle() {
        return rightPaddle;
    }
    /**
     * Gets the left paddle's score.
     *
     * @return  the score
     */
    public final int getLeftSideScore() {
        return leftSideScore;
    }
    /**
     * Gets the right paddle's score.
     *
     * @return  the score
     */
    public final int getRightSideScore() {
        return rightSideScore;
    }
    @Override
    public final String toString() {
        return "GameSnapshot [ball=" + ball
                + ", leftPaddle=" + leftPaddle + ", rightPaddle=" + rightPaddle
                + ", leftSideScore=" + leftSideScore
                + ", rightSideScore=" + rightSideScore + "]";
    }
}
