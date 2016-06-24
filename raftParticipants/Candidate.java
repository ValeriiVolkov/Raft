package raftParticipants;

import models.Node;
import utils.CandidateReadThread;
import utils.ConsensusUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static java.lang.Thread.sleep;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class Candidate extends Node {
    protected Map<String, Socket> nodesList;
    protected List<String> nodesIpList;

    protected int allNodesSize;
    private int acceptedNodesSize = 0;

    public Candidate(String ip, int port) throws IOException, InterruptedException {
        super(ip, port);
        createSocket();
        requestVote();
    }

    public void requestVote() throws InterruptedException, IOException {
        sleep(electionTimeout);
        sendToAllConnectedNodes(ConsensusUtils.REQUEST_VOTE);
    }

    /**
     * Creates socket
     */
    protected void createSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            nodesList = new HashMap<>();
            nodesIpList = new ArrayList<>();
            System.out.println("Server is started");
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
        CandidateReadThread candidateReadThread = new CandidateReadThread(socket, this);
        candidateReadThread.setPriority(Thread.MAX_PRIORITY);
        candidateReadThread.start();
    }

    protected void createWriteThread() {
        Thread writeThread = new Thread() {
            public void run() {
                try {
                    while (socket.isConnected()) {
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                        sleep(SLEEP_TIME);
                        String typedMessage = inputReader.readLine();
                        if (typedMessage != null && typedMessage.length() > 0) {
                            sendToAllConnectedNodes(typedMessage);
                            sleep(SLEEP_TIME);
                        }
                    }
                } catch (UnsupportedEncodingException ue) {
                    LOGGER.log(Level.SEVERE, "Unsupported Encoding Exception", ue);
                } catch (UnknownHostException e) {
                    LOGGER.log(Level.SEVERE, "Unknown Host Exception", e);
                } catch (InterruptedException ie) {
                    LOGGER.log(Level.SEVERE, "Interrupted Exception", ie);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "IO Exception", e);
                } finally {
                    interrupt();
                }
            }
        };
        writeThread.setPriority(Thread.MAX_PRIORITY);
        writeThread.start();
    }

    /**
     * Sends messages to all clients
     *
     * @param message
     */
    public void sendToAllConnectedNodes(String message) throws IOException {
        for (Map.Entry<String, Socket> entry : nodesList.entrySet()) {
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
    public List<String> getClients() {
        return nodesIpList;
    }

    /**
     * Closes a socket
     *
     * @param socket
     * @throws IOException
     */
    public void closeClient(Socket socket) throws IOException {
        socket.close();
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
