package raftModels;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static utils.RaftUtils.LEADER_ELECTED;
import static utils.RaftUtils.getCandidate;

/**
 * Created by Valerii Volkov
 */
public class LeaderElection {
    private List<Socket> socketList;
    private List<Follower> followerList;
    private List<String> nodesIpList;
    private int port;

    private int requestVoteMaxAttempts = 3;
    private ServerSocket serverSocket;

    public static boolean isLeaderElected = false;

    /**
     * Constructor adds nodes, which are already within a system
     */
    public LeaderElection(ServerSocket serverSocket, List<Socket> sockets) {
        this.socketList = sockets;
        this.serverSocket = serverSocket;

        port = sockets.get(0).getPort();
        socketList = sockets;

        followerList = new ArrayList<>();
        for(Socket s : sockets)
        {
            Follower f = new Follower(s.getInetAddress().getHostAddress(), port);
            f.startElectionTime();
            followerList.add(f);
        }
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("Leader election is started...");

        Follower mainFollower = new Follower(serverSocket.getInetAddress().getHostAddress(),
                port);
        mainFollower.setSocketList(socketList);
        mainFollower.startElectionTime();
        Candidate candidate = getCandidate(mainFollower, followerList);
        candidate.start();

        for (int i = 0; i < requestVoteMaxAttempts; ++i) {
            if (candidate.isLeader()) {
                break;
            } else {
                Thread.sleep(500);
                candidate.requestVote();
            }
        }
        Thread.sleep(4000);
        //TODO If a leader is not elected
        if (!candidate.isLeader()) {
            System.out.println("Leader is not elected. Please, re-run the system");
            System.exit(0);
        }

        //Leader is elected
        Leader leader = candidate.toLeader();
        leader.setSocketList(candidate.getConnectedSockets());
        leader.start();

        System.out.println("Leader is elected. Please write the " +
                "input for the document and press ENTER to commit it");
    }

    public boolean isLeaderElected() {
        for (Follower f : followerList) {
            if (f.getLog().indexOf(LEADER_ELECTED) != -1) {
                isLeaderElected = true;
                return isLeaderElected;
            }
        }
        return isLeaderElected;
    }
}
