package mullen.alex.pong.net.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import mullen.alex.pong.net.PongFrame;
import mullen.alex.pong.net.StreamConnection;

/**
 * A {@link PongClientConnection} implementation that communicates across a
 * {@link StreamConnection} using JSON.
 *
 * @author  Alex Mullen
 *
 */
public class JsonPongClientConnection implements PongClientConnection {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(JsonPongClientConnection.class.getName());
    /** The character encoding this connection reads and writes in. */
    private static final Charset CONNECTION_CHARSET = StandardCharsets.UTF_8;
    /** The connection. */
    private final StreamConnection connection;
    /** Holds the handler. */
    private final Handler handler;
    /** The thread that sits and waits for received frames. */
    private final Thread receiveFrameThread;
    /** The JSON reader instance for reading the JSON tokens from the stream. */
    private final JsonReader jsonReader;
    /** The writer instance for writing the JSON to the stream. */
    private final Writer writer;
    /** The Gson instance to use for parsing and writing JSON frames. */
    private final Gson gson;
    /**
     * Creates a new instance that uses the given stream connection and handler.
     *
     * @param streamConnection   the connection
     * @param connectionHandler  the handler
     *
     * @throws IOException       if an exception occurs
     */
    public JsonPongClientConnection(final StreamConnection streamConnection,
            final Handler connectionHandler) throws IOException {
        connection = Objects.requireNonNull(streamConnection);
        handler = Objects.requireNonNull(connectionHandler);
        writer = new BufferedWriter(new OutputStreamWriter(
                connection.getOutputStream(), CONNECTION_CHARSET));
        jsonReader = new JsonReader(
                new BufferedReader(
                        new InputStreamReader(connection.getInputStream(),
                                CONNECTION_CHARSET)));
        gson = new Gson();
        receiveFrameThread = new Thread(this::receiveThreadBody);
    }
    /**
     * Initialises the receive thread since it is bad practice to start threads
     * within constructors.
     */
    final void initialise() {
        receiveFrameThread.start();
    }
    /**
     * The code for the receive thread.
     */
    private void receiveThreadBody() {
        boolean continueLoop = true;
        do {
            try {
                final PongFrame frame =
                        gson.fromJson(jsonReader, PongFrame.class);
                // EOF check.
                if (frame == null) {
                    continueLoop = false;
                } else {
                    handler.onReceivedFrame(this, frame);
                }
            } catch (final JsonSyntaxException | JsonIOException e) {
                continueLoop = false;
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        } while (continueLoop);
        shutdownAndCleanup();
        handler.onDisconnected(this);
    }
    /**
     * Closes the connection and cleans up any resources used. Successive calls
     * have no effect.
     */
    private void shutdownAndCleanup() {
        try {
            writer.close();
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        try {
            jsonReader.close();
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        connection.close();
    }
    @Override
    public final void close() {
        shutdownAndCleanup();
        // Wait for receive thread to finish.
        try {
            receiveFrameThread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    @Override
    public final void sendFrameToClient(final PongFrame frame) {
        try {
            writer.write(gson.toJson(frame));
            writer.flush();
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    @Override
    public final String toString() {
        return "JsonPongClientConnection [connection=" + connection + "]";
    }
}
