import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class myFirstUDPServer {
    public static void main(String[] args) {
        // Error handle invalid num of params
        if (args.length != 1) {
            System.out.println("Usage: java myFirstUDPServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP server is running on port " + port);

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                byte[] receivedData = new byte[receivePacket.getLength()];
                System.arraycopy(receiveBuffer, 0, receivedData, 0, receivePacket.getLength());

                System.out.print("Received bytes: ");
                for (byte b : receivedData) {
                    System.out.printf("0x%02X ", b);
                }
                System.out.println();

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                String response;
                if (receivedData.length != 2) {
                    response = "****";
                } else {
                    ByteBuffer buffer = ByteBuffer.wrap(receivedData);
                    short number = buffer.getShort();
                    System.out.println("Received number: " + number);
                    System.out.println("From client: " + clientAddress + ":" + clientPort);
                    response = Short.toString(number);
                }

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_16);

                System.out.print("Sending bytes: ");
                for (byte b : responseBytes) {
                    System.out.printf("0x%02X ", b);
                }
                System.out.println();

                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes, responseBytes.length, clientAddress, clientPort
                );
                socket.send(responsePacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
