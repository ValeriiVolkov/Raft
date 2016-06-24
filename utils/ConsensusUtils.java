package utils;

/**
 * Created by Valerii Volkov on 22.06.2016.
 */
public class ConsensusUtils {
    public static final String REQUEST_VOTE = "REQUEST_VOTE";
    public static final String VOTE = "VOTE";
    public static final String LEADER_QUITS = "LEADER_QUITS";

    /**
     * Returns a proportion (n out of a total) as a percentage, in a float.
     */
    public static float getPercentage(int n, int total) {
        float proportion = ((float) n) / ((float) total);
        return proportion * 100;
    }
}
