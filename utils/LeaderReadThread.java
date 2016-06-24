package utils;

import raftParticipants.Leader;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Valerii Volkov
 */
public class LeaderReadThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private Leader leader;
    protected static final String CHARSET = "UTF-8";

    private Logger LOGGER = Logger.getLogger("LeaderReadThread");

    public LeaderReadThread(Socket socket, Leader leader)
    {
        this.socket = socket;
        this.leader = leader;
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

                    synchronized (message) {
                        if(leader.handleStop(message))
                        {
                            leader.sendToAllConnectedNodes(ConsensusUtils.LEADER_QUITS);
                        }

                        leader.sendToAllConnectedNodes(message);
                    }
                } else {
                    //If there is at least one connected client then notify these clients
                    if (!leader.getClients().isEmpty()) {
                        notify();
                        leader.closeClient(socket);
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
}