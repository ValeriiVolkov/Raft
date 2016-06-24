import raftParticipants.Follower;

/**
 * Created by Valerii Volkov
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
            System.out.println("Two parameters: 1st - for port, 2nd-... - for ips are needed");
        } else {
            try {
                Follower node = new Follower(args[1], Integer.valueOf(args[0]));
                node.start();

                LeaderElection leaderElection;
                //If there are already other nodes within the system then
                if (args.length > 2) {
                    leaderElection = new LeaderElection(args);
                    leaderElection.start();
                }
            } catch (NumberFormatException e) {
                System.out.println("Wrong input for the port");
            }
        }
    }
}
