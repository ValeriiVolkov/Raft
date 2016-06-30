package raftModels;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.RaftUtils.LEADER_ELECTED;
import static utils.RaftUtils.getCandidate;

/**
 * Created by Valerii Volkov
 */
public class LeaderElection {
    private List<Socket> socketList;
    private Map<String, Follower> followerList;
    private List<String> nodesIpList;
    private int port;

    private int requestVoteMaxAttempts = 3;

    public static boolean isLeaderElected = false;

    /**
     * Constructor adds nodes, which are already within a system
     */
    public LeaderElection(List<Socket> sockets) {
        socketList = new ArrayList<>();
        followerList = new HashMap<>();
        nodesIpList = new ArrayList<>();

        port = sockets.get(0).getPort();
        for (int i = 0; i < sockets.size(); ++i) {
            nodesIpList.add(sockets.get(i).getInetAddress().getHostAddress());
            socketList.add(sockets.get(i));
            Follower follower = new Follower(sockets.get(i).getInetAddress().getHostAddress(), port);
            follower.startElectionTime();
            followerList.put(sockets.get(i).getInetAddress().getHostAddress(), follower);
        }
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("Leader election is started...");
        Candidate candidate = getCandidate(followerList);
        candidate.start();

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
        for (Map.Entry follower : followerList.entrySet()) {
            Follower f = (Follower) follower.getValue();
            if (f.getLog().indexOf(LEADER_ELECTED) != -1) {
                isLeaderElected = true;
                return isLeaderElected;
            }
        }
        return isLeaderElected;
    }
}
