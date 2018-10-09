package mullen.alex.pong.net.server;

import mullen.alex.pong.net.Connection;
import mullen.alex.pong.net.PongFrame;

/**
 * Defines an interface for representing and communicating with a Pong client
 * from the server's perspective.
 *
 * @author  Alex Mullen
 *
 */
public interface PongClientConnection extends Connection {
    /**
     * Sends a frame to this client.
     *
     * @param frame  the frame to send
     */
    void sendFrameToClient(PongFrame frame);
    /**
     * Defines an interface for implementing a class that handles events and
     * actions relating to the pong client connection.
     *
     * @author  Alex Mullen
     *
     */
    interface Handler {
        /**
         * Invoked when a frame is received from a client.
         *
         * @param client  the client the frame originated from
         * @param frame   the frame
         */
        void onReceivedFrame(PongClientConnection client, PongFrame frame);
        /**
         * Invoked when the connection to the client is closed or lost.
         *
         * @param client  the client that is no longer connected
         */
        void onDisconnected(PongClientConnection client);
    }
}
