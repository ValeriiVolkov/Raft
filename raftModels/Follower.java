package raftModels;

import utils.StringChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Valerii Volkov on 20.06.2016.
 */
public class Follower extends Node {
    private ServerSocket serverSocket;
    private StringChangeListener stringChangeListener;

    public Follower(String ip, int port) {
        super(ip, port);
    }

    public Follower(int port) {
        super(port);
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("Follower starts...");
        socketList = new HashMap<>();
        nodesIpList = new ArrayList<>();
        if (ip == null) {
            startServer();
            return;
        }

        socket = new Socket(ip, port);
        inStream = socket.getInputStream();
        outStream = socket.getOutputStream();

        //Send log file after a first connection
        outStream.write(getLog().toString().getBytes(CHARSET));

        createReadThread();
        createWriteThread();

        startElectionTime();
    }

    public void startServer() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(port);
        startElectionTime();
        while (true) {
            socket = serverSocket.accept();
            nodesIpList.add(socket.getInetAddress().getHostAddress());
            socketList.put(socket.getRemoteSocketAddress().toString(), socket);
            createReadThread();
            createWriteThread();

            LeaderElection leaderElection = new LeaderElection(getConnectedSockets());
            leaderElection.start();
        }
    }

    /**
     * Creates read for getting messages from sender
     */
    private void createReadThread() throws IOException {
        Thread readThread = new Thread() {
            public void run() {
                while (socket.isConnected()) {
                    try {
                        byte[] readBuffer = new byte[SIZE_OF_BUFFER];
                        InputStream inStream = socket.getInputStream();
                        int size = inStream.read(readBuffer);
                        if (size > 0) {
                            byte[] arrayBytes = new byte[size];
                            System.arraycopy(readBuffer, 0, arrayBytes, 0, size);
                            String receivedMessage = new String(arrayBytes, CHARSET);
                            receiveMessage(receivedMessage);
                        }
                    } catch (SocketException se) {
                        LOGGER.log(Level.SEVERE, "Socket Exception", se);
                        System.exit(0);
                    } catch (IOException ie) {
                        LOGGER.log(Level.SEVERE, "IO Exception", ie);
                    }
                }
            }
        };
        readThread.setPriority(Thread.MAX_PRIORITY);
        readThread.start();
    }

    /**
     * Creates write for sending messages to receiver
     */
    private void createWriteThread() {
        if (socket.isConnected()) {
            stringChangeListener = new StringChangeListener(log, outStream);
        }

        /*while (socket.isConnected()) {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                sleep(SLEEP_TIME);
                String inputMessage = inputReader.readLine();
                if (inputMessage != null && inputMessage.length() > 0) {
                    synchronized (inputMessage) {
                        outStream.write(inputMessage.getBytes(CHARSET));
                        sleep(SLEEP_TIME);
                    }
                }
            } catch (IOException ie) {
                LOGGER.log(Level.SEVERE, "IO Exception", ie);
            } catch (InterruptedException ie) {
                LOGGER.log(Level.SEVERE, "Interrupted Exception", ie);
            }
        }*/
    }

    /**
     * Makes election time start
     */
    public void startElectionTime() {
        Thread electionThread = new Thread() {
            public void run() {
                for (int i = 0; i < electionTimeout; ++i) {
                    try {
                        //To prevent split votes in the first place, election timeouts are
                        //chosen randomly from a fixed interval (e.g., 150–300ms).
                        sleep(100);
                        reduceRemainingElectionTimeout();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        electionThread.start();
    }

    public List<Socket> getConnectedSockets() {
        return new ArrayList<>(socketList.values());
    }
}

