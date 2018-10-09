package mullen.alex.pong.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a generic connection between two entities that communicate with
 * exchanging raw bytes via two streams.
 *
 * @author  Alex Mullen
 *
 */
public interface StreamConnection extends Connection {
    /**
     * Gets the input stream to read received data from the connection.
     *
     * @return              the input stream
     * @throws IOException  if an error occurs acquiring it
     */
    InputStream getInputStream() throws IOException;
    /**
     * Gets the output stream to send data to the connection.
     *
     * @return              the output stream
     * @throws IOException  if an error occurs acquiring it
     */
    OutputStream getOutputStream() throws IOException;
    /**
     * A class for representing a way to save the information required to create
     * a connection, but leave the instantiation to something else.
     * <p>
     * Rather than giving a caller the raw <code>StreamConnection</code>
     * instance, they are given a <code>StreamConnection.Factory</code> instance
     * so that they have the control for creating the connection. This can be
     * useful in situations where the connection might have to be re-built
     * because it was previously lost.
     *
     * @author  Alex Mullen
     */
    @FunctionalInterface
    public interface Factory {
        /**
         * Creates a new <code>StreamConnection</code> instance based on the
         * settings in this factory.
         *
         * @return              a new <code>StreamConnection</code> instance
         * @throws IOException  if an error occurs
         */
        StreamConnection newInstance() throws IOException;
    }
}
