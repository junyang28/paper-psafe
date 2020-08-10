public class Link {
    public int source;
    public int destination;
    public int[] channels = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public boolean flag; // true=selected

    public Link(int src, int dest, int[] ch) {
        this.source = src;
        this.destination = dest;
        this.flag = false;
        this.channels = ch;
    }

    public void printLink() {
        String out = source + " " + destination + ":";
        for (int i = 0; i < 16; i++) {
            out = out + channels[i] + " ";
        }
        System.out.println(out);
    }

    public void setFlag(boolean f) {
        this.flag = f;
    }

    public void setChannel_1(int chNum) {
        channels[chNum - 11] = 1;
    }

    public void setChannel_0(int chNum) {
        channels[chNum - 11] = 0;
    }

    public void updateChannel(int[] ch) {
        channels = ch;
    }

}

