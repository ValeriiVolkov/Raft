package communicationThreads;

import java.util.Random;

/**
 * Created by Valerii Volkov on 30.06.2016.
 *
 * Election thread starts election timeout for each node within a system
 */
public class ElectionThread extends Thread {
    private int electionTimeout;//time a follower waits until becoming a candidate
    private volatile Integer remainingElectionTimeout = electionTimeout;
    private static final int SLEEP_TIME = 1000;

    public ElectionThread() {
        Random random = new Random();
        electionTimeout = random.nextInt(30);
    }

    /**
     * Runs a thread, which processed election timeout
     */
    public void run() {
        for (int i = 0; i < electionTimeout; ++i) {
            try {
                //To prevent split votes in the first place, election timeouts are
                //chosen randomly from a fixed interval (e.g., 150–300ms).
                sleep(SLEEP_TIME);
                synchronized (remainingElectionTimeout) {
                    reduceRemainingElectionTimeout();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns remaining election timeout for a node
     * @return
     */
    public Integer getRemainingTime() {
        return remainingElectionTimeout;
    }

    /**
     * Reduces election timeout and returns it to initial value if it is over
     */
    public void reduceRemainingElectionTimeout() {
        if (remainingElectionTimeout == 0) {
            remainingElectionTimeout = electionTimeout;
        }
        remainingElectionTimeout--;
    }
}
