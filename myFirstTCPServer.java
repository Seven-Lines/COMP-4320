import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class myFirstTCPServer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java myFirstTCPServer <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            // Wait for a client to connect
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected from " 
                               + clientSocket.getInetAddress().getHostAddress() 
                               + ":" + clientSocket.getPort());

            InputStream in = clientSocket.getInputStream();
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            while (true) {
                byte[] buffer = new byte[2];
                int totalBytesRead = 0;

                while (totalBytesRead < 2) {
                    int bytesRead = in.read(buffer, totalBytesRead, 2 - totalBytesRead);
                    if (bytesRead == -1) {
                        // Client closed the connection or stream ended
                        System.out.println("Client disconnected.");
                        clientSocket.close();
                        return;
                    }
                    totalBytesRead += bytesRead;
                }

                // At this point, we've read exactly 2 bytes
                // Display byte-per-byte in hex
                System.out.print("\nReceived bytes (hex): ");
                printHex(buffer);

                // Convert the 2 bytes to a short
                DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(buffer));
                short number = dataIn.readShort();

                // Display the number N for the user
                System.out.println("Received short integer: " + number);

                // Display client info (IP + port)
                System.out.println("Client IP:Port -> " 
                                    + clientSocket.getInetAddress().getHostAddress() 
                                    + ":" + clientSocket.getPort());

                String s = String.valueOf(number);

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byteStream.write((byte)0xFE);
                byteStream.write((byte)0xFF);
                
                for (char c : s.toCharArray()) {
                    // Write high-order byte, then low-order byte
                    byteStream.write((byte)(c >> 8));
                    byteStream.write((byte)(c & 0xFF));
                }

                byte[] responseBytes = byteStream.toByteArray();
                System.out.print("Sending bytes (hex): ");
                printHex(responseBytes);

                out.write(responseBytes);
                out.flush();
            }

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
}
