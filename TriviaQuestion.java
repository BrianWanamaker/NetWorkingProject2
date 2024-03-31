import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TriviaQuestion {
    private String question;
    private List<String> options;
    private String correctAnswer;

    public TriviaQuestion(String question, List<String> options, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
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

}

