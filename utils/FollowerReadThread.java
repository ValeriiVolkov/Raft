package utils;

import raftModels.Follower;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Valerii Volkov
 */
public class FollowerReadThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private Follower follower;
    protected static final String CHARSET = "UTF-8";

    private Logger LOGGER = Logger.getLogger("FollowerReadThread");

    public FollowerReadThread(Socket socket, Follower follower) {
        this.socket = socket;
        this.follower = follower;
    }

    public void run() {
        while (socket.isConnected()) {
            try {
                byte[] readBuffer = new byte[200];

                inputStream = socket.getInputStream();
                int size = inputStream.read(readBuffer);
                if (size > 0) {
                    byte[] arrayBytes = new byte[size];
                    System.arraycopy(readBuffer, 0, arrayBytes, 0, size);
                    String message = new String(arrayBytes, CHARSET);
                    follower.receiveMessage(message);
                }
            } catch (SocketException se) {
                //Catched, however, not logged to eliminate excess output in the console
            } catch (IOException i) {
                LOGGER.log(Level.SEVERE, "IO Exception", i);
            } catch (IllegalMonitorStateException ie) {
                LOGGER.log(Level.SEVERE, "Illegal Monitor State Exception", ie);
            } finally {
                interrupt();
            }
        }
    }
}
