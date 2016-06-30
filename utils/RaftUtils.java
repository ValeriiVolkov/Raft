package utils;

import raftModels.Candidate;
import raftModels.Follower;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class RaftUtils {
    public static final String REQUEST_VOTE = "REQUEST_VOTE";
    public static final String VOTE = "VOTE";
    public static final String LEADER_QUITS = "LEADER_QUITS";
    public static final String LEADER_ELECTED = "LEADER_ELECTED";

    public static final String DELETE_ENTRY = "DEL ||";
    public static final String ADD_ENTRY = "ADD ||";

    /**
     * Returns a proportion (n out of a total) as a percentage, in a float.
     */
    public static float getPercentage(int n, int total) {
        float proportion = ((float) n) / ((float) total);
        return proportion * 100;
    }

    /**
     * Returns a follower with the smaller election timeout
     */
    public static Candidate getCandidate(Map<String, Follower> followers) throws IOException, InterruptedException {
        int[] electionTimeouts = new int[followers.size()];
        int i = 0;
        List<Follower> followersList = followers.entrySet().
                stream().map(Map.Entry::getValue).collect(Collectors.toList());

        for (Map.Entry<String, Follower> follower : followers.entrySet()) {
            electionTimeouts[i++] = follower.getValue().getRemainingElectionTimeout();
        }
        int minTimeIndex = findMinElectionTimeIndex(electionTimeouts);

        return new Candidate(followersList.get(minTimeIndex).getIp(),
                followersList.get(minTimeIndex).getPort());
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
