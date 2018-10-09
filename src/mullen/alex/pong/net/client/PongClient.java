package mullen.alex.pong.net.client;

import mullen.alex.pong.net.Connection;
import mullen.alex.pong.net.PongFrame;

/**
 * Defines an interface for implementing a client that communicates with a Pong
 * server.
 *
 * @author  Alex Mullen
 *
 */
public interface PongClient extends Connection {
    /**
     * Sends a frame to the server.
     *
     * @param frame  the frame
     */
    void sendFrameToServer(PongFrame frame);
    /**
     * Waits for then returns a frame sent from the server. This is a blocking
     * operation.
     *
     * @return  the received frame or <code>null</code> if the connection is
     *          closed
     */
    PongFrame recvFrameFromServer();
}
