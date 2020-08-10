import org.jgrapht.alg.*;
import org.jgrapht.*;
//import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;
import org.jgrapht.graph.DefaultEdge;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.*;

public class Dijkstra {
    public ArrayList<Link> links;
    public ArrayList<Integer> vertices;
    public FlowSet flowset;
    public Graph<String, DefaultEdge> graph;
    public ArrayList<Integer> numhop;

    public Dijkstra(ArrayList<Link> l, FlowSet fs) {
        links = l;
        flowset = fs;
        vertices = new ArrayList<Integer>();
        numhop = new ArrayList<Integer>();
        graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        this.initGraph();
    }

    public void initGraph() // step 1
    {
        ArrayList<Integer> exist = new ArrayList<Integer>();
        for (int i = 0; i < links.size(); i++) {
            Link l = links.get(i);
            // add vertex, if not already added
            if (!checkNodeExist(exist, l.source)) {
                exist.add(l.source);
                graph.addVertex(Integer.toString(l.source));
                vertices.add(l.source);
                //System.out.println(l.source);
            }
            if (!checkNodeExist(exist, l.destination)) {
                exist.add(l.destination);
                graph.addVertex(Integer.toString(l.destination));
                vertices.add(l.destination);
                //System.out.println(l.destination);
            }
            // add edge
            graph.addEdge(Integer.toString(l.source), Integer.toString(l.destination));
        }

    }

    public boolean checkNodeExist(ArrayList<Integer> exist, int node) // 1.1
    {
        for (int i = 0; i < exist.size(); i++) {
            if (node == exist.get(i))
                return true;
        }
        return false;
    }

    public int findFlowPaths() // step 2
    {
        for (int i = 0; i < flowset.numFlows; i++) // for each flow
        {
            int src = flowset.getSource(i);
            int dest = flowset.getDestination(i);
            boolean c1 = this.checkVertex(src);
            boolean c2 = this.checkVertex(dest);
            boolean ap = this.checkVertex(flowset.accessPoints);

            // nodes exist in the graph => get uplink and downlink
            if (c1 && c2 && ap) {
                List up = findUplink(src);
                List down = findDownlink(dest);
                //System.out.println("flow " +i);
                //System.out.println(up +"\n" +down);
                if (up == null || down == null) // no route
                {
                    return 1;
                } else // route exist
                {
                    createFlow(up, down, src, dest, i);
                }
            } else // node does not exist in a graph
            {
                return 2;
            }
        }
        //this.printFlowSet();
        return 0;
    }

    public boolean checkVertex(int node) //2.1
    {
        for (int i = 0; i < vertices.size(); i++) {
            if (node == vertices.get(i))
                return true;
        }
        return false;
    }

    public List findUplink(int src) //2.2
    {
        List l = findPath(src, flowset.accessPoints);
        if (l != null)
            return l;
        else
            return null;
    }

    public List findDownlink(int dest) //2.3
    {
        List l = findPath(dest, flowset.accessPoints);
        if (l != null)
            return l;
        else
            return null;

    }

    public List findPath(int src, int dest)//2.1.2-3
    {
        GraphPath path = DijkstraShortestPath.findPathBetween(graph, Integer.toString(src), Integer.toString(dest));
        //return path;
        return path.getEdgeList();
        //return 0;
    }

    public void createFlow(List up, List down, int src, int dest, int flowID) //2.4
    {

        Flow f = new Flow(src, dest);
        String out = flowID + ": " + src + "," + dest + "\n" + "uplink: ";
        ArrayList<Transmission> tup = new ArrayList<Transmission>();
        ArrayList<Transmission> tdown = new ArrayList<Transmission>();
        for (int i = 0; i < up.size(); i++) // uplink
        {
            int sender = Integer.parseInt(graph.getEdgeSource((DefaultEdge) up.get(i)));
            int receiver = Integer.parseInt(graph.getEdgeTarget((DefaultEdge) up.get(i)));
            out = out + sender + "->" + receiver + ",";
            Transmission t = new Transmission(sender, receiver, flowID);
            tup.add(t);
        }
        out = out + "\n" + "downlink: ";
        for (int i = 0; i < down.size(); i++) // downlink
        {
            int sender = Integer.parseInt(graph.getEdgeSource((DefaultEdge) down.get(i)));
            int receiver = Integer.parseInt(graph.getEdgeTarget((DefaultEdge) down.get(i)));
            Transmission t = new Transmission(sender, receiver, flowID);
            tdown.add(t);
            out = out + sender + "->" + receiver + ",";
        }
        //out = out +"\n" +flowID;
        //System.out.println(out);
        f.addUplink(tup);
        f.addDownlink(tdown);
        flowset.flows.add(f);
    }

    public void printFlowSet() {
        for (int i = 0; i < flowset.numFlows; i++) {
            flowset.flows.get(i).printFlowSource();
        }
    }

    public ArrayList<Integer> findNeighbors(int node, int nodeEx) {
        ArrayList<Integer> peers = new ArrayList<Integer>();
        for (int i = 0; i < links.size(); i++) {
            int src = links.get(i).source;
            int dest = links.get(i).destination;
            if (src == node && dest != nodeEx && this.checkVertex(dest))
                peers.add(links.get(i).destination);
        }
        return peers;
    }
}
