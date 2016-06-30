package utils;

import raftModels.Candidate;
import raftModels.Follower;

import java.io.IOException;
import java.util.List;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class RaftUtils {
    public static final String REQUEST_VOTE = "REQUEST_VOTE";
    public static final String VOTE = "Vote is processed";
    public static final String LEADER_QUITS = "LEADER_QUITS";
    public static final String LEADER_ELECTED = "Leader is elected";
    public static final String SHOW_LOG = "LOG";

    public static final String DELETE_ENTRY = "DEL ||";
    public static final String ADD_ENTRY = "ADD ||";

    /**
     * Returns a proportion (n out of a total) as a percentage, in a float.
     */
    public static float getPercentage(int n, int total) {
        if (total == 0) {
            return 100;
        }

        float proportion = ((float) n) / ((float) total);
        return proportion * 100;
    }

    /**
     * Returns a follower with the smaller election timeout
     */
    public static Candidate getCandidate(Follower mainFollower, List<Follower> followersList) throws IOException, InterruptedException {
        int[] electionTimeouts = new int[followersList.size() + 1];

        electionTimeouts[0] = mainFollower.getRemainingElectionTimeout();
        int i = 1;
        for (Follower follower : followersList) {
            electionTimeouts[i++] = follower.getRemainingElectionTimeout();
        }
        int minTimeIndex = findMinElectionTimeIndex(electionTimeouts);

        if (followersList.get(minTimeIndex).getConnectedSockets().isEmpty()) {
            return new Candidate(mainFollower.getConnectedSockets());
        }

        return new Candidate(followersList.get(minTimeIndex).getConnectedSockets());
    }

    /**
     * Returns an index of the node with smallest election timeout
     */
    public static int findMinElectionTimeIndex(int[] array) {
        int index = 0;
        int min = array[index];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
                index = i;
            }
        }
        return index;
    }
}
