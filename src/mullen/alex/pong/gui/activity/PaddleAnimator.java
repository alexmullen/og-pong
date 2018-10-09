package mullen.alex.pong.gui.activity;

import mullen.alex.pong.Paddle;
import mullen.alex.pong.Paddle.Input;

public class PaddleAnimator {
    private final Paddle paddle;
    private int srcY;
    private int destY;
    private float yvolMultipliedBySpeed;
    public PaddleAnimator(final Paddle p) {
        paddle = p;
    }
    public void start(final Input input) {
        configureVelocityForInput(input);
        srcY = paddle.getTransform().y;
        destY = (int) (paddle.getTransform().y
                + (paddle.getVelocity().y * paddle.getSpeed()));
        yvolMultipliedBySpeed = paddle.getVelocity().y * paddle.getSpeed();
        paddle.getTransform().y = srcY;
    }
    public void animate(final double delta) {
        paddle.getTransform().y = (int) (srcY + (yvolMultipliedBySpeed * delta));
        paddle.getTransform().y = Math.max(0, paddle.getTransform().y);
        paddle.getTransform().y = Math.min(
                paddle.getWorld().getHeight() - paddle.getTransform().height,
                paddle.getTransform().y);
    }
    public void finish() {
        paddle.getTransform().y = Math.max(0, destY);
        paddle.getTransform().y = Math.min(
                paddle.getWorld().getHeight() - paddle.getTransform().height,
                paddle.getTransform().y);
        paddle.getVelocity().y = 0.0f;
    }
    private void configureVelocityForInput(final Input input) {
        if (input == Input.MOVE_DOWN) {
            paddle.getVelocity().y = 1.0f;
        } else if (input == Input.MOVE_UP) {
            paddle.getVelocity().y = -1.0f;
        } else {
            paddle.getVelocity().y = 0.0f;
        }
    }
}
