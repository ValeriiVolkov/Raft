package raftModels;

import communicationThreads.NodeReadThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Valerii Volkov on 20.06.2016.
 */
public class Follower extends Node {
    private ServerSocket serverSocket;

    public Follower(String ip, int port) {
        super(ip, port);
    }

    public Follower(int port) {
        super(port);
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("Follower starts...");
        socketMap = new HashMap<>();
        socketList = new ArrayList<>();
        nodesIpList = new ArrayList<>();
        if (ip == null) {
            startServer();
            return;
        }

        socket = new Socket(ip, port);
        inStream = socket.getInputStream();
        outStream = socket.getOutputStream();

        createReadThread();
        createWriteThread();

        startElectionTime();

        //Send log file after a first connection
        outStream.write(getLog().toString().getBytes(CHARSET));
    }

    public void startServer() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(port);
        startElectionTime();
        while (true) {
            socket = serverSocket.accept();
            nodesIpList.add(socket.getInetAddress().getHostAddress());
            socketMap.put(socket.getRemoteSocketAddress().toString(), socket);
            socketList.add(socket);
            ++allNodesSize;
            createReadThread();
            createWriteThread();

            LeaderElection leaderElection = new LeaderElection(serverSocket, getConnectedSockets());
            leaderElection.start();
        }
    }

    /**
     * Creates read for getting messages from sender
     */
    private void createReadThread() throws IOException {
        NodeReadThread readThread = new NodeReadThread(socket, this) ;
        readThread.setPriority(Thread.MAX_PRIORITY);
        readThread.start();
    }
}

