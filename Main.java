import raftParticipants.Candidate;
import raftParticipants.Leader;

import static utils.ConsensusUtils.getPercentage;

/**
 * Created by Valerii Volkov on 23.06.2016.
 */
public class Main {
    private String nameOfContainer;
    private int port;

    public Main(String nameOfContainer, int port) {
        this.nameOfContainer = nameOfContainer;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Two parameters: 1st - for ip, 2nd - for port are needed");
        } else {
            try {
                Candidate candidate = new Candidate(args[0], Integer.valueOf(args[1]));

                while (getPercentage(candidate.getAcceptedNodesSize(), candidate.getAllNodesSize()) < 0.5f)
                {
                    candidate.requestVote();
                }

                Leader leader = new Leader(args[0], Integer.valueOf(args[1]));
            } catch (NumberFormatException e) {
                System.out.println("Wrong input for the port");
            }
        }
    }
}
