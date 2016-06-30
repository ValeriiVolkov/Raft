package raftModels;

import utils.RaftUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class Candidate extends Node {
    protected int allNodesSize;

    public Candidate(){}

    public Candidate(List<Socket> sockets) {
        this.socketList = sockets;
        startElectionTime();
    }

    /**
     * Creates socket
     */
    public void start() throws InterruptedException, IOException {
        System.out.println("Candidate starts...");
        try {
            for (Socket s : socketList) {
                requestVote(s);
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
        sendToAllConnectedNodes(RaftUtils.REQUEST_VOTE);
    }

    /**
     * Sends request to other nodes and collects votes to become a leader
     */
    public void requestVote() throws InterruptedException, IOException {
        sendToAllConnectedNodes(RaftUtils.REQUEST_VOTE);
    }
}
