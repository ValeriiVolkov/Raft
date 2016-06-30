package raftModels;

import utils.CandidateReadThread;
import utils.RaftUtils;
import utils.StringChangeListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static utils.RaftUtils.getPercentage;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class Candidate extends Follower {
    private StringChangeListener stringChangeListener;
    protected int allNodesSize;
    private int acceptedNodesSize = 0;

    public Candidate(String ip, int port) throws IOException, InterruptedException {
        super(ip, port);
        startElectionTime();
    }

    /**
     * Creates socket
     */
    public void start() throws InterruptedException {
        System.out.println("Candidate starts...");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            createReadThread();
            socketList = new HashMap<>();
            nodesIpList = new ArrayList<>();
            System.out.println("Candidate is started");
            while (true) {
                socket = serverSocket.accept();
                nodesIpList.add(socket.getInetAddress().getHostAddress());
                socketList.put(socket.getRemoteSocketAddress().toString(), socket);
                requestVote(socketList.get(socket.getRemoteSocketAddress().toString()));
                ++allNodesSize;
            }
        } catch (IOException io) {
            LOGGER.log(Level.SEVERE, "IO Exception", io);
        }
    }

    /**
     * Sends request to other nodes and collects votes to become a leader
     */
    public void requestVote(Socket socket) throws InterruptedException, IOException {
        socketList.put(socket.getInetAddress().getHostName(), socket);
        sendToAllConnectedNodes(RaftUtils.REQUEST_VOTE);
    }

    /**
     * Sends request to other nodes and collects votes to become a leader
     */
    public void requestVote() throws InterruptedException, IOException {
        sendToAllConnectedNodes(RaftUtils.REQUEST_VOTE);
    }

    /**
     * Creates read thread for getting messages from clients
     */
    protected void createReadThread() {
        CandidateReadThread candidateReadThread = new CandidateReadThread(socket, this);
        candidateReadThread.setPriority(Thread.MAX_PRIORITY);
        candidateReadThread.start();
    }

    /**
     * Sends messages to all clients
     *
     * @param message
     */
    public void sendToAllConnectedNodes(String message) throws IOException {
        for (Map.Entry<String, Socket> entry : socketList.entrySet()) {
            OutputStream outputStream = entry.getValue().getOutputStream();
            try {
                outputStream.write(message.getBytes(CHARSET));
            } catch (IOException ie) {
                LOGGER.log(Level.SEVERE, "IO Exception", ie);
            }
        }
    }

    /**
     * Returns the list of all connected clients
     */
    public List<String> getConnectedNodes() {
        return nodesIpList;
    }

    /**
     * Closes a socket
     *
     * @throws IOException
     */
    public void close() throws IOException {
        socket.close();
    }

    /**
     * Convert to candidate
     */
    public Leader toLeader() throws IOException, InterruptedException {
        return new Leader(ip, port);
    }

    /**
     * Is candidate chosen to be a leader
     */
    public boolean isLeader() {
        return (getPercentage(this.getAcceptedNodesSize(), this.getAllNodesSize()) > 0.5);
    }

    public final void addAcceptedVote() {
        ++acceptedNodesSize;
    }

    public int getAllNodesSize() {
        return allNodesSize;
    }

    public final int getAcceptedNodesSize() {
        return acceptedNodesSize;
    }
}
