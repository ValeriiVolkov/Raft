import raftModels.Follower;

/**
 * Created by Valerii Volkov
 */
public class Main {
    private String nameOfContainer;
    private int port;

    public static void main(String[] args) throws Exception {
        Follower follower;
        switch (args.length) {
            case 0:
                System.out.println("At least one parameter: for port is needed");
                System.out.println("OR");
                System.out.println("two parameters: 1st - for port, 2nd-... - for ips are needed");
                break;
            case 1:
                follower = new Follower(Integer.valueOf(args[0]));
                follower.start();
                break;
            case 2:
                follower = new Follower(args[1], Integer.valueOf(args[0]));
                follower.start();
                break;
            default:
                follower = new Follower(args[1], Integer.valueOf(args[0]));
                follower.start();
                break;
        }
    }
}
