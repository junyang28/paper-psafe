import java.util.ArrayList;


public class Config_CCA {
    public ArrayList<Integer> rankChannel;
    public ArrayList<Integer> availChannels;
    public ArrayList<Node> nodes;
    public FlowSet flows;
    public Topology topology;
    public int min_prr;
    public int min_peer = 0;
    public int min_channel = 9;
    public int min_deg = 3;

    double[] scores = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    public Config_CCA(Topology top, ArrayList<Integer> ac, FlowSet fs, int prr) {
        topology = top;
        availChannels = ac;
        min_prr = prr;
        flows = fs;
        nodes = new ArrayList<Node>();
        rankChannel = new ArrayList<Integer>();
    }

    public void runCCA() // step 0
    {
        this.filterNode();
        //System.out.println("Number of Nodes: " +nodes.size());
        double[] degree = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        for (int i = 0; i < availChannels.size(); i++) // get average degree of each channel
        {
            int ch = availChannels.get(i);
            degree[ch - 11] = this.getAverageDegree(ch);
        }
        this.printArrayDouble(degree);
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            int count = this.countGoodChannel(n, degree);
            nodes.get(i).updateGoodChannel(count);
            double[] normDeg = this.normalizedDegree(n);
            nodes.get(i).normDegree = normDeg;
        }
        //this.printNodeTable(nodes);
        for (int i = 0; i < availChannels.size(); i++) // get average degree of each channel
        {
            int ch = availChannels.get(i);
            scores[ch - 11] = this.calculateScore(ch);
        }
        //this.printArrayDouble(scores);
        this.getRankChannel(scores); // output is in rankChannel
        //this.printRankChannels(rankChannel);

    }

    public void filterNode() // step 1
    {
        ArrayList<Node> tempNode = topology.getNumPeers(min_prr);
        //topology.printPeerCount(tempNode);
        for (int i = 0; i < tempNode.size(); i++) {
            Node n = tempNode.get(i);
            boolean keyNode = this.checkIfKeyNode(n.nodeID);
            if (keyNode)
                nodes.add(n);
            else {
                int numChannels = this.countActiveChannels(n.countPeers);
                if (numChannels >= min_channel) {
                    nodes.add(n);
                } else {
                    //System.out.println(n.nodeID +" " +numChannels);
                }
            }
        }
    }

    public boolean checkIfKeyNode(int nodeID) // 1.1
    {

        if (nodeID == flows.accessPoints)
            return true;

        for (int i = 0; i < flows.sources.size(); i++) {
            if (flows.sources.get(i) == nodeID || flows.destinations.get(i) == nodeID)
                return true;
        }
        return false;
    }

    public int countActiveChannels(int[] peers) // 1.2
    {
        int count = 0;
        for (int i = 0; i < peers.length; i++) {
            if (peers[i] > min_peer)
                count++;
        }
        return count;
    }

    public double getAverageDegree(int channel) // step 2
    {
        double sum = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            sum = sum + (double) n.countPeers[channel - 11];
        }
        return sum / (double) nodes.size();
    }

    public int countGoodChannel(Node n, double[] deg) // step 3
    {
        int count = 0;
        for (int i = 0; i < availChannels.size(); i++) {
            int ch = availChannels.get(i);
            //if(this.checkIfKeyNode(n.nodeID))
            //	return 0;
            if (n.countPeers[ch - 11] > deg[ch - 11] && n.countPeers[ch - 11] >= min_deg)
                count++;
        }
        return count;
    }

    public double[] normalizedDegree(Node n) // step 4
    {
        int maxDeg = this.findMaxDegree(n);
        double[] norm = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        for (int i = 0; i < availChannels.size(); i++) {
            int ch = availChannels.get(i);
            norm[ch - 11] = (double) n.countPeers[ch - 11] / (double) maxDeg;
        }
        return norm;
    }

    public int findMaxDegree(Node n) // 4.1
    {
        int max = 0;
        for (int i = 0; i < availChannels.size(); i++) {
            int ch = availChannels.get(i);
            if (n.countPeers[ch - 11] > max)
                max = n.countPeers[ch - 11];
        }
        return max;
    }

    public double calculateScore(int channel) // step 5
    {
        double score = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (n.goodChannels == 0) // no good channels at all
                score = score + (n.normDegree[channel - 11]);
            else
                score = score + (n.normDegree[channel - 11] * (double) (1 / n.goodChannels));
        }
        return score;
    }

    public void getRankChannel(double[] s) //step 6
    {
        for (int i = 0; i < s.length; i++) {
            if (s[i] >= 0) {
                int maxIndex = this.findMax(s);
                rankChannel.add(maxIndex + 11);
                s[maxIndex] = 0;
            }
        }
    }

    public int findMax(double[] val) // 6.1
    {
        double max = 0;
        int maxIndex = -1;
        for (int i = 0; i < val.length; i++) {
            if (val[i] > max) {
                max = val[i];
                maxIndex = i;
            }
        }
        //System.out.println(max +" " +(maxIndex+11));
        return maxIndex;
    }

    public void printArrayDouble(double[] arr) {
        String out = "";
        for (int i = 0; i < arr.length; i++) {
            out = out + arr[i] + " ";
        }
        System.out.println(out);
    }

    public void printArrayInt(int[] arr) {
        String out = "";
        for (int i = 0; i < arr.length; i++) {
            out = out + arr[i] + " ";
        }
        System.out.println(out);
    }

    public void printRankChannels(ArrayList<Integer> values) // 1.3
    {
        String out = "-----------Rank channel----------\n";
        for (int i = 0; i < values.size(); i++) {
            out = out + " " + values.get(i);
        }
        System.out.println(out);
    }

    public void printNodeTable(ArrayList<Node> nArray) {
        String out = "--------------------Node Table-----------------------\n";
        for (int i = 0; i < nArray.size(); i++) {
            Node n = nArray.get(i);
            out = out + n.nodeID + ":" + n.getPeerCount() + "/" + n.getNormDegree() + "--" + n.goodChannels + "\n";
        }
        System.out.println(out);
    }

}

