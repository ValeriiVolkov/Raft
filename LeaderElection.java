import raftParticipants.Candidate;
import raftParticipants.Follower;
import raftParticipants.Leader;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.LeaderElectionUtils.LEADER_ELECTED;
import static utils.LeaderElectionUtils.getCandidate;

/**
 * Created by Valerii Volkov
 */
public class LeaderElection {
    private Map<String, Socket> socketList;
    private Map<String, Follower> followerList;
    private List<String> nodesIpList;
    private int port;

    private int requestVoteMaxAttempts = 3;

    public static boolean isLeaderElected = false;

    /**
     * Constructor adds nodes, which are already within a system
     */
    public LeaderElection(String[] ips) {
        socketList = new HashMap<>();
        followerList = new HashMap<>();
        nodesIpList = new ArrayList<>();

        port = Integer.valueOf(ips[0]);
        for (int i = 2; i < ips.length; ++i) {
            nodesIpList.add(ips[i]);
            try {
                followerList.put(ips[i], new Follower(ips[i], port));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("Leader election is started...");
        Candidate candidate = getCandidate(followerList);

        for (int i = 0; i < requestVoteMaxAttempts; ++i) {
            if (candidate.isLeader()) {
                break;
            } else {
                candidate.requestVote();
            }
        }

        //TODO If a leader is not elected
        if (!candidate.isLeader()) {
            System.out.println("Leader is not elected. Please, re-run the system");
            System.exit(0);
        }

        //Leader is elected
        Leader leader = candidate.toLeader();
        candidate.close();
        leader.start();
    }

    public boolean isLeaderElected() {
        for(Map.Entry follower : followerList.entrySet())
        {
            Follower f = (Follower) follower.getValue();
            if(f.getLog().indexOf(LEADER_ELECTED) != -1)
            {
                isLeaderElected = true;
                return isLeaderElected;
            }
        }
        return isLeaderElected;
    }
}
