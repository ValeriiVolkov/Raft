package communicationThreads;

import raftModels.Follower;
import utils.RaftUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Valerii Volkov
 */
public class NodeReadThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private Follower follower;
    protected static final String CHARSET = "UTF-8";

    private Logger LOGGER = Logger.getLogger("FollowerReadThread");

    public NodeReadThread(Socket socket, Follower follower) {
        this.socket = socket;
        this.follower = follower;
    }

    /**
     * Runs a thread for node to read input from connected sockets
     */
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
                    follower.sendToAllConnectedNodes(follower.getLastLogEntry());

                    if (!message.contains(RaftUtils.REQUEST_VOTE)) {
                        System.out.println(message);
                    }

                    synchronized (message) {
                        handleVote(message);
                        if (handleStop(message)) {
                            follower.sendToAllConnectedNodes(RaftUtils.LEADER_QUITS);
                            interrupt();
                            return;
                        }
                    }
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

    /**
     * Handles votes recieved from voting nodes
     * @param s
     * @throws IOException
     */
    public void handleVote(String s) throws IOException {
        if (s.contains(RaftUtils.REQUEST_VOTE)) {
            socket.getOutputStream().write(RaftUtils.VOTE.getBytes(CHARSET));
        } else if (s.contains(RaftUtils.VOTE)) {
            follower.addAcceptedVote();
        }
    }

    /**
     * Handles leader's exit
     * @param message
     * @return
     */
    public boolean handleStop(String message) {
        if (message.toUpperCase().contains(RaftUtils.LEADER_QUITS)) {
            System.out.println("Leaders quits");
        }
        return false;
    }
}
