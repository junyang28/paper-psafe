import java.util.ArrayList;

public class Slot {
    public int slotNum;
    public int numChannels1;
    public int numChannels2;
    public int oddChannel;
    public int currNum1, currNum2;
    public ArrayList<Transmission> trans;

    public Slot(int slotNum, int numChannel) {
        this.slotNum = slotNum;
        this.numChannels1 = numChannel;
        this.numChannels2 = numChannel;
        this.currNum1 = 0;
        this.currNum2 = 0;
        this.oddChannel = 0;
        trans = new ArrayList<Transmission>();
    }

    public void addTransmission(Transmission tran) {
        trans.add(tran);
    }

    public boolean checkTransConflict(int node) {
        for (int i = 0; i < trans.size(); i++) {
            Transmission t = trans.get(i);
            if (node == t.source || node == t.destination)
                return false;
        }
        return true;
    }

    public boolean checkChannelConflict() {
        if (trans.size() >= numChannels1)
            return false;
        return true;
    }

    public boolean checkChannelConflict2(int type) {
        if (type == 0 && currNum1 >= numChannels1) {
            return false;
        }
        if (type == 1 && currNum2 >= numChannels2) {
            return false;
        }
        return true;
    }

    public void updateChannel1() {
        this.currNum1++;
    }

    public void updateChannel2() {
        this.currNum2++;
    }

    public void printSlot() {
        String out = "";
        for (int i = 0; i < trans.size(); i++) {
            int ch = (slotNum + trans.get(i).chOffset) % numChannels1;
            out = out + slotNum + " " + trans.get(i).source + " " + trans.get(i).destination + " " + trans.get(i).flowID;
            out = out + " " + trans.get(i).channelSet + " " + ch + "\n";
        }
        System.out.println(out);
    }

    public int getNumTrans() {
        return trans.size();
    }
}
