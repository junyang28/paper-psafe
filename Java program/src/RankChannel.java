import java.util.ArrayList;
import java.util.Collections;

public class RankChannel {
    public ArrayList<Integer> availChannels;

    public Topology topology;
    public int min_prr1;
    public int totalChannels;

    int[] numLinks = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    float[] averagePRR = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    public int rankType;


    public RankChannel(ArrayList<Integer> ac, Topology top, int prr) {
        availChannels = ac;
        topology = top;
        min_prr1 = prr;
    }

    public ArrayList<Integer> ranking_numLinks() // step 1
    {
        int[] numLinks = this.countLinks();
        ArrayList<Integer> rankChannel = this.rankByNumLinks(numLinks);
        this.printRankChannels(rankChannel);
        return rankChannel;
    }

    public int[] countLinks() // 1.1
    {

        for (int i = 0; i < availChannels.size(); i++) {
            int ch = availChannels.get(i);
            numLinks[ch - 11] = 0;
            averagePRR[ch - 11] = 0;
            ArrayList<Integer> linkPRR = new ArrayList<Integer>();

            for (int j = 0; j < topology.sources.size(); j++) {
                int s = topology.sources.get(j);
                int d = topology.destinations.get(j);
                if (this.topology.checkBidirectional(s, d, ch, min_prr1)) // a link is bidirectional
                {
                    linkPRR.add(topology.topology[s][d][ch - 11]);
                }
            }
            numLinks[ch - 11] = linkPRR.size();
            averagePRR[ch - 11] = this.findAvg(linkPRR);

            String out = ch + ":" + numLinks[ch - 11] + " " + averagePRR[ch - 11] + " " + this.findMedian(linkPRR);
            System.out.println(out);
        }
        return numLinks;
    }

    public ArrayList<Integer> rankByNumLinks(int[] numlinks) //1.2
    {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < numlinks.length; i++) {
            if (numlinks[i] >= 0) {
                int maxIndex = this.findMax(numlinks);
                result.add(maxIndex + 11);
                numlinks[maxIndex] = 0;
            }
        }
        return result;
    }

    public void printRankChannels(ArrayList<Integer> values) // 1.3
    {
        String out = "Rank channel:";
        for (int i = 0; i < values.size(); i++) {
            out = out + " " + values.get(i);
        }
        System.out.println(out);
    }

    public int findMax(int[] val) {
        int max = 0;
        int maxIndex = -1;
        for (int i = 0; i < val.length; i++) {
            if (val[i] > max) {
                max = val[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public int findAvg(ArrayList<Integer> values) // 1.1.1
    {
        int sum = 0;
        for (int i = 0; i < values.size(); i++) {
            sum = sum + values.get(i);
        }
        return sum / values.size();
    }

    public int findMedian(ArrayList<Integer> values) {
        Collections.sort(values);
        if (values.size() % 2 == 1)
            return values.get((values.size() + 1) / 2 - 1);
        else {
            int lower = values.get(values.size() / 2 - 1);
            int upper = values.get(values.size() / 2);

            return (lower + upper) / 2;
        }
    }

}
