package raftParticipants;

import utils.ConsensusUtils;
import utils.LeaderReadThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by Valerii Volkov on 20.06.2016.
 */
public class Leader extends Candidate {
    private StringBuilder log;
    private boolean stopped = false;

    public Leader(String ip, int port) throws IOException, InterruptedException {
        super(ip, port);
        createSocket();
    }

    /**
     * Creates socket
     */
    protected void createSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            nodesList = new HashMap<>();
            nodesIpList = new ArrayList<>();
            while (true) {
                socket = serverSocket.accept();
                nodesIpList.add(socket.getInetAddress().getHostAddress());
                nodesList.put(socket.getRemoteSocketAddress().toString(), socket);

                ++allNodesSize;

                createReadThread();
                createWriteThread();
            }
        } catch (IOException io) {
            LOGGER.log(Level.SEVERE, "IO Exception", io);
        }
    }

    /**
     * Creates read thread for getting messages from clients
     */
    protected void createReadThread() {
        LeaderReadThread leaderReadThread = new LeaderReadThread(socket, this);
        leaderReadThread.setPriority(Thread.MAX_PRIORITY);
        leaderReadThread.start();
    }

    /**
     * Handles message about exit from a client
     *
     * @param message
     */
    public boolean handleStop(String message) {
        if (message.toUpperCase().equals(ConsensusUtils.LEADER_QUITS)) {
            stopped = true;
            return stopped;
        }
        return false;
    }

    public boolean runs()
    {
        if(!stopped)
        {
            return true;
        }
        else {
            return false;
        }
    }
}
