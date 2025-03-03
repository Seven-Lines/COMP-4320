import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class myFirstTCPClient {
    private static final int NUM_MEASUREMENTS = 7;
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java myFirstTCPClient <serverHost> <serverPort>");
            System.exit(1);
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        
        long[] rttMeasurements = new long[NUM_MEASUREMENTS];
        int count = 0;
        
        try (Socket socket = new Socket(serverHost, serverPort);
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             InputStream in = socket.getInputStream();
             Scanner userInput = new Scanner(System.in)) {

            System.out.println("Connected to server " + serverHost + " on port " + serverPort);
            System.out.println("Enter " + NUM_MEASUREMENTS + " integers to measure round trip times.");

            while (count < NUM_MEASUREMENTS) {
                System.out.print("\nEnter a short integer (attempt "
                                 + (count + 1) + " of " + NUM_MEASUREMENTS + "): ");
                
                short number;
                try {
                    number = Short.parseShort(userInput.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number −32768..32767.");
                    continue;
                }

                // Prepare the two bytes to send
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                DataOutputStream tempOut = new DataOutputStream(byteStream);
                tempOut.writeShort(number);
                tempOut.flush();
                byte[] shortBytes = byteStream.toByteArray();

                // Display bytes in hex (what we're sending)
                System.out.print("Sending bytes (hex): ");
                printHex(shortBytes);

                // Record time just before sending
                long sendTime = System.currentTimeMillis();

                // Send the two bytes
                dout.write(shortBytes);
                dout.flush();

                // Read the server’s response (variable-length UTF-16 string)
                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                socket.setSoTimeout(2000);

                try {
                    // A simple loop to read until the server stops sending
                    // or we hit the timeout
                    byte[] buf = new byte[256];
                    while (true) {
                        int bytesRead = in.read(buf);
                        if (bytesRead < 0) { break; }
                        responseBuffer.write(buf, 0, bytesRead);
                        if (in.available() == 0) { break; }
                    }
                } catch (IOException timeoutOrError) {
                    break;
                }

                // Record time just after finishing the read
                long receiveTime = System.currentTimeMillis();
                long rtt = receiveTime - sendTime;
                rttMeasurements[count] = rtt;

                // Convert response to byte[]
                byte[] serverResponse = responseBuffer.toByteArray();

                // Display received bytes in hex
                System.out.print("Received bytes (hex): ");
                printHex(serverResponse);

                // Decode as UTF-16 (the server should be sending a BOM 0xFE,0xFF first)
                // but we’ll just interpret the entire thing as UTF-16 Big Endian
                // to keep it simple
                String decodedString;
                try {
                    decodedString = new String(serverResponse, "UTF-16");
                } catch (UnsupportedEncodingException e) {
                    decodedString = "[Decoding Error]";
                }

                System.out.println("Decoded response: " + decodedString);
                System.out.println("Round-Trip Time: " + rtt + " ms");

                count++;
            }

            printRTTStats(rttMeasurements);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHex(byte[] data) {
        for (byte b : data) {
            System.out.printf("0x%02X ", b);
        }
        System.out.println();
    }

    private static void printRTTStats(long[] rtts) {
        if (rtts.length == 0) return;
        long min = rtts[0], max = rtts[0], sum = 0;
        for (long rtt : rtts) {
            if (rtt < min) min = rtt;
            if (rtt > max) max = rtt;
            sum += rtt;
        }
        double avg = (double) sum / rtts.length;
        System.out.println("\n=== RTT Statistics ===");
        System.out.println("Min RTT: " + min + " ms");
        System.out.println("Max RTT: " + max + " ms");
        System.out.printf("Avg RTT: %.2f ms%n", avg);
    }
}
