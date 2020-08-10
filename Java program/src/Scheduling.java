import java.util.ArrayList;

public class Scheduling {
    public FlowSet fs;
    public ArrayList<Flow> flows;
    public ArrayList<Integer> periods;
    public int numFlows;
    public int numChannels;
    public ArrayList<Slot> schedule;

    public int transConflicts;
    public int chConflicts;
    public int newOffset;

    public Scheduling(FlowSet fs, int numChannel) {
        this.fs = fs;
        this.numFlows = fs.numFlows;
        this.flows = fs.flows;
        this.periods = fs.period;
        this.numChannels = numChannel;
        schedule = new ArrayList<Slot>();
        transConflicts = 0;
        chConflicts = 0;
    }

    public boolean schedule_source() //step 1
    {
        for (int i = 0; i < numFlows; i++) // schedule each flow
        {
            Flow f = flows.get(i);
            int deadline = periods.get(i);
            // schedule uplink
            int slotNum = 0;
            for (int j = 0; j < f.uplink.size(); j++) {
                Transmission t = f.uplink.get(j);
                slotNum = this.schedule_trasnmission(t, deadline, slotNum, 0);
                if (slotNum == -1)
                    return false;
            }
            //schedule downlink
            for (int j = 0; j < f.downlink.size(); j++) {
                Transmission t = f.downlink.get(j);
                slotNum = this.schedule_trasnmission(t, deadline, slotNum, 1);
                if (slotNum == -1)
                    return false;
            }
            flows.get(i).delay = slotNum;
            //System.out.println(slotNum);
        }
        return true;
    }

    public int schedule_trasnmission(Transmission tran, int deadline, int slotNum, int type) // 1.1
    {
        int slotNum1 = this.findFreeSlot(slotNum, tran); // first transmission
        this.addTransmissionToSlot(slotNum1, tran, type);
        int slotNum2 = this.findFreeSlot(slotNum1, tran); // 2nd transmission
        this.addTransmissionToSlot(slotNum2, tran, type);
        if (slotNum2 > deadline)
            return -1;
        return slotNum2;
    }

    public int findFreeSlot(int slotNum, Transmission tran) // 1.1.1
    {
        // find free slot from the existing slot
        for (int i = slotNum; i < schedule.size(); i++) {
            Slot slot = schedule.get(i);
            if (this.checkChannelConflict(slot)) {
                if (this.checkTransConflict(slot, tran)) {
                    // take this slot
                    return slot.slotNum;
                } else {
                    //count trans conflict
                    this.transConflicts++;
                }
            }
            //count channel conflict
            this.chConflicts++;
        } // end for
        // cannot find free slot -> create a new one
        return schedule.size() + 1;
    }

    public boolean checkTransConflict(Slot slot, Transmission tran) //1.1.1.2
    {
        boolean flag1 = slot.checkTransConflict(tran.source);
        boolean flag2 = slot.checkTransConflict(tran.destination);
        if (flag1 && flag2)
            return true;
        return false;
    }

    public boolean checkChannelConflict(Slot slot) //1.1.1.2
    {
        return slot.checkChannelConflict();
    }

    public void addTransmissionToSlot(int slotNum, Transmission tran, int type) {
        Transmission newtran = new Transmission(tran.source, tran.destination, tran.flowID);
        if (slotNum > schedule.size()) // create new slot
        {
            Slot s = new Slot(slotNum, numChannels);
            newtran.chOffset = s.trans.size();
            newtran.tranType = type;
            s.addTransmission(newtran);
            schedule.add(s);
        } else // add transmission to slot
        {
            Slot s = schedule.get(slotNum - 1);
            newtran.chOffset = s.trans.size();
            newtran.tranType = type;
            schedule.get(slotNum - 1).addTransmission(newtran);
        }
    }

    public boolean schedule_source2() {
        for (int i = 0; i < numFlows; i++) // schedule each flow
        {
            Flow f = flows.get(i);
            int deadline = periods.get(i);
            // schedule uplink
            int slotNum;
            if (i % 2 == 0)
                slotNum = 0;
            else
                slotNum = 1;
            for (int j = 0; j < f.uplink.size(); j++) {
                Transmission t = f.uplink.get(j);
                slotNum = this.schedule_trasnmission2(t, deadline, slotNum, 0);
                if (slotNum == -1)
                    return false;
            }
            //schedule downlink
            for (int j = 0; j < f.downlink.size(); j++) {
                Transmission t = f.downlink.get(j);
                slotNum = this.schedule_trasnmission2(t, deadline, slotNum, 1);
                if (slotNum == -1)
                    return false;
            }
            flows.get(i).delay = slotNum;
            //System.out.println(slotNum);
        }
        return true;

    }

    public int schedule_trasnmission2(Transmission tran, int deadline, int slotNum, int type) // 1.1
    {
        int slotNum1 = this.findFreeSlot_2(slotNum, tran, -1); // first transmission
        int logic1 = this.addTransmissionToSlot_2(slotNum1, tran, 0, type, -1);
        int slotNum2 = this.findFreeSlot_2(slotNum1, tran, logic1); // 2nd transmission
        int logic2 = this.addTransmissionToSlot_2(slotNum2, tran, 1, type, logic1);
        if (slotNum2 > deadline)
            return -1;
        return slotNum2;
    }

    public int findFreeSlot_2(int slotNum, Transmission tran, int logicalChannel) {
        for (int i = slotNum; i < schedule.size(); i++) {
            Slot slot = schedule.get(i);
            if (this.checkTransConflict(slot, tran)) {

                if (this.checkChannelConflict_2(slot, logicalChannel))
                    return slot.slotNum;
                else
                    this.chConflicts++;
            } else {
                this.transConflicts++;
            }
        }
        return schedule.size() + 1;
    }

    public boolean checkChannelConflict_2(Slot slot, int logicalChannel) {
        if (logicalChannel == -1) // first transmission
        {
            return slot.checkChannelConflict2(0);
        } else // second transmission
        {
            if (slot.checkChannelConflict2(1)) // still some channel left
            {
                for (int i = 0; i < slot.trans.size(); i++) {
                    Transmission t = slot.trans.get(i);
                    int newCh = (slot.slotNum + t.chOffset) % (numChannels / 2);
                    if (t.channelSet == 2 && logicalChannel == newCh) {
                        return false;
                    }
                }
            } else
                return false;
        }
        return true;
    }

    public int addTransmissionToSlot_2(int slotNum, Transmission tran, int ttype, int type, int prevCh) // return channel offset
    {
        Slot s;
        Transmission newtran = new Transmission(tran.source, tran.destination, tran.flowID);
        if (slotNum > schedule.size()) // create new slot
        {
            s = new Slot(slotNum, numChannels / 2);
            if (ttype == 0) // first transmission
            {
                newtran.chOffset = s.currNum1; // actual channel offset
                newtran.channelSet = 1;
                s.updateChannel1();
            } else {
                newtran.chOffset = this.findOffset(slotNum, prevCh); // (prevSlot+prevOffset)%numChannel
                newtran.channelSet = 2;
                s.updateChannel2();
            }
            newtran.tranType = type;
            s.addTransmission(newtran);
            schedule.add(s);
            //System.out.println(slotNum +" " +newtran.chOffset +" " +newtran.source +" " +newtran.destination +" " +newtran.flowID +" " +newtran.tranType);
        } else // add transmission to slot
        {
            s = schedule.get(slotNum - 1);
            if (ttype == 0) // first transmission
            {
                newtran.chOffset = s.currNum1; // actual channel offset
                newtran.channelSet = 1;
                s.updateChannel1();
            } else {
                newtran.chOffset = this.findOffset(slotNum, prevCh); // (prevSlot+prevOffset)%numChannel
                newtran.channelSet = 2;
                s.updateChannel2();
            }
            newtran.tranType = type;
            schedule.get(slotNum - 1).addTransmission(newtran);
            //System.out.println(slotNum +" " +newtran.chOffset +" " +newtran.source +" " +newtran.destination +" " +newtran.flowID +" " +newtran.tranType);
        }
        int logicalChannel = (slotNum + newtran.chOffset) % (numChannels / 2);
        return logicalChannel;
    }

    public int findOffset(int slotNum, int logicCh) {
        for (int i = 0; i < numChannels; i++) {
            int ch = (slotNum + i) % (numChannels / 2);
            if (ch == logicCh)
                return i;
        }
        return -1;
    }

    public void printSchedule() {
        String out = "";
        for (int i = 0; i < schedule.size(); i++) {
            Slot slot = schedule.get(i);
            slot.printSlot();
        }
    }

    public void printScheduleByFlow(int flowID) {
        String out = "flow " + flowID + "\n";
        for (int i = 0; i < schedule.size(); i++) {
            Slot slot = schedule.get(i);
            for (int j = 0; j < slot.getNumTrans(); j++) {
                Transmission t = slot.trans.get(j);
                if (t.flowID == flowID) {
                    int chOff = (slot.slotNum + t.chOffset) % (numChannels / 2);
                    out = out + slot.slotNum + " " + t.source + " " + t.destination + " " + t.channelSet + " " + chOff + "\n";
                }
            }
        }
        System.out.println(out);
    }

}
