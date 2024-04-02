import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private static final int portNumber = 12345;
    private static ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private static List<TriviaQuestion> triviaQuestions;
    private static int currentQuestionIndex = 0;

    public static void main(String[] args) {
        triviaQuestions = new ArrayList<>();
        try {
            readInFile("qAndA.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(triviaQuestions.get(currentQuestionIndex).toString());

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
                        sendCurrentQuestionToClient(clientSocket);

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

    // reads in file and adds String question, List<String> options, String
    // correctAnswer to arraylist of trivaQuestions
    public static void readInFile(String path) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException();

        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            String str = reader.nextLine();
            if (!str.isEmpty()) {
                String question = str;
                List<String> options = new ArrayList<>();
                options.add(reader.nextLine());
                options.add(reader.nextLine());
                options.add(reader.nextLine());
                options.add(reader.nextLine());
                String correctAnswer = reader.nextLine();
                triviaQuestions.add(new TriviaQuestion(question, options, correctAnswer));
            }

        }
        reader.close();
    }

    private static void sendCurrentQuestionToClient(Socket clientSocket) throws IOException {
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        String str = triviaQuestions.get(currentQuestionIndex).toString();
        byte[] b = str.getBytes();

        dos.write(b, 0, str.length());
        dos.flush();
        dos.close();
    }

}
