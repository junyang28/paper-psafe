
public class Transmission {
    public int source;
    public int destination;
    public int chOffset; // type=0-real offset, type=1-(slot1+offset1)%numChannel
    public int flowID;
    public int tranType; // 0-up, 1-down
    public int routeNum;
    public int prevSlot;
    public int channelSet; // 1-good channel, 2-bad channel, 3-odd channel

    public Transmission(int src, int dest, int flowNum) {
        this.source = src;
        this.destination = dest;
        this.flowID = flowNum;
    }

    public void addChannelOffset(int ch) {
        this.chOffset = ch;
    }

    public void addTranType(int type) {
        tranType = type;
    }
}
