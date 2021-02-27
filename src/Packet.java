import java.io.Serializable;

public class Packet implements Comparable<Packet>, Serializable {
    // Stores the data of this portion of the message
    String data;
    // Stores the index in which this data belongs (for client to re-order)
    int index;
    // Stores total number of packets in this message (for client to build array)
    int size;

    public Packet(){
        data = "";
        // index and size are initialized to -1 to avoid conflicts when server
        // sends "Done" or "Complete" messages to client
        index = -1;
        size = -1;
    }

    /**
     * Custom compareTo() method that sorts a Packet List by the size of the data in the packet
     *
     * @param packet
     * @return
     */
    @Override
    public int compareTo(Packet packet) {
        return Integer.compare(this.data.length(), packet.data.length());
    }
}
