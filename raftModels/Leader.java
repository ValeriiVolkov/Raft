package raftModels;

import utils.LeaderReadThread;
import utils.RaftUtils;
import utils.StringChangeListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by Valerii Volkov on 20.06.2016.
 */
public class Leader extends Candidate {
    private StringChangeListener stringChangeListener;
    private boolean stopped = false;
    private static final int heartbeatTimeout = 0;//time period, between which a leader sends the message to assure that other nodes are alive

    public Leader(String ip, int port) throws IOException, InterruptedException {
        super(ip, port);
    }

    /**
     * Creates socket
     */
    public void start() {
        System.out.println("Leader starts");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            socketList = new HashMap<>();
            nodesIpList = new ArrayList<>();
            while (true) {
                socket = serverSocket.accept();
                nodesIpList.add(socket.getInetAddress().getHostAddress());
                socketList.put(socket.getRemoteSocketAddress().toString(), socket);

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

    protected void createWriteThread() {
        if(socket.isConnected())
        {
            stringChangeListener = new StringChangeListener(log, outStream);
        }
        /*Thread writeThread = new Thread() {
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
        writeThread.start();*/
    }

    /**
     * Handles message about exit from a client
     *
     * @param message
     */
    public boolean handleStop(String message) {
        if (message.toUpperCase().equals(RaftUtils.LEADER_QUITS)) {
            stopped = true;
            return stopped;
        }
        return false;
    }

    public boolean isRun() {
        if (!stopped) {
            return true;
        } else {
            return false;
        }
    }
}
