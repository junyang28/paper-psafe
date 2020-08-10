import java.util.ArrayList;

/*
*   No need to add the header file
*   The main class here servers as a begin for topology collection and ranking different channels
*   main()
*       ChannelConfig()
*           initAllChannels();
 *          new Topology();
 *          run_CR_CP();
 *              new FlowSet();
*
 */

public class ChannelConfig {
    public Topology topology;
    public FlowSet flowset;
    public int min_prr = 96;
    public int min_prr1 = 80;
    public int min_prr2 = 70;
    public int numFlows = 6;
    public int maxChannel;


    public ArrayList<Integer> allChannels;
    public ArrayList<Integer> numNodes;
    public ArrayList<Integer> Degrees;


    public ChannelConfig() {
        initAllChannels();
        this.topology = new Topology("Aug15");          //Clear
        //topology.printTopology();                         //Clear
        this.run_CR_CP();                                   //go below
    }


    public void run_CR_CP() {
        // create flows
        flowset = new FlowSet(numFlows);
        flowset.flows.clear();
        flowset.printFlow();

        // channel filtering
        ChannelFiltering filter = new ChannelFiltering(flowset, topology, min_prr1, 0);
        ArrayList<Integer> filterChannels = filter.getAvailableChannels();
        maxChannel = filterChannels.size();
        System.out.println("Max Channels = " + maxChannel);

        // channel ranking
        Config_CCA cc = new Config_CCA(topology, filterChannels, flowset, min_prr1);
        cc.runCCA();
        ArrayList<Integer> channels = cc.rankChannel;
        cc.printRankChannels(channels);


        for (int i = maxChannel; i >= 2; i--) // find max #channels that allows the flow set to be schedulable
        {
            // Channel Pairing
            flowset.flows.clear();
            Config_DoublePRR cd = new Config_DoublePRR(topology, channels, i, true);
            float[] avgPRR = topology.getAvgPRR(channels, min_prr1);
            cd.splitChannel_both(avgPRR);
            ArrayList<Link> links = cd.getOutputLinks(1); //0-no hop, 1-hop

            // find source route for flows
            Dijkstra dj = new Dijkstra(links, flowset);
            int sourceSucc = dj.findFlowPaths();

            // source rouitng is successful success
            if (sourceSucc == 0) {
                // generate a schedule
                Scheduling sched = new Scheduling(dj.flowset, i);
                boolean succSched = sched.schedule_source2();

                // schedule is succesfully generated
                if (succSched) {
                    System.out.println("---schedule is succesfully created! with " + i + " channels");
                    System.out.println("slot, sender, receiver, channel set, channel offset");

                    // print the schedule
                    for (int j = 0; j < numFlows; j++) {
                        sched.printScheduleByFlow(j);
                    }

                    //ChannelAssignment assign = new ChannelAssignment(sched.schedule);
                    //assign.assignMultiple(cd.firstSet, cd.secondSetUse, cd.oddChannel);
                    break;
                }
            }
        }
    }

    public void initAllChannels() {
        allChannels = new ArrayList<Integer>();
        for (int i = 0; i < 16; i++) {
            allChannels.add(i + 11);
        }
    }

    public void printLinks(ArrayList<Link> links) {
        int totalLinks = links.size();
        for (int i = 0; i < totalLinks; i++) {
            Link l = links.get(i);
            System.out.println(l.source + " " + l.destination);
        }
    }

    public void topologyStat(ArrayList<Link> links, int schedID, int numChannels) {
        ArrayList<Node> res = topology.getDegrees(links);
        String out = "";
        for (int i = 0; i < res.size(); i++) {
            Node n = res.get(i);
            out = out + n.nodeID + ",";
        }
        System.out.println(out);
    }

    public void printNumHop(ArrayList<ArrayList<Integer>> hops, int num) {
        for (int i = 0; i < num; i++) {
            String out = "";
            for (int j = 0; j < 16; j++) {
                ArrayList<Integer> arr = hops.get(j);
                out = out + arr.get(i) + " ";
            }
            System.out.println(out);
        }
    }

    public static void main(String args[]) {
        ChannelConfig cc = new ChannelConfig();
    }
}
