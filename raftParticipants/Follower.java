package raftParticipants;

import models.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import static java.lang.Thread.sleep;

/**
 * Created by Valerii Volkov on 20.06.2016.
 */
public class Follower extends Node {
    public Follower(String ip, int port) throws IOException, InterruptedException {
        super(ip, port);
        createSocket();
    }

    public void createSocket() throws IOException, InterruptedException {
        socket = new Socket(ip, port);
        inStream = socket.getInputStream();
        outStream = socket.getOutputStream();
        createReadThread();
        createWriteThread();
    }

    /**
     * Creates read for getting messages from sender
     */
    public void createReadThread() {
        while (socket.isConnected()) {
            try {
                byte[] readBuffer = new byte[SIZE_OF_BUFFER];
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

    /**
     * Creates write for sending messages to receiver
     */
    public void createWriteThread() {
        while (socket.isConnected()) {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                sleep(SLEEP_TIME);
                String inputMessage = inputReader.readLine();
                if (inputMessage != null && inputMessage.length() > 0) {
                    synchronized (socket) {
                        outStream.write(inputMessage.getBytes(CHARSET));
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
}
