package raftModels;

import communicationThreads.ElectionThread;
import utils.RaftUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.RaftUtils.getPercentage;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class Node {
    private static final long SLEEP_TIME = 500;
    protected boolean isLeader = false;
    protected Map<String, Socket> socketMap;
    protected List<Socket> socketList;
    protected List<String> nodesIpList;

    protected Socket socket;
    protected InputStream inStream;
    protected OutputStream outStream;

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

    /**
     * Writes a message to a log
     *
     * @param s
     */
    public void receiveMessage(String s) {
        if (!s.toLowerCase().contains("del") && !s.contains(RaftUtils.VOTE)
                && !s.contains(RaftUtils.REQUEST_VOTE)) {
            log.append("Added entry: " + s);
        } else if (!s.contains(RaftUtils.VOTE)
                && !s.contains(RaftUtils.REQUEST_VOTE)) {
            log.append("Deleted entry: " + s.substring(s.indexOf("del") + 3));
        }
        log.append("\n");
    }

    /**
     * Returns ip of a node
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns connection port of a node
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns a log
     *
     * @return
     */
    public StringBuilder getLog() {
        return log;
    }

    /**
     * Returns remaining election timeout
     *
     * @return
     */
    public int getRemainingElectionTimeout() {
        return electionThread.getRemainingTime();
    }

    /**
     * Makes election time start
     */
    public void startElectionTime() {
        electionThread = new ElectionThread();
        electionThread.start();
    }

    /**
     * Return connected nodes
     *
     * @return
     */
    public List<Socket> getConnectedSockets() {
        return (socketList != null ? socketList : new ArrayList<>());
    }

    /**
     * Increases the number of voted nodes
     */
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
                        handleLog(inputMessage);
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

    public boolean handleLog(String message) {
        if (message.toUpperCase().contains(RaftUtils.SHOW_LOG)) {
            System.out.println("Log is the following:");
            System.out.println(getLog().toString());
        }
        return false;
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
        return (getPercentage(this.getAgreedNodesSize(), this.getAllNodesSize()) > 50);
    }

    /**
     * Returns a size of all connected nodes
     *
     * @return
     */
    public int getAllNodesSize() {
        return allNodesSize;
    }

    /**
     * Returns a size of accepted (voted) nodes
     *
     * @return
     */
    public final int getAgreedNodesSize() {
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

    /**
     * Set a list of sockets
     *
     * @param socketList
     */
    public void setSocketList(List<Socket> socketList) {
        this.socketList = socketList;
    }

    /**
     * Returns a last added entry of a log
     *
     * @return
     */
    public String getLastLogEntry() {
        String logs = log.toString();
        String line = "";
        Scanner scanner = new Scanner(logs);
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
        }

        return line;
    }
}
