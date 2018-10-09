package mullen.alex.pong.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Map;
import java.util.Objects;

import mullen.alex.pong.Game;
import mullen.alex.pong.Paddle;
import mullen.alex.pong.World2D;
import mullen.alex.pong.gui.components.Label;
import mullen.alex.pong.gui.components.Label.HorizontalAlignment;
import mullen.alex.pong.net.PongPlayer;
import mullen.alex.pong.net.Role;

/**
 * A renderer implementation for rendering a Pong game.
 *
 * @author  Alex Mullen
 *
 */
public class GameRenderer {
    /** Holds the dash pattern for the mid line. */
    private static final float[] MIDLINE_DASH_PATTERN = new float[] {10.0f};
    /** Holds how thick the mid line is relative to the paddle width. */
    private static final float MIDLINE_WIDTH_TO_PADDLE_WIDTH_RATIO = 4.0f;
    /** Holds the font for drawing the scores. */
    private static final Font SCORE_FONT = new Font("Arial", Font.PLAIN, 50);
    /** Holds the game context. */
    private final Game gameContext;
    /** Holds the stroke to use for drawing the mid line. */
    private final Stroke midLineStroke;
    /**
     * Creates a new instance that will render the specified game context.
     *
     * @param game  the game context
     */
    public GameRenderer(final Game game) {
        gameContext = Objects.requireNonNull(game);
        midLineStroke = new BasicStroke(
                gameContext.getLeftPaddle().getTransform().width
                    / MIDLINE_WIDTH_TO_PADDLE_WIDTH_RATIO,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                1.0f,
                MIDLINE_DASH_PATTERN,
                0.0f);
    }
    /**
     * Renders a frame.
     *
     * @param g     the graphics context
     * @param size  the size of the canvas
     */
    public final void render(final Graphics2D g, final Dimension size) {
        // Clear the background.
        g.clearRect(0, 0, size.width, size.height);
        // Perform any transformations required.
        performTransformations(g, size);
        // Paint the world area.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, gameContext.getWorld().getWidth(),
                gameContext.getWorld().getHeight());
        g.setColor(Color.WHITE);
        // Draw the dashed mid line.
        drawMidLine(g);
        // Render the ball and paddles.
        drawPaddle(g, gameContext.getLeftPaddle());
        drawPaddle(g, gameContext.getRightPaddle());
        drawBall(g);
        // Draw the scores.
        drawScores(g);
    }
    /**
     * Draws the ball.
     *
     * @param g  the graphics context
     */
    private void drawBall(final Graphics2D g) {
        g.fill(gameContext.getBall().getTransform());
    }
    /**
     * Draws the specified paddle.
     *
     * @param g       the graphics context
     * @param paddle  the paddle to draw
     */
    private static void drawPaddle(final Graphics2D g, final Paddle paddle) {
        g.fill(paddle.getTransform());
    }
    /**
     * Performs transformation operations from world space to view space.
     * World space aspect ratio is maintained and centred to the viewport.
     *
     * @param g     the graphics context to perform the transformations on
     * @param size  the size of the graphics context
     */
    private void performTransformations(final Graphics2D g,
            final Dimension size) {
        final World2D world = gameContext.getWorld();
        /*
         * Calculate the scale multipliers required to fit the world onto the
         * viewport space we have.
         */
        final double widthScale = size.getWidth() / world.getWidth();
        final double heightScale = size.getHeight() / world.getHeight();
        // Choose the minimum of the scale multipliers.
        final double minScale = Math.min(widthScale, heightScale);
        // Calculate the scaled dimensions in view space.
        final int scaledViewWidth = (int) (world.getWidth() * minScale);
        final int scaledViewHeight = (int) (world.getHeight() * minScale);
        /*
         * Calculate how much space will remain on each axis after scaling so
         * that we can centre the frame by translating by half the remaining
         * space on each axis.
         */
        final int widthInset = (size.width - scaledViewWidth) / 2;
        final int heightInset = (size.height - scaledViewHeight) / 2;
        g.translate(widthInset, heightInset);
        g.scale(minScale, minScale);
    }
    /**
     * Renders the scores to the specified graphics context.
     *
     * @param g  the graphics context
     */
    private void drawScores(final Graphics2D g) {
        // TODO: Fix the font offset with different world sizes.
        g.setFont(SCORE_FONT);
        // Left score.
        final String leftScoreString =
                String.valueOf(gameContext.getLeftSideScore());
        final int leftX = (gameContext.getWorld().getWidth() / 2)
                - (g.getFontMetrics().stringWidth(leftScoreString)) - 25;
        g.drawString(leftScoreString, leftX,
                gameContext.getWorld().getHeight() / 15);
        // Right score.
        final String rightScoreString =
                String.valueOf(gameContext.getRightSideScore());
        final int rightX = (gameContext.getWorld().getWidth() / 2) + 25;
        g.drawString(rightScoreString, rightX,
                gameContext.getWorld().getHeight() / 15);
    }
    /**
     * Renders the division line to the specified graphics context.
     *
     * @param g  the graphics context
     */
    private void drawMidLine(final Graphics2D g) {
        g.setStroke(midLineStroke);
        final World2D world = gameContext.getWorld();
        final int halfWorldWidth = world.getWidth() / 2;
        g.drawLine(halfWorldWidth,
                   0,
                   halfWorldWidth,
                   world.getHeight());
    }
    public void renderPaddleInfo(final Graphics2D g,
            final Map<Role, PongPlayer> players) {
        // Left paddle tooltip.
        final Label leftNameLabel =
                new Label(players.get(Role.LEFT_PADDLE).getName());
        leftNameLabel.setTextColour(Color.GRAY);
        leftNameLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
        leftNameLabel.setOpaque(false);
        leftNameLabel.getBounds().x = gameContext.getLeftPaddle().getTransform().x + gameContext.getLeftPaddle().getTransform().width + 10;
        leftNameLabel.getBounds().y = gameContext.getLeftPaddle().getTransform().y + 10;
        leftNameLabel.getBounds().width = 200;
        leftNameLabel.getBounds().height = 50;
        leftNameLabel.render(g);
        final Label leftPingLabel =
                new Label("Ping: " + players.get(Role.LEFT_PADDLE).getPing()
                        + "ms");
        leftPingLabel.setTextColour(Color.GRAY);
        leftPingLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
        leftPingLabel.setOpaque(false);
        leftPingLabel.getBounds().x = gameContext.getLeftPaddle().getTransform().x + gameContext.getLeftPaddle().getTransform().width + 10;
        leftPingLabel.getBounds().y = leftNameLabel.getBounds().y + leftNameLabel.getBounds().height;
        leftPingLabel.getBounds().width = 200;
        leftPingLabel.getBounds().height = 30;
        leftPingLabel.render(g);
        // Right paddle tooltip.
        final Label rightNameLabel =
                new Label(players.get(Role.RIGHT_PADDLE).getName());
        rightNameLabel.setTextColour(Color.GRAY);
        rightNameLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        rightNameLabel.setOpaque(false);
        rightNameLabel.getBounds().x = gameContext.getRightPaddle().getTransform().x - 200 - 10;
        rightNameLabel.getBounds().y = gameContext.getRightPaddle().getTransform().y + 10;
        rightNameLabel.getBounds().width = 200;
        rightNameLabel.getBounds().height = 50;
        rightNameLabel.render(g);
        final Label rightPingLabel =
                new Label("Ping: " + players.get(Role.RIGHT_PADDLE).getPing()
                        + "ms");
        rightPingLabel.setTextColour(Color.GRAY);
        rightPingLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        rightPingLabel.setOpaque(false);
        rightPingLabel.getBounds().x = rightNameLabel.getBounds().x;
        rightPingLabel.getBounds().y = rightNameLabel.getBounds().y + rightNameLabel.getBounds().height;
        rightPingLabel.getBounds().width = 200;
        rightPingLabel.getBounds().height = 30;
        rightPingLabel.render(g);
    }
}
