package mullen.alex.pong.gui.activity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import mullen.alex.jge.TickSchedulerService.Task;
import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.components.Label;
import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.PongFrame.Type;
import mullen.alex.pong.net.Role;
import mullen.alex.pong.net.client.PongClient;
import mullen.alex.pong.net.client.PongClientFactory;

/**
 * An activity for creating the connection to the server and waiting for the
 * game to start before handing off the client to the game activity.
 *
 * @author  Alex Mullen
 *
 */
public class ConnectingActivity implements PongActivity {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(ConnectingActivity.class.getName());
    /** How many dots (.) to show up to. */
    private static final int DOTDOTDOT_LENGTH = 3;
    /** The interval in ticks the dot animation updates. */
    private static final int DOTDOTDOT_INTERVAL = 30;
    /** The engine reference. */
    private final PongEngine engine;
    /** The server host to connect to. */
    private final String hostname;
    /** Holds the name of this player. */
    private final String playerName;
    /** The thread for executing the connection initiation code. */
    private final Thread connectThread;
    /** The status label. */
    private final Label statusLabel;
    /** The client instance. */
    private PongClient client;
    /** The task that animates the series of animating dots. */
    private Task dotDotDotTask;
    /** Holds the current series of dots to display. */
    private String dotDotDotStr;
    /** Indicates whether the wait was cancelled. */
    private boolean waitCancelled;
    /**
     * Creates a new instance using the specified arguments.
     *
     * @param eng            the engine instance
     * @param hostnameStr    the host to connect to
     * @param playerNameStr  the name of the local player
     */
    public ConnectingActivity(final PongEngine eng, final String hostnameStr,
            final String playerNameStr) {
        engine = Objects.requireNonNull(eng);
        hostname = Objects.requireNonNull(hostnameStr);
        playerName = Objects.requireNonNull(playerNameStr);
        connectThread = new Thread(this::connect);
        statusLabel = new Label("Connecting");
        statusLabel.setTextColour(Color.WHITE);
        dotDotDotStr = "";
    }
    @Override
    public final void onActivityStarted(final Context context) {
        /*
         * Start a task to animate a string of dots to indicate that we are
         * waiting for something.
         */
        dotDotDotTask = engine.getSchedulerService().scheduleRepeatedly(() -> {
            if (dotDotDotStr.length() == DOTDOTDOT_LENGTH) {
                dotDotDotStr = "";
            } else {
                dotDotDotStr += ".";
            }
        }, 0, DOTDOTDOT_INTERVAL);
        // Connect to the server in separate thread/
        connectThread.start();
    }
    /**
     * Connects to the server.
     */
    private void connect() {
        try {
            client = PongClientFactory.createAndConnect(hostname, 30000);
            // Send an authorisation frame.
            final PongFrame authFrame = new PongFrame(Type.AUTHORISATION);
            authFrame.args.put("NAME", playerName);
            client.sendFrameToServer(authFrame);
            // Empty.
            final Thread recvThread = new Thread(this::receiveThreadProcedure);
            recvThread.start();
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            engine.execute(() ->
                engine.getActivityService().startActivity(new MainMenuActivitiy(engine,
                        new World2D(1024, 768)))
            );
        }
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        dotDotDotTask.cancel();
        // Can be null if connecting failed.
        if (reason == StoppedReason.SERVICE_SHUTDOWN && client != null) {
            client.close();
        }
    }
    @Override
    public final void update() {
        if (engine.getKeyboardService().isPressed(KeyEvent.VK_ESCAPE)) {
            waitCancelled = true;
            connectThread.interrupt();
            try {
                connectThread.join();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
            if (client != null) {
                client.close();
            }
            engine.getActivityService().startActivity(
                    new MainMenuActivitiy(engine, new World2D(1024, 768)));
        }
    }
    @Override
    public final void render(final Graphics2D g, final Dimension size,
            final double delta) {
        g.clearRect(0, 0, size.width, size.height);
        final int labelWidth = size.width * 3 / 4;
        final int labelHeight = size.height / 10;
        statusLabel.getBounds().x = (size.width - labelWidth) / 2;
        statusLabel.getBounds().y = size.height / 2 - labelHeight / 2;
        statusLabel.getBounds().width = labelWidth;
        statusLabel.getBounds().height = labelHeight;
        statusLabel.setText("Connecting" + dotDotDotStr);
        statusLabel.render(g);
    }
    private void receiveThreadProcedure() {
        boolean continueReceiving = true;
        do {
            final PongFrame frame = client.recvFrameFromServer();
            if (frame == null) {
                onDisconnected();
                continueReceiving = false;
            } else {
                // We need to wait for a START frame to indicate the game has started.
                if (frame.getType() == Type.EVENT
                        && "STARTED".equals(frame.args.get("EVENT"))) {
                    handleReceivedStartEvent(frame);
                    continueReceiving = false;
                } else if (frame.getType() == Type.PING) {
                    client.sendFrameToServer(new PongFrame(Type.PING_REPLY));
                } else {
                    LOG.warning("Unexpected frame: " + frame);
                }
            }
        } while (continueReceiving);
    }
    private void handleReceivedStartEvent(final PongFrame frame) {
        engine.execute(() -> {
            // Make sure the wait was not cancelled.
            if (waitCancelled) {
                return;
            }
            final ConnectedGameActivity.ConstructorArgs args =
                    new ConnectedGameActivity.ConstructorArgs();
            args.engine = engine;
            args.client = client;
            args.clientRole = Role.valueOf(frame.args.get("ROLE"));
            args.players = frame.players;
            engine.getActivityService().startActivity(
                    new ConnectedGameActivity(args));
        });
    }
    private void onDisconnected() {
        engine.execute(() -> {
            LOG.info("Lost connection to server.");
            engine.getActivityService().startActivity(
                    new MainMenuActivitiy(engine, new World2D(1024, 768)));
        });
    }
}
