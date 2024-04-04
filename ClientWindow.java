import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.TimerTask;
import java.util.Scanner;
import java.util.Timer;
import javax.swing.*;

public class ClientWindow implements ActionListener {
    private JButton poll;
    private JButton submit;
    private JRadioButton options[];
    private ButtonGroup optionGroup;
    private static JLabel question;
    private static JLabel msg;
    private JLabel timer;
    private JLabel score;
    private TimerTask clock;
    private String serverIP = "127.0.0.1";
    private int serverPort = 12345;
    private static boolean canAnswer = false;
    private JFrame window;
    private Socket socket;

    public ClientWindow() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter sever IP adress: ");
        serverIP = in.nextLine();

        window = new JFrame("Trivia");

        question = new JLabel("Q1. This is a sample question");
        msg = new JLabel("Connected to server.");
        window.add(question);
        window.add(msg);
        msg.setBounds(400, 210, 350, 100);
        question.setBounds(10, 5, 450, 100);

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (

                int index = 0; index < options.length; index++) {
            options[index] = new JRadioButton("Option " + (index + 1));
            options[index].addActionListener(this);
            options[index].setBounds(10, 110 + (index * 20), 350, 20);
            options[index].setEnabled(false);
            window.add(options[index]);
            optionGroup.add(options[index]);
        }

        timer = new JLabel("TIMER"); // represents the countdown shown on the window
        timer.setBounds(250, 250, 100, 20);
        clock = new TimerCode(30); // represents clocked task that should run after X seconds
        Timer t = new Timer(); // event generator
        t.schedule(clock, 0, 1000); // clock is called every second
        window.add(timer);

        score = new JLabel("SCORE");
        score.setBounds(50, 250, 100, 20);
        window.add(score);

        poll = new JButton("Poll");
        poll.setBounds(10, 300, 100, 20);
        poll.addActionListener(this);
        window.add(poll);

        submit = new JButton("Submit");
        submit.setBounds(200, 300, 100, 20);
        submit.addActionListener(this);
        submit.setEnabled(canAnswer);
        window.add(submit);

        window.setSize(700, 400);
        window.setBounds(50, 50, 700, 400);
        window.setLayout(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        try {
            socket = new Socket(serverIP, serverPort);
            System.out.println("Connected to server.");
            window.setTitle("Connected to " + serverIP);
            readFromSocket(socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String input = e.getActionCommand();
        switch (input) {
            case "Poll":
                try {
                    if (!canAnswer) {
                        byte[] buf = "buzz".getBytes();
                        InetAddress address = InetAddress.getByName(serverIP);
                        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, serverPort);
                        DatagramSocket socket = new DatagramSocket();
                        socket.send(packet);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                break;
            case "Submit":
                String selectedAnswer = null;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].isSelected()) {
                        selectedAnswer = options[i].getText();
                        break;
                    }
                }
                if (selectedAnswer != null) {
                    System.out.println("Selected Answer: " + selectedAnswer);
                    sendAnswer(selectedAnswer);
                }
                break;
        }

    }

    // this class is responsible for running the timer on the window
    public class TimerCode extends TimerTask {
        private int duration; // write setters and getters as you need

        public TimerCode(int duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            if (duration < 0) {
                timer.setText("Timer expired");
                window.repaint();
                this.cancel();
                submit.setEnabled(false);
                poll.setEnabled(false);
                for (JRadioButton option : options) {
                    option.setEnabled(false);
                }
                optionGroup.clearSelection();
                if (canAnswer) {
                    sendAnswer("Score 20");
                    msg.setText("You did not answer in time! -20");
                } else {
                    sendAnswer("Expired");
                    msg.setText("Out of time!");
                    clock.cancel();
                }
                canAnswer = false;
                return;
            }

            if (duration < 6)
                timer.setForeground(Color.red);
            else
                timer.setForeground(Color.black);

            timer.setText(duration + "");
            duration--;
            window.repaint();
        }
    }

    public static void main(String[] args) {
        ClientWindow window = new ClientWindow();
    }

    private void readFromSocket(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String str;
        while ((str = reader.readLine()) != null) {
            if (str.startsWith("Q")) {
                processQuestion(str.substring(1));
                poll.setEnabled(true);
            } else if (str.trim().equals("ACK")) {
                System.out.println("ACK");

                canAnswer = true;
                submit.setEnabled(canAnswer);
                for (JRadioButton option : options) {
                    option.setEnabled(canAnswer);
                }
                msg.setText("You buzzed first!");
            } else if (str.trim().equals("NAK")) {
                System.out.println("NAK");
                msg.setText("Too Slow!");
            } else if (str.startsWith("correct")) {
                String scoreValue = str.substring("correct ".length()).trim();
                canAnswer = false;
                submit.setEnabled(canAnswer);
                for (JRadioButton option : options) {
                    option.setEnabled(canAnswer);
                }
                optionGroup.clearSelection();
                msg.setText("Good job! +10");
                score.setText("SCORE: " + scoreValue);
            } else if (str.startsWith("wrong")) {
                String scoreValue = str.substring("wrong ".length()).trim();
                canAnswer = false;
                submit.setEnabled(canAnswer);
                for (JRadioButton option : options) {
                    option.setEnabled(canAnswer);
                }
                optionGroup.clearSelection();
                msg.setText("Maybe next time! -10");
                score.setText("SCORE: " + scoreValue);
            } else if (str.startsWith("score")) {
                String scoreValue = str.substring("score ".length()).trim();
                score.setText("SCORE: " + scoreValue);
            } else if (str.startsWith("END")) {
                question.setForeground(Color.red);
                question.setText("Thank you for playing! You're final score is below!");
                poll.setEnabled(false);
                msg.setText("");
                clock.cancel();
            } else if (str.startsWith("Time")) {
                int time = Integer.parseInt(str.substring("Time ".length()).trim());
                resetTimer(time);
            }
        }
        reader.close();
    }

    private void processQuestion(String questionData) {
        String[] parts = questionData.split("\\[");
        String questionPart = parts[0];
        String choices = questionData.substring(questionPart.length() + 1, questionData.length() - 1);
        String questionNumber = questionPart.split("\\.")[0].trim();
        String questionText = questionPart.substring(questionNumber.length() + 1).trim();
        updateOptions(questionNumber, questionText, choices);
    }

    public void updateOptions(String questionNumber, String questionText, String optionsPart) {
        question.setText(questionNumber + ". " + questionText);

        String[] optionsArray = optionsPart.split(", ");
        for (int i = 0; i < this.options.length && i < optionsArray.length; i++) {
            this.options[i].setText(optionsArray[i].trim());
        }
    }

    private void sendAnswer(String answer) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetTimer(int newDuration) {
        if (clock != null) {
            clock.cancel();
        }
        clock = new TimerCode(newDuration);
        Timer t = new Timer();
        t.scheduleAtFixedRate(clock, 0, 1000);
    }
}