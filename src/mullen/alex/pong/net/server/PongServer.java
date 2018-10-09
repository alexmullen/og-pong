package mullen.alex.pong.net.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mullen.alex.pong.Game;
import mullen.alex.pong.net.PongFrame;

/**
 * The pong server that coordinates the game between opponents.
 *
 * @author  Alex Mullen
 *
 */
public class PongServer implements PongClientConnectionListener.Handler,
        PongClientConnection.Handler {
    /** The logger instance for this class. */
    static final Logger LOG = Logger.getLogger(PongServer.class.getName());
    /** The game builder to use for creating a game. */
    final Game.Builder gameBuilder;
    /** The single thread executor service AKA the server thread. */
    final ScheduledExecutorService executor;
    /** Holds the connection listener that listens for connections. */
    final PongClientConnectionListener connectionListener;
    /** Holds the connections and their associated information. */
    final Map<PongClientConnection, PongClientBundle> connections;
    /** Holds the current state pattern state for this server. */
    private ServerState state;
    private Object shutdownLock;
    /**
     * Creates a new instance using the specified connection listener
     * builder and game builder.
     *
     * @param builder        the connection listener builder
     * @param gb             the game builder
     *
     * @throws IOException  if an I/O exception occurs whilst constructing the
     *                      connection listener
     */
    public PongServer(final PongClientConnectionListener.Builder builder,
            final Game.Builder gb)
                    throws IOException {
        gameBuilder = Objects.requireNonNull(gb);
        connectionListener = builder.build(this, this);
        connections = new HashMap<>();
        executor = Executors.newSingleThreadScheduledExecutor();
        state = new InitialState();
        shutdownLock = new Object();
    }
    /**
     * Start the server.
     */
    public final void start() {
        executor.execute(state::start);
    }
    /**
     * Shutdown the server.
     */
    public final void shutdown() {
        // TODO: is buggy.
        synchronized (shutdownLock) {
            if (executor.isShutdown()) {
                LOG.log(Level.SEVERE, "Attempt to shutdown when already is.");
            } else {
                /*
                 * Exception in thread "EngineThread" java.util.concurrent.RejectedExecutionException: Task java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask@746e0c4 rejected from java.util.concurrent.ScheduledThreadPoolExecutor@5c71c169[Terminated, pool size = 0, active threads = 0, queued tasks = 0, completed tasks = 8591]
    at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2047)
    at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:823)
    at java.util.concurrent.ScheduledThreadPoolExecutor.delayedExecute(ScheduledThreadPoolExecutor.java:326)
    at java.util.concurrent.ScheduledThreadPoolExecutor.schedule(ScheduledThreadPoolExecutor.java:533)
    at java.util.concurrent.ScheduledThreadPoolExecutor.execute(ScheduledThreadPoolExecutor.java:622)
    at java.util.concurrent.Executors$DelegatedExecutorService.execute(Executors.java:668)
    at mullen.alex.pong.net.server.PongServer.shutdown(PongServer.java:74)
    at mullen.alex.pong.gui.activity.HostedGameActivity.onActivityStopped(HostedGameActivity.java:109)
    at mullen.alex.pong.engine.PongActivityService.startActivity(PongActivityService.java:56)
    at mullen.alex.pong.gui.activity.HostedGameActivity.lambda$3(HostedGameActivity.java:225)
    at mullen.alex.pong.gui.activity.HostedGameActivity$$Lambda$73/387546379.run(Unknown Source)
    at mullen.alex.jge.AbstractFixedUpdateEngine.executePendingSyncTasks(AbstractFixedUpdateEngine.java:181)
    at mullen.alex.jge.AbstractFixedUpdateEngine.engineThreadBody(AbstractFixedUpdateEngine.java:155)
    at mullen.alex.jge.AbstractFixedUpdateEngine$$Lambda$10/120552131.run(Unknown Source)
    at java.lang.Thread.run(Thread.java:745)
                 */

                executor.execute(state::shutdown);
        //        executor.shutdown();
                try {
                    if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
                        LOG.log(Level.SEVERE, "timeout elapsed");
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }
    /**
     * Performs a tick.
     */
    final void tick() {
        executor.execute(state::tick);
    }
    /**
     * Changes the server's state.
     * <p>
     * <b>Make sure this is only executed within the executor thread.</b>
     * </p>
     *
     * @param newState  the state to change to
     */
    final void changeState(final ServerState newState) {
        LOG.info("changing state to: " + newState.getClass().getSimpleName());
        state = newState;
    }
    @Override
    public final void onNewConnection(final PongClientConnection connection) {
        executor.execute(() -> state.onNewConnection(connection));
    }
    @Override
    public final void onDisconnected(final PongClientConnection client) {
        executor.execute(() -> state.onDisconnected(client));
    }
    @Override
    public final void onReceivedFrame(final PongClientConnection client,
            final PongFrame frame) {
        executor.execute(() -> state.onReceivedFrame(client, frame));
    }
    /**
     * Represents the state of the server using the state pattern.
     *
     * @author  Alex Mullen
     *
     */
    public interface ServerState {
        /**
         * Handles start being called in the current state.
         */
        void start();
        /**
         * Handles shutdown being called in the current state.
         */
        void shutdown();
        /**
         * Handles tick being called in the current state.
         */
        void tick();
        /**
         * Handles a new connection being accepted in the current state.
         *
         * @param connection  the connection
         */
        void onNewConnection(PongClientConnection connection);
        /**
         * Handles losing a client connection in the current state.
         *
         * @param client  the client connection that was lost
         */
        void onDisconnected(PongClientConnection client);
        /**
         * Handles receiving a frame from a client in the current state.
         *
         * @param client  the client the frame originated from
         * @param frame   the frame
         */
        void onReceivedFrame(PongClientConnection client,
                PongFrame frame);
    }
    /**
     * Represents the state after the server instance has being instantiated.
     *
     * @author  Alex Mullen
     *
     */
    public class InitialState implements ServerState {
        @Override
        public final void start() {
            changeState(
                    new WaitingForConnectionsToBeReadyState(PongServer.this));
            connectionListener.start();
        }
        @Override
        public final void shutdown() {
            throw new IllegalStateException();
        }
        @Override
        public final void onNewConnection(
                final PongClientConnection connection) {
            throw new IllegalStateException();
        }
        @Override
        public final void onDisconnected(final PongClientConnection client) {
            throw new IllegalStateException();
        }
        @Override
        public final void onReceivedFrame(final PongClientConnection client,
                final PongFrame frame) {
            throw new IllegalStateException();
        }
        @Override
        public final void tick() {
            throw new IllegalStateException();
        }
    }
}
