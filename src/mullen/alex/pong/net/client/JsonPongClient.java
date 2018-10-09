package mullen.alex.pong.net.client;

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
 * A {@link PongClient} implementation that communicates across a
 * {@link StreamConnection} using JSON.
 *
 * @author  Alex Mullen
 *
 */
public class JsonPongClient implements PongClient {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(JsonPongClient.class.getName());
    /** The character encoding this connection reads and writes in. */
    private static final Charset CONNECTION_CHARSET = StandardCharsets.UTF_8;
    /** The connection. */
    private final StreamConnection connection;
    /** The JSON reader instance for reading the JSON tokens from the stream. */
    private final JsonReader jsonReader;
    /** The writer instance for writing the JSON to the stream. */
    private final Writer writer;
    /** The Gson instance to use for parsing and writing JSON frames. */
    private final Gson gson;
    /**
     * Creates a new instance that uses the given stream connection.
     *
     * @param streamConnection  the connection
     * @throws IOException      if an exception occurs
     */
    public JsonPongClient(final StreamConnection streamConnection) throws IOException {
        connection = Objects.requireNonNull(streamConnection);
        writer = new BufferedWriter(new OutputStreamWriter(
                connection.getOutputStream(), CONNECTION_CHARSET));
        jsonReader = new JsonReader(
                new BufferedReader(
                        new InputStreamReader(connection.getInputStream(),
                                CONNECTION_CHARSET)));
        gson = new Gson();
    }
    @Override
    public final PongFrame recvFrameFromServer() {
        PongFrame frame = null;
        try {
            frame = gson.fromJson(jsonReader, PongFrame.class);
        } catch (final JsonSyntaxException | JsonIOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
//            close(); // feels dirty having this here.
        }
        return frame;
    }
    @Override
    public final void sendFrameToServer(final PongFrame frame) {
        try {
            writer.write(gson.toJson(frame));
            writer.flush();
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    @Override
    public final void close() {
        /*
         * Need to close writer first otherwise closing the reader first causes
         * the code to hang for some reason.
         */
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
    public final String toString() {
        return "JsonPongClient [connection=" + connection + "]";
    }
}
