import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class Server {
    private static final int portNumber = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started. Waiting for clients to connect...");
            List<Socket> clientSockets = new ArrayList<>();

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
}
