import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class myFirstUDPClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java myFirstUDPClient <hostname> <port>");
            return;
        }

        final int NUM_TRIPS = 7;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (DatagramSocket socket = new DatagramSocket(); Scanner scanner = new Scanner(System.in)) {
            InetAddress serverAddress = InetAddress.getByName(hostname);
            long[] rtts = new long[NUM_TRIPS];

            for (int i = 0; i < NUM_TRIPS; i++) {
                System.out.print("Enter a number (-32768 to 32767): ");
                short number = scanner.nextShort();

                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.putShort(number);
                byte[] sendBytes = buffer.array();

                System.out.print("Sending bytes: ");
                for (byte b : sendBytes) {
                    System.out.printf("0x%02X ", b);
                }
                System.out.println();

                DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, serverAddress, port);

                long startTime = System.currentTimeMillis();
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                long endTime = System.currentTimeMillis();

                // Round Trip Time (RTT)
                long rtt = endTime - startTime;
                rtts[i] = rtt;

                byte[] receivedData = new byte[receivePacket.getLength()];
                System.arraycopy(receiveBuffer, 0, receivedData, 0, receivePacket.getLength());

                // Display received bytes in hex
                System.out.print("Received bytes: ");
                for (byte b : receivedData) {
                    System.out.printf("0x%02X ", b);
                }
                System.out.println();

                // Decode UTF-16 response
                String response = new String(receivedData, StandardCharsets.UTF_16);
                System.out.println("Received string: " + response);
                System.out.println("Round-trip time: " + rtt + " ms\n");
            }

            // Report
            long min = rtts[0], max = rtts[0], sum = 0;
            for (long rtt : rtts) {
                if (rtt < min) min = rtt;
                if (rtt > max) max = rtt;
                sum += rtt;
            }
            double average = sum / (double) NUM_TRIPS;

            System.out.println("RTT Summary:");
            System.out.println("Min RTT: " + min + " ms");
            System.out.println("Max RTT: " + max + " ms");
            System.out.println("Avg RTT: " + String.format("%.2f", average) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
