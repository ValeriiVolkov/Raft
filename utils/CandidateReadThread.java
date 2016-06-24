package utils;

import raftParticipants.Candidate;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Valerii Volkov on 23.06.2016.
 */
public class CandidateReadThread extends Thread {
    private Socket socket;
    private InputStream inputStream;
    private Candidate candidate;
    private static final String CHARSET = "UTF-8";
    private Logger LOGGER = Logger.getLogger("LeaderReadThread");

    public CandidateReadThread(Socket socket, Candidate candidate) {
        this.socket = socket;
        this.candidate = candidate;
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
                        handleVote(message);
                    }
                } else {
                    //If there is at least one connected client then notify these clients
                    if (!candidate.getClients().isEmpty()) {
                        notify();
                        candidate.closeClient(socket);
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

    public void handleVote(String s) {
        if (s.equals(ConsensusUtils.VOTE)) {
            candidate.addAcceptedVote();
        }
    }
}
