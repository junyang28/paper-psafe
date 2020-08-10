import java.util.ArrayList;

public class FlowSet {

    public int numFlows;

    public ArrayList<Integer> sources;
    public ArrayList<Integer> destinations;
    public ArrayList<Integer> period;
    public ArrayList<Flow> flows;
    public int accessPoints;

    public FlowSet(int nf) {
        this.numFlows = nf;
        sources = new ArrayList<Integer>();
        destinations = new ArrayList<Integer>();
        period = new ArrayList<Integer>();
        flows = new ArrayList<Flow>();

        this.createFlow();
    }

    public void createFlow() // create flow
    {

        accessPoints = 121;

        int[] s = {147, 144, 105, 149, 136, 137};
        int[] d = {146, 143, 104, 102, 135, 108};

        for (int i = 0; i < numFlows; i++) {
            this.sources.add(s[i]);
            this.destinations.add(d[i]);
            period.add(100); // single period
        }
    }

    public int getSource(int flowID) {
        return this.sources.get(flowID);
    }

    public int getDestination(int flowID) {
        return this.destinations.get(flowID);
    }

    public void printFlow() {
        System.out.println("---------------Flow Set ---------------");
        String out = accessPoints + ": ";
        for (int i = 0; i < numFlows; i++) {
            out = out + "(" + sources.get(i) + " " + destinations.get(i) + ")" + " , ";
        }
        System.out.println(out);
    }

}
