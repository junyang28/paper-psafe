import java.util.ArrayList;


public class Config_DoublePRR {
    public ArrayList<Integer> rankChannel;
    public ArrayList<Integer> firstSet;
    public ArrayList<Integer> firstSetLink;
    public ArrayList<Integer> secondSet;
    public ArrayList<Integer> secondSetUse;
    public ArrayList<Link> links;

    public Topology topology;
    public int min_prr1 = 90;
    public int min_prr2 = 70;
    public double min_prr = 0.99;
    public int min_hop = 4;
    public int totalChannels;
    public boolean isSource;
    public int oddChannel;

    public Config_DoublePRR(Topology top, ArrayList<Integer> rank, int numChannel, boolean isSource) {
        topology = top;
        totalChannels = numChannel;
        firstSet = new ArrayList<Integer>();
        firstSetLink = new ArrayList<Integer>();
        secondSet = new ArrayList<Integer>();
        secondSetUse = new ArrayList<Integer>();
        rankChannel = rank;
        this.isSource = isSource;
        System.out.println("=======" + numChannel + "=======");
    }

    public void splitChannel(float[] avgPRR) // step 1
    {
        oddChannel = 0;
        ArrayList<Integer> tempFirst = new ArrayList<Integer>();
        if (isSource) {
            int start1 = (totalChannels / 2) - 1;
            int start2 = totalChannels / 2;

            for (int i = start1; i >= 0; i--) {
                int ch = rankChannel.get(i);
                tempFirst.add(ch);
                firstSetLink.add(ch);
            }
            firstSet = this.rankByAvgPRR(avgPRR, tempFirst); // 1.1
            for (int j = start2; j < rankChannel.size(); j++) {
                int ch = rankChannel.get(j);
                secondSet.add(ch);
            }
            this.printSplitting();
        }
    }

    public void splitChannel_both(float[] avgPRR) {
        if (totalChannels % 2 == 0) // even no. of channels
        {
            this.splitChannel(avgPRR);
        } else // odd number of channels
        {
            ArrayList<Integer> tempFirst = new ArrayList<Integer>();
            int start1 = (totalChannels / 2) - 1;
            int start2 = (totalChannels / 2) + 1;
            for (int i = start1; i >= 0; i--) {
                int ch = rankChannel.get(i);
                tempFirst.add(ch);
            }
            oddChannel = rankChannel.get(start1 + 1);
            firstSet = this.rankByAvgPRR(avgPRR, tempFirst); // 1.1
            for (int i = (start1 + 1); i >= 0; i--) {
                int ch = rankChannel.get(i);
                firstSetLink.add(ch);
            }
            for (int j = start2; j < rankChannel.size(); j++) {
                int ch = rankChannel.get(j);
                secondSet.add(ch);
            }
            this.printSplitting();
        }
    }

    public ArrayList<Integer> rankByAvgPRR(float[] avgPRR, ArrayList<Integer> chs) // 1.1
    {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < avgPRR.length; i++) // consider only channel in top list
        {
            boolean flag = false;
            for (int j = 0; j < chs.size(); j++) {
                if (i == (chs.get(j) - 11))
                    flag = true;
            }
            if (!flag)
                avgPRR[i] = -1;
        }
        for (int i = 0; i < chs.size(); i++) {
            int ch = chs.get(i);
            if (avgPRR[ch - 11] != -1) {
                int minIndex = this.findMin(avgPRR); //1.1.1
                result.add(minIndex + 11);
                avgPRR[minIndex] = 101;
            }
        }
        return result;
    }

    public int findMin(float[] val) // 1.1.1
    {
        float min = 101;
        int minIndex = -1;
        for (int i = 0; i < val.length; i++) {
            if (val[i] != -1) {
                if (val[i] < min) {
                    min = val[i];
                    minIndex = i;
                }
            }
        }
        return minIndex;
    }

    public void printSplitting() {
        String out1 = "first set:";
        String out2 = "second set:";
        String out3 = "third set";
        for (int i = 0; i < firstSet.size(); i++) {
            out1 = out1 + " " + firstSet.get(i);
        }
        for (int i = 0; i < secondSet.size(); i++) {
            out2 = out2 + " " + secondSet.get(i);
        }
        if (oddChannel > 0)
            out3 = out3 + " " + oddChannel;
        //System.out.println("---------splitting channels--------");
        //System.out.println(out1 +" -- " +out2 +" -- " +out3);
    }

    public ArrayList<Link> getOutputLinks(int type) // step 2
    {
        links = topology.getLinks(firstSetLink, min_prr1); // get links of good channels
        //System.out.println("totalLink first set " +links.size());
        this.matchChannel(type); // match with poorer channels
        this.printMatchChannel();
        // get final set of links
        ArrayList<Link> outputLinks = new ArrayList<Link>();
        for (int i = 0; i < links.size(); i++) {
            Link l = links.get(i);
            boolean flag = true;
            for (int j = 0; j < secondSetUse.size(); j++) {
                int ch2 = secondSetUse.get(j);
                if (l.channels[ch2 - 11] == 0) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                outputLinks.add(l);
            }
        }
        //System.out.println("final total links " +outputLinks.size());
        return outputLinks;
    }

    public void matchChannel(int type) //step 2.1
    {
        int[] flag = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < firstSet.size(); i++) {
            int ch1 = firstSet.get(i);
            int[] result;

            result = this.findMatchChannel(ch1, flag, type);

            int matCh = result[0];
            int num = result[1];
            flag[matCh - 11] = 1;
            flag[ch1 - 11] = 1;
            secondSetUse.add(matCh);
            //System.out.println("-->" +ch1 +" " +matCh +" " +num);
        }
    }

    public void printMatchChannel() {
        String out1 = "first set:";
        String out2 = "second set:";
        String out3 = "third set";
        for (int i = 0; i < firstSet.size(); i++) {
            out1 = out1 + " " + firstSet.get(i);
        }
        for (int i = 0; i < secondSetUse.size(); i++) {
            out2 = out2 + " " + secondSetUse.get(i);
        }
        if (oddChannel > 0)
            out3 = out3 + " " + oddChannel;
        System.out.println("---------set of selected channels--------");
        System.out.println(out1 + " -- " + out2 + " -- " + out3);
    }

    public int[] findMatchChannel(int ch1, int[] selectFlag, int type) // 2.1.1
    {
        int[] output = new int[2];
        int bestChannel = -1;
        int maxLink = 0;

        ArrayList<Integer> secondFilter = new ArrayList<Integer>();
        if (type == 1) // include hop
            secondFilter = this.hopChannel(ch1, selectFlag);
        else
            secondFilter = secondSet;

        for (int i = 0; i < secondFilter.size(); i++) // for each channel is the second set
        {
            int ch2 = secondFilter.get(i);
            if (selectFlag[ch2 - 11] != 1) // channel hasn't been selected yet
            {
                int count = 0;
                for (int j = 0; j < links.size(); j++) {
                    Link l = links.get(j);
                    int s = l.source;
                    int d = l.destination;
                    boolean flag1 = topology.checkBidirectional(s, d, ch1, min_prr1);
                    boolean flag2 = topology.checkBidirectional(s, d, ch2, min_prr2);
                    if (flag1 && flag2) //usable links for an input channel
                    {
                        double prob1 = topology.topology[s][d][ch1 - 11] / 100.0;
                        double prob2 = topology.topology[s][d][ch2 - 11] / 100.0;
                        if (prob2 >= (min_prr - prob1) / (1 - prob1)) {
                            count++;
                            links.get(j).setChannel_1(ch1);
                            links.get(j).setChannel_1(ch2);
                        }
                    }
                } // end for each links
                if (count > maxLink) {
                    maxLink = count;
                    bestChannel = ch2;

                }
                //System.out.println(ch1 +" " +ch2 +" " +count);
            } // end unselected channels
        }
        output[0] = bestChannel;
        output[1] = maxLink;
        return output;
    }

    public ArrayList<Integer> hopChannel(int ch1, int[] selectFlag) //2.1.1.1 - candidate channel is x hop away
    {
        ArrayList<Integer> output = new ArrayList<Integer>();
        int max_hop = 0;
        int max_channel = -1;
        for (int i = 0; i < secondSet.size(); i++) {
            int ch2 = secondSet.get(i);
            int chDist = Math.abs(ch1 - ch2);
            if (selectFlag[ch2 - 11] != 1) {
                if (chDist >= min_hop) {
                    output.add(ch2);
                }
                if (chDist > max_hop) {
                    max_hop = chDist;
                    max_channel = ch2;
                }
            }
        }
        if (output.size() == 0)
            output.add(max_channel);
        return output;
    }


}
