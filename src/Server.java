import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Server {
    Socket socket;
    ServerSocket server;
    DataInputStream input;
    ObjectOutputStream output;
    ArrayList<Packet> packets;
    ArrayList<Packet> packetsSorted = new ArrayList<>();
    ArrayList<Packet> lostPackets = new ArrayList<>();

    String message = "He heard the crack echo in the late afternoon about a mile away. His heart started racing and he bolted into a full sprint. \"It wasn't a gunshot, it wasn't a gunshot,\" he repeated under his breathlessness as he continued to sprint.\n" +
            "I'm heading back to Colorado tomorrow after being down in Santa Barbara over the weekend for the festival there. I will be making October plans once there and will try to arrange so I'm back here for the birthday if possible. I'll let you know as soon as I know the doctor's appointment schedule and my flight plans.\n" +
            "The chair sat in the corner where it had been for over 25 years. The only difference was there was someone actually sitting in it. How long had it been since someone had done that? Ten years or more he imagined. Yet there was no denying the presence in the chair now.\n" +
            "She tried to explain that love wasn't like pie. There wasn't a set number of slices to be given out. There wasn't less to be given to one person if you wanted to give more to another. That after a set amount was given out it would all disappear. She tried to explain this, but it fell on deaf ears.\n" +
            "He watched as the young man tried to impress everyone in the room with his intelligence. There was no doubt that he was smart. The fact that he was more intelligent than anyone else in the room could have been easily deduced, but nobody was really paying any attention due to the fact that it was also obvious that the young man only cared about his intelligence.";

    public Server(int port){
        try{
            server = new ServerSocket(port);
            System.out.println("Server Started");

            System.out.println("Waiting for client...");

//             Split message into packets before connection with client is established.
//             This ensures the message is prepared to be sent as soon as the client connects.
            splitMessage();

            socket = server.accept();
            System.out.println("Client Accepted");

            input = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream())
            );
            output = new ObjectOutputStream(socket.getOutputStream());

            sendPackets();
            System.out.println("Transfer Complete.");

            System.out.println("Closing connection");

            socket.close();
            input.close();
        }
        catch (IOException e){
            System.out.println(e);
        }

    }

    /**
     * Splits the message into packets using a custom Packet object that stores relevant info in each packet,
     * including data (the contents of the message), index (the correct location of the message),
     * and size (the total number of packets in this message).
     */
    private void splitMessage(){
        String[] words = message.split(" ");
        packets = new ArrayList<>();
        int len = words.length;
        for (int i = 0; i < len; i++){
            Packet packet = new Packet();
            packet.data = words[i] + " ";
            packet.index = i;
            packet.size = len;
            packets.add(packet);
        }
        packetsSorted.addAll(packets);
//        packets get sorted by the size of the data to send the smallest pieces first, ensuring fastest transfer time
        packetsSorted.sort(Packet::compareTo);
    }

    /**
     * Sends packets to client using send() helper method. It then notifies the client when it's done,
     * sending a packet labeled "Done". To avoid conflicts, this Packet will also have size and index
     * values of -1, as is the default.
     */
    private void sendMessage(){
        try {
            send(packetsSorted, false);
            Packet packet = new Packet();
            packet.data = "Done";
            output.writeObject(packet);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    /**
     * Receive indexes of missing packets from the client.
     */
    private void recoverLostPackets(){
        try {
            int lostInt = 0;
            while (!(lostInt == -1)) {
                lostInt = input.readInt();
                if (lostInt >= 0) {
                    Packet packet = packets.get(lostInt);
                    lostPackets.add(packet);
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    /**
     * Sends lost packets to client, making sure the lostPackets ArrayList is empty before moving on.
     */
    private void sendLostPackets(){
        try {
            while (lostPackets.size() > 0) {
                send(lostPackets, true);
            }
            Packet packet = new Packet();
            packet.data = "Complete";
            output.writeObject(packet);
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    /**
     * Sends broken-down message (packets) to client, in order from the smallest slice to the largest.
     * The client is then responsible for putting the message back in the order it's supposed to be in.
     */
    private void sendPackets(){

        String line = "";

        while (!line.equals("Done")){
            try {
                System.out.println("Sending Message...");
                sendMessage();
                System.out.println("Recovering lost packets...");
                recoverLostPackets();
                System.out.println("Sending lost packets...");
                sendLostPackets();
                line = "Done";
            }
            catch (Exception e){
                System.out.println(e);
            }
        }
    }

    /**
     * Iterates over ArrayList of packets and sends each one to the client.
     * Every packet has a 20% chance of dropping and never making it to the client.
     *
     * @param packets
     * @param remove
     */
    private void send(ArrayList<Packet> packets, boolean remove){
        try {
            for (int i = packets.size() - 1; i >= 0; i--) {
                Random rand = new Random();
                int num = rand.nextInt(100);
                if (num >= 20) {
                    output.writeObject(packets.get(i));
                    output.flush();
                    if(remove) {
                        packets.remove(i);
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    public static void main(String[] args) {
        Server server = new Server(5000);
    }
}
