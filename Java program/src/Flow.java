import java.util.*;

public class Flow {

    public int source;
    public int destination;
    public int delay;
    public ArrayList<Transmission> uplink;
    public ArrayList<Transmission> downlink;
    ArrayList<ArrayList<Transmission>> upBack;
    ArrayList<ArrayList<Transmission>> downBack;

    public Flow(int s, int d) {
        this.source = s;
        this.destination = d;
        uplink = new ArrayList<Transmission>();
        downlink = new ArrayList<Transmission>();
        upBack = new ArrayList<ArrayList<Transmission>>();
        downBack = new ArrayList<ArrayList<Transmission>>();
    }

    public void addUplink(ArrayList<Transmission> up) {
        uplink = up;
    }

    public void addDownlink(ArrayList<Transmission> down) {
        downlink = down;
    }

    public void addUpBack(ArrayList<Transmission> up) {
        upBack.add(up);
    }

    public void addDownBack(ArrayList<Transmission> down) {
        downBack.add(down);
    }

    public int getNumHops() {
        return uplink.size() + downlink.size();
    }

    public void printFlowSource() {
        String out = source + " " + destination + "\n" + "uplink:";
        for (int i = 0; i < uplink.size(); i++) {
            out = out + "," + uplink.get(i).source + "->" + uplink.get(i).destination;
        }
        out = out + "\ndownlink:";
        for (int i = 0; i < downlink.size(); i++) {
            out = out + "," + downlink.get(i).source + "->" + downlink.get(i).destination;
        }
        System.out.println(out);
    }

    public void printFlowGraph() {
        String out = "===== " + source + " " + destination + " =====\n" + "uplink:";
        for (int i = 0; i < uplink.size(); i++) {
            out = out + "," + uplink.get(i).source + "->" + uplink.get(i).destination;
        }
        for (int i = 0; i < upBack.size(); i++) {
            ArrayList<Transmission> trans = upBack.get(i);
            out = out + "\n";
            for (int j = 0; j < trans.size(); j++) {
                out = out + "," + trans.get(j).source + "->" + trans.get(j).destination;
            }
        }
        out = out + "\ndownlink:";
        for (int i = 0; i < downlink.size(); i++) {
            out = out + downlink.get(i).source + "->" + downlink.get(i).destination + ",";
        }
        for (int i = 0; i < downBack.size(); i++) {
            ArrayList<Transmission> trans = downBack.get(i);
            out = out + "\n";
            for (int j = 0; j < trans.size(); j++) {
                out = out + trans.get(j).source + "->" + trans.get(j).destination + ",";
            }
        }
        System.out.println(out);

    }

}
