import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private static final int portNumber = 12345;
    private static ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            System.out.println("Server started. Waiting for clients to connect...");
            List<Socket> clientSockets = new ArrayList<>();
            UDPThread udpThread = new UDPThread();
            udpThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress().toString());

                new Thread(() -> {
                    try {
                        // reading message from client
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));

                        String str = reader.readLine();
                        System.out.println("str from client: " + str);
                    } catch (IOException e) {
                        System.out.println("An error occurred with a client connection.");
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("An error occurred starting the server.");
            e.printStackTrace();
        }
    }

    private static class UDPThread extends Thread {
        private DatagramSocket socket;
        private boolean running;
        private byte[] buf = new byte[256];

        public UDPThread() throws SocketException {
            socket = new DatagramSocket(portNumber);
        }

        public void run() {
            running = true;
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);

                    String received = new String(packet.getData(), 0, packet.getLength());

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    System.out.println(
                            "Received: " + received + " from: " + address.getHostAddress() + ":" + port);

                    messageQueue.add(received);
                } catch (IOException e) {
                    System.out.println("IOException in UDPThread: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            socket.close();
        }
    }
}
