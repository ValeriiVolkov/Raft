package raftModels;

import java.io.IOException;

/**
 * Created by Valerii Volkov on 20.06.2016.
 */

public class Leader extends Node {
    private boolean stopped = false;
    private static final int heartbeatTimeout = 0;//time period, between which a leader sends the message to assure that other nodes are alive

    public Leader(){}

    /**
     * Creates socket
     */
    public void start() throws InterruptedException, IOException {
        System.out.println("Leader starts...");
    }

    public boolean isRun() {
        if (!stopped) {
            return true;
        } else {
            return false;
        }
    }
}
