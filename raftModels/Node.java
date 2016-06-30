package raftModels;

import communicationThreads.ElectionThread;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.RaftUtils.getPercentage;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class Node {
    private static final long SLEEP_TIME = 500;
    protected Map<String, Socket> socketMap;
    protected List<Socket> socketList;
    protected List<String> nodesIpList;

    protected Socket socket;
    protected InputStream inStream;
    protected OutputStream outStream;

    protected static final int SIZE_OF_BUFFER = 100;
    protected static final String CHARSET = "UTF-8";
    protected Logger LOGGER = Logger.getLogger("Node");

    protected String ip;
    protected int port;

    protected StringBuilder log;

    protected int allNodesSize;
    private int acceptedNodesSize = 0;

    private ElectionThread electionThread;

    public Node() {
    }

    public Node(String ip, int port) {
        log = new StringBuilder();

        this.ip = ip;
        this.port = port;
    }

    public Node(int port) {

        log = new StringBuilder();
        this.port = port;
    }

    public void receiveMessage(String s) {
        log.append(s);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public StringBuilder getLog() {
        return log;
    }

    public int getRemainingElectionTimeout() {
        return electionThread.getRemainingTime();
    }

    public void addEntry(String s) {
        log.append(s);
    }

    /**
     * Makes election time start
     */
    public void startElectionTime() {
        electionThread = new ElectionThread();
        electionThread.start();
    }

    public List<Socket> getConnectedSockets() {
        return (socketList != null ? socketList : new ArrayList<>());
    }

    public final void addAcceptedVote() {
        ++acceptedNodesSize;
    }

    /**
     * Creates write for sending messages to receiver
     */
    protected void createWriteThread() throws IOException {
        Thread writeThread = new Thread() {
            public void run() {
                while (socket.isConnected()) {
                    try {
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                        sleep(SLEEP_TIME);
                        String inputMessage = inputReader.readLine();
                        if (inputMessage != null && inputMessage.length() > 0) {
                            synchronized (inputMessage) {
                                socket.getOutputStream().write(inputMessage.getBytes(CHARSET));
                                sendToAllConnectedNodes(inputMessage);
                                sleep(SLEEP_TIME);
                            }
                        }
                    } catch (IOException ie) {
                        LOGGER.log(Level.SEVERE, "IO Exception", ie);
                    } catch (InterruptedException ie) {
                        LOGGER.log(Level.SEVERE, "Interrupted Exception", ie);
                    }
                }
            }
        };
        writeThread.setPriority(Thread.MAX_PRIORITY);
        writeThread.start();
    }

    /**
     * Convert to candidate
     */
    public Candidate toCandidate() {
        return new Candidate();
    }

    /**
     * Convert to leader
     */
    public Leader toLeader() {
        return new Leader();
    }

    /**
     * Is candidate chosen to be a leader
     */
    public boolean isLeader() {
        return (getPercentage(this.getAcceptedNodesSize(), this.getAllNodesSize()) > 50);
    }

    public int getAllNodesSize() {
        return allNodesSize;
    }

    public final int getAcceptedNodesSize() {
        return acceptedNodesSize;
    }

    /**
     * Sends messages to all clients
     *
     * @param message
     */
    public void sendToAllConnectedNodes(String message) throws IOException {
        if (socketList != null) {
            for (Socket s : socketList) {
                OutputStream outputStream = s.getOutputStream();
                try {
                    outputStream.write(message.getBytes(CHARSET));
                } catch (IOException ie) {
                    LOGGER.log(Level.SEVERE, "IO Exception", ie);
                }
            }
        }
    }

    public void setSocketList(List<Socket> socketList) {
        this.socketList = socketList;
    }
}
