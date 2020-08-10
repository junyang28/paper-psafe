import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class Topology {
    public Connection conn;
    public String dbName;
    public int[][][] topology = new int[200][200][16];
    public ArrayList<Integer> sources;
    public ArrayList<Integer> destinations;
    public ArrayList<Integer> disSources;

    public Topology(String db) {
        dbName = db;
        sources = new ArrayList<Integer>();
        destinations = new ArrayList<Integer>();
        disSources = new ArrayList<Integer>();

        this.initTopology();
        this.connectDB();
        this.readTopology();
        this.getDistinctSources();
    }

    public void initTopology() {
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                for (int k = 0; k < 16; k++) {
                    topology[i][j][k] = 0;
                }
            }
        }
    }

    public void connectDB() {
        conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:profile.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public void readTopology() {
        try {
            String sql = "select distinct src, dest from " + dbName + " order by src, dest";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int s = rs.getInt(1); // source
                int d = rs.getInt(2); // dest
                for (int i = 0; i < 16; i++) {
                    topology[s][d][i] = 0;
                }
                readChannel(s, d);
                sources.add(s);
                destinations.add(d);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
        }
    }

    public void readChannel(int s, int d) {
        try {
            String sql = "select * from " + dbName + " where src = " + s + " and dest= " + d;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int prob = rs.getInt(3); // prr
                int ch = rs.getInt(4); // channel
                int index = ch - 11;
                topology[s][d][index] = prob;
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
        }
    }

    public void getDistinctSources() {
        int prevSource = 0;
        for (int i = 0; i < sources.size(); i++) {
            int s = sources.get(i);
            if (prevSource != s) {
                disSources.add(s);
                prevSource = s;
            }
        }
    }

    public void printTopology() {
        String out = "------ Raw Topology -------\n";
        for (int i = 0; i < sources.size(); i++) {
            int s = sources.get(i);
            int d = destinations.get(i);
            out = out + s + " " + d;
            for (int j = 0; j < 16; j++) {
                out = out + " " + topology[s][d][j];
            }
            out = out + "\n";
        }
        System.out.println(out);
    }

    public boolean checkBidirectional(int n1, int n2, int ch, int prob) {
        if (topology[n2][n1][ch - 11] >= prob && topology[n1][n2][ch - 11] >= prob) {
            //System.out.println(n1 +" " +n2 +" " +ch +" "+topology[n1][n2][ch-11] +" " +topology[n2][n1][ch-11]);
            return true;
        }
        return false;
    }

    public boolean checkUnidirectional(int n1, int n2, int ch, int prob) {
        if (topology[n1][n2][ch - 11] >= prob)
            return true;
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    public ArrayList<Node> getNumPeers(int prr) // get list of nodes and their no. of peers
    {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < disSources.size(); i++) // for each source
        {
            Node n = new Node(disSources.get(i));
            for (int j = 11; j <= 26; j++) // for each channel
            {
                ArrayList<Integer> dests = this.getDestinations(disSources.get(i));
                int numPeers = this.countPeers(disSources.get(i), j, prr, dests);
                n.updatePeerCount(j, numPeers);
            }
            nodes.add(n);
        }
        return nodes;
    }

    public int countPeers(int source, int channel, int prr, ArrayList<Integer> dests) // count no. of neighbors of each channel
    {
        int count = 0;
        for (int i = 0; i < dests.size(); i++) {
            int dest = dests.get(i);
            if (topology[source][dest][channel - 11] > 0) // this destination exist
            {
                if (checkBidirectional(source, dest, channel, prr)) {
                    count++;
                    //System.out.println(source +" " +dest +" " +channel);
                }
            }
        }
        return count;
    }

    public ArrayList<Integer> getDestinations(int src) {
        ArrayList<Integer> dests = new ArrayList<Integer>();
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i) == src)
                dests.add(destinations.get(i));
        }
        return dests;
    }

    public void printPeerCount(ArrayList<Node> nodes) {
        String out = "-------Peer Counts-------\n";
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            //out = out +n.nodeID +"\n";
            out = out + n.nodeID + " " + n.getPeerCount() + "\n";
        }
        System.out.println(out);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    public ArrayList<Link> getLinks(ArrayList<Integer> channels, int prr) // get links, given prr in all channels
    {
        ArrayList<Link> links = new ArrayList<Link>();
        for (int i = 0; i < sources.size(); i++) {
            boolean flag = true;
            int s = sources.get(i);
            int d = destinations.get(i);
            int[] channelArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (int j = 0; j < channels.size(); j++) {
                int ch = channels.get(j);
                channelArray[ch - 11] = 1;
                boolean bi = checkBidirectional(s, d, ch, prr);
                if (!bi) {
                    flag = false;
                    break;
                }
            }// for each channel
            if (flag) // a link works in all channels
            {
                Link l = new Link(s, d, channelArray);
                //l.printLink();
                links.add(l);
            }
        }
        return links;
    }

    public float[] getAvgPRR(ArrayList<Integer> channel, int prr) // 1.1
    {
        int[] numLinks = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        float[] averagePRR = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        for (int i = 0; i < channel.size(); i++) {
            int ch = channel.get(i);
            numLinks[ch - 11] = 0;
            averagePRR[ch - 11] = 0;
            ArrayList<Integer> linkPRR = new ArrayList<Integer>();
            for (int j = 0; j < this.sources.size(); j++) {
                int s = this.sources.get(j);
                int d = this.destinations.get(j);
                if (this.checkBidirectional(s, d, ch, prr)) // a link is bidirectional
                {
                    linkPRR.add(this.topology[s][d][ch - 11]);
                }
            }
            numLinks[ch - 11] = linkPRR.size();
            averagePRR[ch - 11] = this.findAvg(linkPRR);

            String out = ch + ":" + numLinks[ch - 11] + " " + averagePRR[ch - 11];
            //System.out.println(out);
        }
        return averagePRR;
    }

    public ArrayList<Integer> getNumNodes(ArrayList<Integer> channels, int prr) {
        ArrayList<Link> links = this.getLinks(channels, prr);
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        Node[] nodeOut = new Node[200];
        for (int i = 0; i < 200; i++) {
            nodeOut[i] = new Node(i);
        }
        for (int i = 0; i < links.size(); i++) {
            int src = links.get(i).source;
            int dest = links.get(i).destination;
            nodeOut[src].neighbors.add(dest);
        }
        int countNode = 0;
        for (int i = 0; i < nodeOut.length; i++) {
            if (nodeOut[i].neighbors.size() > 0) {
                neighbors.add(nodeOut[i].neighbors.size());
                countNode++;
            }
        }
        neighbors.add(countNode); // last element is no. of node
        return neighbors;
    }

    public void printNeighborsNum(ArrayList<Integer> n) {
        int total = n.size();
        String out = "";
        for (int i = 0; i < total - 1; i++) {
            out = out + n.get(i) + "\n";
        }
        out = out + "total Node = " + n.get(total - 1);
        System.out.println(out);
    }

    public void printNeighbors(Node[] nodeArr) {
        for (int i = 0; i < nodeArr.length; i++) {
            String out = "";
            if (nodeArr[i].neighbors.size() > 0) {
                int src = nodeArr[i].nodeID;
                out = out + src + ": ";
                for (int j = 0; j < nodeArr[i].neighbors.size(); j++) {
                    out = out + nodeArr[i].neighbors.get(j) + " ";
                }
                System.out.println(out);
            }
        }
    }

    public int findAvg(ArrayList<Integer> values) // 1.1.1
    {
        int sum = 0;
        for (int i = 0; i < values.size(); i++) {
            sum = sum + values.get(i);
        }
        return sum / values.size();
    }

    public int findMin(ArrayList<Integer> values) // 1.1.1
    {
        int min = 1000;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) < min)
                min = values.get(i);
        }
        return min;
    }

    public ArrayList<Integer> getNodes(ArrayList<Link> links) {
        ArrayList<Integer> exist = new ArrayList<Integer>();
        for (int i = 0; i < links.size(); i++) {
            Link l = links.get(i);
            if (!checkNodeExist(exist, l.source)) {
                exist.add(l.source);

            }
            if (!checkNodeExist(exist, l.destination)) {
                exist.add(l.destination);
            }
            // add edge
        }
        return exist;
    }

    public ArrayList<Node> getDegrees(ArrayList<Link> links) {
        ArrayList<Integer> nodes = this.getNodes(links);
        ArrayList<Node> results = new ArrayList<Node>();
        //results.add(nodes.size());
        System.out.println("get Degrees");
        for (int i = 0; i < nodes.size(); i++) {
            int node = nodes.get(i);
            Node n = new Node(node);
            int deg = 0;
            for (int j = 0; j < links.size(); j++) {
                if (links.get(j).source == node) {
                    n.neighbors.add(links.get(j).destination);
                    deg++;
                }
            }
            System.out.println(node + " " + deg);
            results.add(n);
        }
        return results;
    }

    public boolean checkNodeExist(ArrayList<Integer> exist, int node) // 1.1
    {
        for (int i = 0; i < exist.size(); i++) {
            if (node == exist.get(i))
                return true;
        }
        return false;
    }

    public void getConnectivityArray(ArrayList<Link> links) {
        int[][] arr = new int[disSources.size()][disSources.size()]; // each row is node, each column is its neighbors
        for (int i = 0; i < disSources.size(); i++) {
            int src = disSources.get(i);
            for (int k = 0; k < disSources.size(); k++) // init each row
            {
                arr[i][k] = 0;
            }
            for (int j = 0; j < links.size(); j++) {
                int node = links.get(j).source;
                if (node == src) {
                    int peer = links.get(j).destination;
                    int peerIndex = -1;
                    for (int k = 0; k < disSources.size(); k++) // find index of a neighbor
                    {
                        if (peer == disSources.get(k))
                            peerIndex = k;
                    }
                    if (peerIndex > 0)
                        arr[i][peerIndex] = 1;
                    else
                        System.out.println("something's wrong");
                }
            } // end for each link
        }// end for each source
        // print connectivity array
        for (int i = 0; i < disSources.size(); i++) {
            String out = "";
            for (int j = 0; j < disSources.size(); j++) {
                out = out + arr[i][j] + " ";
            }
            System.out.println(out);
        }
    }
}
