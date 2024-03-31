import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import javax.swing.*;

public class ClientWindow implements ActionListener {
    private JButton poll;
    private JButton submit;
    private JRadioButton options[];
    private ButtonGroup optionGroup;
    private JLabel question;
    private JLabel timer;
    private JLabel score;
    private TimerTask clock;
    private String serverIP = "127.0.0.1";
    private int serverPort = 12345;
    private List<TriviaQuestion> triviaQuestions;
    private int currentQuestionIndex = 0;

    private JFrame window;

    private static SecureRandom random = new SecureRandom();

    // write setters and getters as you need

    public ClientWindow() {
        try (Socket socket = new Socket(serverIP, serverPort)) {
            System.out.println("Connected to server.");
            // Question and answer choices from server
            InputStream inputStream = socket.getInputStream();

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File triviaFile = new File("qAndA.txt"); // Change to your file path
            triviaQuestions = readTriviaQuestions(triviaFile);

            // Display trivia questions
            for (int i = 0; i < triviaQuestions.size(); i++) {
                TriviaQuestion question = triviaQuestions.get(i);
                System.out.println((i + 1) + ". " + question.getQuestion());
                List<String> options = question.getOptions();
                for (int j = 0; j < options.size(); j++) {
                    System.out.println((char) ('A' + j) + ") " + options.get(j));
                }
                System.out.println("Correct Answer: " + question.getCorrectAnswer() + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    

        JOptionPane.showMessageDialog(window, "This is a trivia game");

        window = new JFrame("Trivia");
 // Fetch the first trivia question from the list
TriviaQuestion firstQuestion = triviaQuestions.get(0);

// Set the question text from the first question
question = new JLabel("Q1. " + firstQuestion.getQuestion());
window.add(question);
question.setBounds(10, 5, 350, 100);

options = new JRadioButton[4];
optionGroup = new ButtonGroup();

List<String> questionOptions = firstQuestion.getOptions();
for (int index = 0; index < options.length; index++) {
    // Get option text from the question options list
    String optionText = questionOptions.get(index);
    
    options[index] = new JRadioButton(optionText); // Use the option text
    options[index].addActionListener(this);
    options[index].setBounds(10, 110 + (index * 20), 350, 20);
    window.add(options[index]);
    optionGroup.add(options[index]);
}


        timer = new JLabel("TIMER"); // represents the countdown shown on the window
        timer.setBounds(250, 250, 100, 20);
        clock = new TimerCode(30); // represents clocked task that should run after X seconds
        Timer t = new Timer(); // event generator
        t.schedule(clock, 0, 1000); // clock is called every second
        window.add(timer);

        score = new JLabel("SCORE"); // represents the score
        score.setBounds(50, 250, 100, 20);
        window.add(score);

        poll = new JButton("Poll"); // button that use clicks/ like a buzzer
        poll.setBounds(10, 300, 100, 20);
        poll.addActionListener(this); // calls actionPerformed of this class
        window.add(poll);

        submit = new JButton("Submit"); // button to submit their answer
        submit.setBounds(200, 300, 100, 20);
        submit.addActionListener(this); // calls actionPerformed of this class
        window.add(submit);

        window.setSize(400, 400);
        window.setBounds(50, 50, 400, 400);
        window.setLayout(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
    }

    
    public static List<TriviaQuestion> readTriviaQuestions(File file) throws FileNotFoundException {
        List<TriviaQuestion> triviaQuestions = new ArrayList<>();
    
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String question = reader.nextLine().replaceFirst("^\\d+\\. ", "").trim(); // Remove initial number
                List<String> options = new ArrayList<>();
    
                // Read options
                for (int i = 0; i < 4 && reader.hasNextLine(); i++) {
                    options.add(reader.nextLine().replaceFirst("^[A-D]\\) ", "").trim()); // Remove initial letter and space
                }
    
                // Read correct answer
                if (reader.hasNextLine()) {
                    String correctAnswerLine = reader.nextLine().trim();
                    if (correctAnswerLine.startsWith("Correct Answer:")) {
                        String correctAnswer = correctAnswerLine.substring(correctAnswerLine.indexOf(":") + 1).trim();
                        triviaQuestions.add(new TriviaQuestion(question, options, correctAnswer));
                    } else {
                        // Handle incorrect format for correct answer
                        System.err.println("Incorrect format for correct answer: " + correctAnswerLine);
                    }
                } else {
                    // Handle unexpected end of file
                    System.err.println("Unexpected end of file while reading questions.");
                    break; // Exit loop
                }
                
                // Skip empty line if present
                if (reader.hasNextLine()) {
                    reader.nextLine();
                }
            }
        }
    
        return triviaQuestions;
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex < triviaQuestions.size()) {
            TriviaQuestion currentQuestion = triviaQuestions.get(currentQuestionIndex);
            // Set the question text
            question.setText("Q" + (currentQuestionIndex + 1) + ". " + currentQuestion.getQuestion());
            
            List<String> questionOptions = currentQuestion.getOptions();
            for (int index = 0; index < options.length; index++) {
                // Get option text from the question options list
                String optionText = questionOptions.get(index);
    
                // Update existing radio buttons with new option text
                options[index].setText(optionText);
                
                // Enable radio buttons if disabled
                options[index].setEnabled(true);
            }
        } else {
            // No more questions, display end game message or handle accordingly
            JOptionPane.showMessageDialog(window, "No more questions. End of game!");
        }
    }

    // this method is called when you check/uncheck any radio button
    // this method is called when you press either of the buttons- submit/poll
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("You clicked " + e.getActionCommand());

        // input refers to the radio button you selected or button you clicked
        String input = e.getActionCommand();
        switch (input) {
            case "Option 1": // Your code here
                break;
            case "Option 2": // Your code here
                break;
            case "Option 3": // Your code here
                break;
            case "Option 4": // Your code here
                break;
            case "Poll": // Your code here
                break;
            case "Submit": // Your code here
            currentQuestionIndex++;
            displayQuestion();
                break;
            default:
                // System.out.println("Incorrect Option");
        }

        // test code below to demo enable/disable components
        // DELETE THE CODE BELOW FROM HERE***
        if (poll.isEnabled()) {
            poll.setEnabled(false);
            submit.setEnabled(true);
        } else {
            poll.setEnabled(true);
            submit.setEnabled(false);
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
                this.cancel(); // cancel the timed task
                return;
                // you can enable/disable your buttons for poll/submit here as needed
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
}