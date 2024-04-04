import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataOutputStream dos;
    private BufferedReader reader;
    private String correctAnswer;
    private int score;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        score = 0;
        send("score " + score);
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer.substring(correctAnswer.length() - 1, correctAnswer.length());
    }

    public void send(String data) throws IOException {
        dos.writeBytes(data + "\n");
        dos.flush();
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void listenForMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received from client: " + message);
                if (message.startsWith("Score")) {
                    score -= Integer.parseInt(message.substring("Score ".length()).trim());
                    send("score " + score);
                    System.out.println("Client did not answer in time");
                    Server.moveAllToNextQuestion();
                } else if (message.startsWith("Expired")) {
                    Server.ClientOutOfTime(this);
                } else {
                    checkAnswer(message);
                }
            }
        } catch (IOException e) {
            try {
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                Server.removeClient(this);
                close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void checkAnswer(String clientAnswer) {
        clientAnswer = clientAnswer.trim();
        clientAnswer = clientAnswer.substring(0, 1);

        if (this.correctAnswer.equals(clientAnswer)) {
            System.out.println("Client answered correctly.");
            score += 10;
            try {
                send("correct " + score);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Client answered incorrectly.");
            score -= 10;
            try {
                send("wrong " + score);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Server.moveAllToNextQuestion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (reader != null)
            reader.close();
        if (dos != null)
            dos.close();
        if (socket != null)
            socket.close();
    }
}
