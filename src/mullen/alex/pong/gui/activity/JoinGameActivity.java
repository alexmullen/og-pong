package mullen.alex.pong.gui.activity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

import mullen.alex.jge.TickSchedulerService.Task;
import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.activity.SlideAnimationActivity.SlideDirection;
import mullen.alex.pong.gui.components.Button;
import mullen.alex.pong.gui.components.Label;
import mullen.alex.pong.gui.components.Label.HorizontalAlignment;
import mullen.alex.pong.gui.components.TextBox;

/**
 * Represents the activity to display when the "Join game" label is chosen
 * on the Multiplayer menu activity.
 *
 * @author  Alex Mullen
 *
 */
public class JoinGameActivity implements PongActivity {
    /** Holds the label labelled "Player name". */
    private final Label playerNameLabel;
    /** Holds the label labelled "Hostname". */
    private final Label hostnameLabel;
    /** Holds the text box to enter the host name in to. */
    private final TextBox hostTextBox;
    /** Holds the text box to enter the player name in to. */
    private final TextBox playerNameTextBox;
    /** Holds the join button. */
    private final Button joinButton;
    /** Holds the previous activity. */
    private final PongActivity previousActivity;
    /** Holds the task for animating the textbox caret. */
    private Task caretTask;
    /** Holds a reference to the engine. */
    private PongEngine engine;
    /**
     * Creates a new instance that scales the menu and games to the specified
     * resolution. A back activity needs to be specified so that the previous
     * activity can be returned to.
     *
     * @param resolution    the resolution
     * @param backActivity  the previous activity
     */
    public JoinGameActivity(final World2D resolution,
            final PongActivity backActivity) {
        previousActivity = Objects.requireNonNull(backActivity);
        hostnameLabel = new Label("Hostname  ");
        hostnameLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        hostnameLabel.setTextColour(Color.WHITE);
        playerNameLabel = new Label("Player name  ");
        playerNameLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        playerNameLabel.setTextColour(Color.WHITE);
        hostTextBox = new TextBox();
        hostTextBox.setText("localhost");
        playerNameTextBox = new TextBox();
        playerNameTextBox.setText(System.getProperty("user.name", ""));
        joinButton = new Button("Join");
    }
    @Override
    public final void onActivityStarted(final Context context) {
        engine = context.getEngine();
        engine.getKeyboardService().registerKeyTypedListener(hostTextBox::applyKeyType);
        engine.getKeyboardService().registerKeyTypedListener(
                playerNameTextBox::applyKeyType);
        engine.getKeyboardService().registerKeyPressedListener(
                playerNameTextBox::handleKeyPress);
        engine.getKeyboardService().registerKeyPressedListener(
                hostTextBox::handleKeyPress);
        caretTask = engine.getSchedulerService().scheduleRepeatedly(() -> {
            hostTextBox.setCaretVisible(!hostTextBox.getCaretVisible());
            playerNameTextBox.setCaretVisible(
                    !playerNameTextBox.getCaretVisible());
        }, 0, 30);
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        engine.getKeyboardService().unregisterKeyTypedListener(hostTextBox::applyKeyType);
        engine.getKeyboardService().unregisterKeyTypedListener(playerNameTextBox::applyKeyType);
        engine.getKeyboardService().unregisterKeyPressedListener(playerNameTextBox::handleKeyPress);
        engine.getKeyboardService().unregisterKeyPressedListener(hostTextBox::handleKeyPress);
        caretTask.cancel();
    }
    @Override
    public final void update() {
        final Point mousePosition = engine.getMouseService().getPosition();
        final boolean buttonPressed =
                engine.getMouseService().isButtonPressed(MouseEvent.BUTTON1);
        hostTextBox.handleMouse(mousePosition, buttonPressed);
        playerNameTextBox.handleMouse(mousePosition, buttonPressed);
        if (engine.getKeyboardService().isPressed(KeyEvent.VK_ESCAPE)) {
            engine.getActivityService().startActivity(new SlideAnimationActivity(
                    SlideDirection.DOWN, this, previousActivity));
        }
        if (joinButton.getBounds().contains(mousePosition)) {
            joinButton.getLabel().setBackgroundColour(Color.GREEN);
            if (buttonPressed) {
                engine.getActivityService().startActivity(
                        new ConnectingActivity(engine, hostTextBox.getText(),
                                playerNameTextBox.getText()));
            } else {
                joinButton.getLabel().setBackgroundColour(Color.GRAY);
            }
        }
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        // Clear the background.
        g.clearRect(0, 0, size.width, size.height);
        final int labelWidth = size.width / 5;
        final int labelHeight = size.height / 15;
        final int textBoxWidth = labelWidth * 3;
        final int textBoxHeight = labelHeight;
        // Position the "Hostname" label.
        hostnameLabel.getBounds().width = labelWidth;
        hostnameLabel.getBounds().height = labelHeight;
        hostnameLabel.getBounds().x =
                (size.width - labelWidth - textBoxWidth) / 2;
        hostnameLabel.getBounds().y = size.height / 2 - (labelHeight / 2);
        // Position the "Player name" label.
        playerNameLabel.getBounds().width = labelWidth + 50;
        playerNameLabel.getBounds().height = labelHeight;
        playerNameLabel.getBounds().x = hostnameLabel.getBounds().x - 50;
        playerNameLabel.getBounds().y =
                hostnameLabel.getBounds().y - labelHeight * 2;
        // Position the player name text box.]
        playerNameTextBox.getBounds().width =
                hostnameLabel.getBounds().x + labelWidth;
        playerNameTextBox.getBounds().height = labelHeight;
        playerNameTextBox.getBounds().x = playerNameLabel.getBounds().x
                + playerNameLabel.getBounds().width;
        playerNameTextBox.getBounds().y = playerNameLabel.getBounds().y;
        // Position the host name text box.
        hostTextBox.getBounds().width = textBoxWidth;
        hostTextBox.getBounds().height = textBoxHeight;
        hostTextBox.getBounds().x = hostnameLabel.getBounds().x + labelWidth;
        hostTextBox.getBounds().y = hostnameLabel.getBounds().y;
        // Position the "Join" button.
        joinButton.getBounds().width = textBoxWidth / 3;
        joinButton.getBounds().height = labelHeight;
        joinButton.getBounds().x =
                hostTextBox.getBounds().x + textBoxWidth
                    - joinButton.getBounds().width;
        joinButton.getBounds().y = hostnameLabel.getBounds().y
                + textBoxHeight + (textBoxHeight / 2);
        // Render them.
        hostnameLabel.render(g);
        hostTextBox.render(g);
        playerNameTextBox.render(g);
        playerNameLabel.render(g);
        joinButton.render(g);
    }
}
