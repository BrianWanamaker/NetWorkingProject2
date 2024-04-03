import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private static final int portNumber = 12345;
    private static ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private static List<TriviaQuestion> triviaQuestions;
    private static int currentQuestionIndex = 0;
    private static boolean receivingPoll = true;
    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) {
        triviaQuestions = new ArrayList<>();
        try {
            readInFile("qAndA.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            System.out.println("Server started. Waiting for clients to connect...");
            UDPThread udpThread = new UDPThread();
            udpThread.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress().toString());

                new Thread(() -> {
                    try {
                        sendCurrentQuestionToClients(clientHandler);
                        clientHandler.listenForMessages();
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
                    if (receivingPoll) {
                        receivingPoll = false;
                        if (messageQueue.size() == 0) {
                            ClientHandler matchingHandler = null;
                            for (ClientHandler handler : clientHandlers) {
                                if (handler.getSocket().getInetAddress().equals(address)) {
                                    matchingHandler = handler;
                                    break;
                                }
                            }

                            if (matchingHandler != null) {
                                System.out.println("Sending ACK to " + address.getHostAddress());
                                try {
                                    matchingHandler.send("ACK");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("No matching TCP client found for " + address.getHostAddress());
                            }
                        } else {
                            ClientHandler matchingHandler = null;
                            for (ClientHandler handler : clientHandlers) {
                                if (handler.getSocket().getInetAddress().equals(address)) {
                                    matchingHandler = handler;
                                    break;
                                }
                            }

                            if (matchingHandler != null) {
                                System.out.println("Sending NAK to " + address.getHostAddress());
                                try {
                                    matchingHandler.send("NAK");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("No matching TCP client found for " + address.getHostAddress());
                            }
                        }
                    }
                } catch (IOException e) {
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

    private static void sendCurrentQuestionToClients(ClientHandler clientHandler) throws IOException {
        if (currentQuestionIndex < triviaQuestions.size()) {
            TriviaQuestion currentQuestion = triviaQuestions.get(currentQuestionIndex);
            String questionData = "Q" + currentQuestion.toString();
            clientHandler.send(questionData);
            clientHandler.setCorrectAnswer(currentQuestion.getCorrectAnswer());
        } else {
            System.out.println("end of game");
            clientHandler.send("END");
        }

    }

    public static void moveAllToNextQuestion() throws IOException {
        receivingPoll = true;
        messageQueue.clear();
        currentQuestionIndex++;

        for (ClientHandler clientHandler : clientHandlers) {
            sendCurrentQuestionToClients(clientHandler);
        }
    }

}
