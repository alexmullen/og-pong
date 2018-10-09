package mullen.alex.pong.gui.activity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

import mullen.alex.jge.TickSchedulerService.Task;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.activity.SlideAnimationActivity.SlideDirection;
import mullen.alex.pong.gui.components.Button;
import mullen.alex.pong.gui.components.Label;
import mullen.alex.pong.gui.components.TextBox;

/**
 * Represents the activity to display when the "Create game" label is chosen
 * on the Multiplayer menu activity.
 *
 * @author  Alex Mullen
 *
 */
public class CreateGameActivity implements PongActivity {
    /** The label labelled "Name". */
    private final Label nameLabel;
    /** The text box for entering the player name. */
    private final TextBox nameTextBox;
    /** The button labelled "Create" for creating the game. */
    private final Button createButton;
    /** Holds the previous activity. */
    private final PongActivity previousActivity;
    /** Holds a reference to the engine. */
    private PongEngine engine;
    /** The task for toggling the visibility of the caret. */
    private Task caretVisibleToggleTask;
    public CreateGameActivity(final PongActivity backActivity) {
        previousActivity = Objects.requireNonNull(backActivity);
        nameLabel = new Label("Player name ");
        nameLabel.setTextColour(Color.WHITE);
        nameTextBox = new TextBox();
        nameTextBox.setText(System.getProperty("user.name", ""));
        createButton = new Button("Create");
    }
    @Override
    public final void onActivityStarted(final Context context) {
        engine = context.getEngine();
        engine.getKeyboardService().registerKeyTypedListener(nameTextBox::applyKeyType);
        caretVisibleToggleTask =
                engine.getSchedulerService().scheduleRepeatedly(() -> {
            nameTextBox.setCaretVisible(!nameTextBox.getCaretVisible());
        }, 0, 30);
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        engine.getKeyboardService().unregisterKeyTypedListener(nameTextBox::applyKeyType);
        caretVisibleToggleTask.cancel();
    }
    @Override
    public final void update() {
        final Point mousePosition = engine.getMouseService().getPosition();
        final boolean buttonPressed =
                engine.getMouseService().isButtonPressed(MouseEvent.BUTTON1);
        nameTextBox.handleMouse(mousePosition, buttonPressed);
        if (engine.getKeyboardService().isPressed(KeyEvent.VK_ESCAPE)) {
            engine.getActivityService().startActivity(new SlideAnimationActivity(
                    SlideDirection.DOWN, this, previousActivity));
        }
        if (createButton.getBounds().contains(mousePosition) && buttonPressed) {
            engine.getActivityService().startActivity(new WaitingForPlayerActivity(engine,
                    nameTextBox.getText()));
        }
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        // Clear the background.
        g.clearRect(0, 0, size.width, size.height);
        final int labelWidth = size.width / 5;
        final int labelHeight = size.height / 15;
        final int textBoxWidth = labelWidth * 2;
        final int textBoxHeight = labelHeight;
        // Position the "Player name" label.
        nameLabel.getBounds().width = labelWidth;
        nameLabel.getBounds().height = labelHeight;
        nameLabel.getBounds().x = (size.width - labelWidth - textBoxWidth) / 2;
        nameLabel.getBounds().y = size.height / 2 - (labelHeight / 2);
        // Position the player name text box.]
        nameTextBox.getBounds().width = nameLabel.getBounds().x + labelWidth;
        nameTextBox.getBounds().height = labelHeight;
        nameTextBox.getBounds().x =
                nameLabel.getBounds().x + nameLabel.getBounds().width;
        nameTextBox.getBounds().y = nameLabel.getBounds().y;
        // Position the "Create" button.
        createButton.getBounds().width = textBoxWidth / 3;
        createButton.getBounds().height = labelHeight;
        createButton.getBounds().x =
                nameTextBox.getBounds().x + textBoxWidth
                    - createButton.getBounds().width;
        createButton.getBounds().y = nameLabel.getBounds().y
                + textBoxHeight + (textBoxHeight / 2);
        // Render them.
        nameLabel.render(g);
        nameTextBox.render(g);
        createButton.render(g);
    }
}
