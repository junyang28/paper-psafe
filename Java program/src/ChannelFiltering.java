import java.util.ArrayList;

public class ChannelFiltering {
    public FlowSet flows;
    public Topology topology;
    public int min_prr;
    public int min_peer;
    ArrayList<Node> nodes;

    public ChannelFiltering(FlowSet fs, Topology top, int prr, int peers) {
        flows = fs;
        topology = top;
        min_prr = prr;
        min_peer = peers;
        nodes = topology.getNumPeers(min_prr);
    }

    public ArrayList<Integer> getAvailableChannels() {
        ArrayList<Integer> channels = new ArrayList<Integer>();
        int[] channelArray = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; // every channel can be used
        int nodeID;

        nodeID = flows.accessPoints;
        channelArray = this.ANDChannel(channelArray, this.findNode(nodeID));

        for (int i = 0; i < flows.numFlows; i++) {
            //System.out.println(flows.getSource(i) +" " +flows.getDestination(i));
            nodeID = flows.getSource(i);
            channelArray = this.ANDChannel(channelArray, this.findNode(nodeID));
            nodeID = flows.getDestination(i);
            channelArray = this.ANDChannel(channelArray, this.findNode(nodeID));
        }
        String out = "-------Available Channels after Filtering ------\n";
        for (int i = 0; i < 16; i++) {
            if (channelArray[i] == 1) {
                int ch = i + 11;
                channels.add(ch);
                out = out + ch + " ";
            }
        }
        System.out.println(out); // print
        return channels;
    }

    public int[] ANDChannel(int[] init, int[] ch) {
        int[] result = init;

        for (int i = 0; i < 16; i++) {
            if (ch[i] <= min_peer) {
                result[i] = 0;
            }
        }
        //this.printANDChannel(init, ch, result); // print
        return result;
    }

    public int[] findNode(int nodeID) {
        int[] peers = new int[16];
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (n.nodeID == nodeID) {
                peers = n.countPeers;
                break;
            }
        }
        return peers;
    }

    public void printANDChannel(int[] init, int[] ch, int[] res) {
        String out1 = "";
        String out2 = "";
        String out3 = "";
        for (int i = 0; i < 16; i++) {
            out1 = out1 + init[i] + " ";
            out2 = out2 + ch[i] + " ";
            out3 = out3 + res[i] + " ";
        }
        System.out.println(out1 + "/" + out2 + "-->" + out3);
    }

}
