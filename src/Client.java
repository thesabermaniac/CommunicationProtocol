import java.io.*;
import java.net.Socket;

public class Client {
    Socket socket;
    ObjectInputStream input;
    DataOutputStream output;
    String[] messages;
    StringBuilder sb = new StringBuilder();

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
            input = new ObjectInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

//            Start receiving packets from server and re-building the message.
            System.out.println("Receiving message...");
            receiveMessage();
            System.out.println("Requesting lost packets...");
            requestLostPackets();
            System.out.println("Receiving lost packets...");
            receiveLostPackets();
            System.out.println("Message received.");

//            Builds the message using a StringBuilder object
            for (String s : messages){
                sb.append(s);
            }
            System.out.println(sb.toString());

            input.close();
            output.close();
            socket.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    /**
     * Receives message from server, in several smaller-sized packets.
     * The packets come out of order, so this method must rearrange them
     * in the proper order, as indicated by the index variable in each packet.
     */
    private void receiveMessage(){
        Packet packet = new Packet();

        while(!(packet.data.equals("Done") && packet.index == -1)){
            try {
                packet = (Packet) input.readObject();
            }
            catch (Exception e){
                System.out.println(e);
            }

//            if messages array hasn't been created yet (i.e. this is the first packet), create a new array, who's size is based on the packet's size variable.
            if(messages == null){
                messages = new String[packet.size];
            }
//            insert packet's data into it's proper index.
            if(packet.index >= 0) {
                messages[packet.index] = packet.data;
            }
        }

    }

    /**
     * Iterates over message list and finds missing data, then requests that data from the server
     */
    private void requestLostPackets(){
        for(int i = 0; i < messages.length; i++){
            if(messages[i] == null){
                try {
                    output.writeInt(i);
                    output.flush();
                }
                catch (Exception e){
                    System.out.println(e);
                }
            }
        }

//        Notify server that we have completed our check by sending a -1
        try {
            output.writeInt(-1);
            output.flush();
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    /**
     * Receives missing data from the server and places it in its proper location in array
     */
    private void receiveLostPackets(){
        Packet packet = new Packet();
        while (!(packet.data.equals("Complete") && packet.index == -1)){
            try {
                packet = (Packet) input.readObject();
                if(packet.index >= 0) {
                    messages[packet.index] = packet.data;
                }
            }
            catch (Exception e){
                System.out.println(e);
            }
        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = new Client("127.0.0.1", 5000);
    }
}
