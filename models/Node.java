package models;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class Node {
    protected static final int electionTimeout = 0;//time a follower waits until becoming a candidate
    protected static final int heartbeatTimeout = 0;

    protected Socket socket;
    protected InputStream inStream;
    protected OutputStream outStream;

    protected static final int SLEEP_TIME = 1000;
    protected static final int SIZE_OF_BUFFER = 100;
    protected static final String CHARSET = "UTF-8";
    protected Logger LOGGER = Logger.getLogger("Node");

    protected String ip;
    protected int port;

    private StringBuilder log;

    public Node(String ip, int port) throws IOException, InterruptedException {
        log = new StringBuilder();

        this.ip = ip;
        this.port = port;
    }

    public void receiveMessage(String s) {
        log.append(s);
    }
}
