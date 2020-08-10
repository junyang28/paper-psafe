import java.util.ArrayList;

public class Node {
    public int nodeID;
    public int[] countPeers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public double[] normDegree = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public int goodChannels;
    public ArrayList<Integer> neighbors;

    public Node(int id) {
        nodeID = id;
        countPeers = new int[16];
        neighbors = new ArrayList<Integer>();
    }

    public void updatePeerCount(int channel, int count) {
        countPeers[channel - 11] = count;
    }

    public void updateNormDegree(int channel, double norm) {
        normDegree[channel - 11] = norm;
    }

    public void updateGoodChannel(int num) {
        this.goodChannels = num;
    }

    public String getPeerCount() {
        String out = "";
        for (int i = 0; i < countPeers.length; i++) {
            out = out + countPeers[i] + " ";
        }
        return out;
    }

    public String getNormDegree() {
        String out = "";
        for (int i = 0; i < normDegree.length; i++) {
            out = out + normDegree[i] + " ";
        }
        return out;
    }
}
