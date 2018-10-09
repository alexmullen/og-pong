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
import mullen.alex.pong.StandardGameBuilder;
import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongActivity;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.activity.SlideAnimationActivity.SlideDirection;
import mullen.alex.pong.gui.components.Label;
import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.Role;
import mullen.alex.pong.net.SocketConnectionListener;
import mullen.alex.pong.net.StreamConnectionListener;
import mullen.alex.pong.net.PongFrame.Type;
import mullen.alex.pong.net.client.PongClient;
import mullen.alex.pong.net.client.PongClientFactory;
import mullen.alex.pong.net.server.JsonPongClientConnectionListener;
import mullen.alex.pong.net.server.PongClientConnectionListener;
import mullen.alex.pong.net.server.PongServer;

/**
 * An activity that presents to the user some form of output to indicate that
 * we are waiting for someone to join the game before it can start.
 *
 * @author  Alex Mullen
 *
 */
public class WaitingForPlayerActivity implements PongActivity {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(WaitingForPlayerActivity.class.getName());
    /** How many dots (.) to show up to. */
    private static final int DOTDOTDOT_LENGTH = 3;
    /** The interval in ticks the dot animation updates. */
    private static final int DOTDOTDOT_INTERVAL = 30;
    /** The engine reference. */
    private final PongEngine engine;
    /** The label labelled "Waiting for player". */
    private final Label statusLabel;
    /** Holds the name of the player hosting the game. */
    private final String hostingPlayerName;
    /** Holds The receive thread. */
    private final Thread recvThread;
    /** The server instance. */
    private PongServer server;
    /** The local client instance. */
    private PongClient clientInstance;
    /** The task that animates the series of animating dots. */
    private Task dotDotDotTask;
    /** Holds the current series of dots to display. */
    private String dotDotDotStr;
    /** Indicates whether the wait was cancelled. */
    private boolean waitCancelled;
    /**
     * Creates a new instance with the engine to use being injected and name to
     * use for the player hosting the game.
     *
     * @param eng            the engine
     * @param playerNameStr  the host player name
     */
    public WaitingForPlayerActivity(final PongEngine eng,
            final String playerNameStr) {
        engine = Objects.requireNonNull(eng);
        hostingPlayerName = Objects.requireNonNull(playerNameStr);
        statusLabel = new Label("Waiting for player");
        statusLabel.setTextColour(Color.WHITE);
        dotDotDotStr = "";
        recvThread = new Thread(this::receiveThreadProcedure);
    }
    @Override
    public final void onActivityStarted(Context context) {
        /*
         * Start a task to animate a string of dots to indicate that we are
         * waiting for a player to join.
         */
        dotDotDotTask = engine.getSchedulerService().scheduleRepeatedly(() -> {
            if (dotDotDotStr.length() == DOTDOTDOT_LENGTH) {
                dotDotDotStr = "";
            } else {
                dotDotDotStr += ".";
            }
        }, 0, DOTDOTDOT_INTERVAL);
        /*
         * Construct a server instance.
         */
        // We want to use a socket stream listener so create a builder for it.
        final StreamConnectionListener.Builder streamConnectionListener
                = new SocketConnectionListener.Builder(30000, s -> {
                    /*
                     * Enable TCP_NODELAY on each connected socket so that
                     * frames are sent ASAP.
                     */
                    try {
                        s.setTcpNoDelay(true);
                    } catch (final Exception e) { // TODO: Catching all exceptions?
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
        /*
         * We want to use a JSON client connection listener so create a builder
         * for it and inject into it the stream listener builder that we
         * created.
         */
        final PongClientConnectionListener.Builder clientListener =
                new JsonPongClientConnectionListener.Builder(
                        streamConnectionListener);
        try {
            server = new PongServer(clientListener, new StandardGameBuilder());     // TODO: Hardcoded!
            server.start();
            // Create and connect a local client to the server.
//            clientInstance = PongClientFactory.createAndConnect(server);
            clientInstance = PongClientFactory.createAndConnect("localhost", 30000);    // TODO: !!!
            // Send an authentication frame.
            final PongFrame authFrame = new PongFrame(Type.AUTHORISATION);
            authFrame.args.put("NAME", hostingPlayerName);
            clientInstance.sendFrameToServer(authFrame);
            // Process received frames in a separate thread.
            recvThread.start();
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    @Override
    public final void update() {
        if (engine.getKeyboardService().isPressed(KeyEvent.VK_ESCAPE)) {
            waitCancelled = true;
            clientInstance.close(); //!!maybe not thread safe.
            server.shutdown();
            engine.getActivityService().startActivity(
                    new MainMenuActivitiy(engine, new World2D(1024, 768)));
        }
    }
    @Override
    public final void onActivityStopped(final StoppedReason reason) {
        dotDotDotTask.cancel();
        if (reason == StoppedReason.SERVICE_SHUTDOWN) {
            clientInstance.close(); //!!maybe not thread safe.
            server.shutdown();
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
        statusLabel.setText("Waiting for player" + dotDotDotStr);
        statusLabel.render(g);
    }
    private final void receiveThreadProcedure() {
        boolean continueReceiving = true;
        do {
            final PongFrame frame = clientInstance.recvFrameFromServer();
            if (frame == null) {
                LOG.severe("Lost connection to our local server somehow.");
                clientInstance.close();
                continueReceiving = false;
            } else {
                // We need to listen for the game start event.
                if (frame.getType() == Type.EVENT
                        && "STARTED".equals(frame.args.get("EVENT"))) {
                    handleReceivedStartEvent(frame);
                    continueReceiving = false;
                } else if (frame.getType() == Type.PING) {
                    clientInstance.sendFrameToServer(new PongFrame(Type.PING_REPLY));
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
            args.client = clientInstance;
            args.clientRole = Role.valueOf(frame.args.get("ROLE"));
            args.players = frame.players;
            engine.getActivityService().startActivity(
                    new SlideAnimationActivity(
                            SlideDirection.LEFT,
                            this,
                            new LocallyConnectedGameActivity(server,
                                    new ConnectedGameActivity(args))));
        });
    }
}
