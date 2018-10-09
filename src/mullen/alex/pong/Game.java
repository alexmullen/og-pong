package mullen.alex.pong;

import java.util.Objects;
import java.util.Random;

/**
 * Represents the Pong game simulation model.
 *
 * @author  Alex Mullen
 * strictfp?
 */
public class Game {
    /** Holds the random number generator this uses. */
    private final Random rng;
    /** Holds the virtual world coordinate space. */
    private final World2D world;
    /** Holds the ball. */
    private final Ball ball;
    /** Holds the left paddle. */
    private final Paddle leftPaddle;
    /** Holds the right paddle. */
    private final Paddle rightPaddle;
    /** Holds the ball collision detector implementation used in this game. */
    private final BallCollisionDetector collisionDetector;
    /** Holds the ball collision resolver implementation used in this game. */
    private final BallCollisionResolver collisionResolver;
    /** Holds the game end checker implementation used in this game. */
    private final GameEndChecker endChecker;
    /** Holds the score for the left side. */
    private int leftSideScore;
    /** Holds the score for the right side. */
    private int rightSideScore;
    /**
     * A class for holding the required constructor arguments required for
     * instantiating the outer class.
     * <p>
     * Using an object like this helps improve readability and maintainability
     * when a constructor requires many arguments.
     *
     * @author  Alex Mullen
     *
     */
    public static class ConstructorArgs {
        /** The random number generator to use. */
        public Random rng;
        /** The world dimensions to scale everything to. */
        public World2D worldDimensions;
        /** The ball instance to use. */
        public Ball ball;
        /** The paddle instance to use for the left side player. */
        public Paddle leftPaddle;
        /** The paddle instance to use for the right side player. */
        public Paddle rightPaddle;
        /** The collision detector implementation to use. */
        public BallCollisionDetector collisionDetector;
        /** The collision resolver implementation to use. */
        public BallCollisionResolver collisionResolver;
        /** The game end checker implementation to use. */
        public GameEndChecker endChecker;
    }
    /**
     * Creates a new instance that uses the specified arguments.
     *
     * @param  args  the arguments
     *
     * @throws NullPointerException  if <code>args</code> or any of its fields
     *                               are <code>null</code>
     */
    public Game(final ConstructorArgs args) {
        Objects.requireNonNull(args);
        rng = Objects.requireNonNull(args.rng);
        world = Objects.requireNonNull(args.worldDimensions);
        ball = Objects.requireNonNull(args.ball);
        leftPaddle = Objects.requireNonNull(args.leftPaddle);
        rightPaddle = Objects.requireNonNull(args.rightPaddle);
        collisionDetector = Objects.requireNonNull(args.collisionDetector);
        collisionResolver = Objects.requireNonNull(args.collisionResolver);
        endChecker = Objects.requireNonNull(args.endChecker);
    }
    /**
     * Gets the random number generator this game uses.
     *
     * @return  the random number generator
     */
    public final Random getRng() {
        return rng;
    }
    /**
     * Gets the virtual world coordinate space this game uses.
     *
     * @return  the world
     */
    public final World2D getWorld() {
        return world;
    }
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
     * Gets the ball collision detector instance this game uses.
     *
     * @return  the ball collision detector instance
     */
    public final BallCollisionDetector getCollisionDetector() {
        return collisionDetector;
    }
    /**
     * Gets the ball collision resolver instance this game uses.
     *
     * @return  the ball collision resolver instance
     */
    public final BallCollisionResolver getCollisionResolver() {
        return collisionResolver;
    }
    /**
     * Gets the game end checker instance for this game.
     *
     * @return  the game end checker instance
     */
    public final GameEndChecker getEndChecker() {
        return endChecker;
    }
    /**
     * Gets the left side's score.
     *
     * @return  the score
     */
    public final int getLeftSideScore() {
        return leftSideScore;
    }
    /**
     * Gets the right side's score.
     *
     * @return  the score
     */
    public final int getRightSideScore() {
        return rightSideScore;
    }
    /**
     * Sets the left side's score.
     *
     * @param score  the new score value
     */
    public final void setLeftSideScore(final int score) {
        leftSideScore = score;
    }
    /**
     * Sets the right side's score.
     *
     * @param score  the new score value
     */
    public final void setRightSideScore(final int score) {
        rightSideScore = score;
    }
    /**
     * Defines an interface for a class that constructs {@link Game} instances.
     *
     * @author  Alex Mullen
     *
     */
    @FunctionalInterface
    public interface Builder {
        /**
         * Constructs and gets a newly created {@link Game} instance.
         *
         * @return  the new instance
         */
        Game createGame();
    }
}
