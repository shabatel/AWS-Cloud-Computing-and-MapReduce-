import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Utills {

    public static String uncapitalizeChars(String s) {
        return s.replace('A', 'a').
                replace('B', 'b').replace('C', 'c').replace('D', 'd').replace('E', 'e')
                .replace('F', 'f').replace('G', 'g').replace('H', 'h').replace('I', 'i')
                .replace('J', 'j').replace('K', 'k').replace('L', 'l').replace('M', 'm')
                .replace('N', 'n').replace('O', 'o').replace('P', 'p').replace('Q', 'q')
                .replace('R', 'r').replace('S', 's').replace('T', 't').replace('U', 'u')
                .replace('V', 'v').replace('W', 'w').replace('X', 'x').replace('Y', 'y')
                .replace('Z', 'z');
    }

    public static void sleepMs(int ms) {
        try {
            sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public static void writeToFile(File toUserSummary, ArrayList<ReviewFromWorker> outputMsgs) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(toUserSummary, outputMsgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stringToHTML(String fileName, String data) {
        String head = "<html lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<title>Summary</title>\n" +
                "</head>\n" +
                "<body>\n";
        String tail = "</body>\n" +
                "</html>";
        try {
            PrintWriter pw = new PrintWriter(fileName + ".html");
            pw.println(head);

            Gson gson = new Gson();

            ReviewFromWorker[] workersReviews = gson.fromJson(data, ReviewFromWorker[].class);
            for (ReviewFromWorker finishedReview : workersReviews) {
                String line = paintLineBySentiment(finishedReview);
                pw.print(line);
            }
            pw.println(tail);

            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String paintLineBySentiment(ReviewFromWorker finishedReview) {
        String line = "";
        int sentiment = finishedReview.getSentimentAnalysis();
        switch (sentiment) {
            case 0:
                line = line + "<l1 style=\"color:darkred;\">";
                break;
            case 1:
                line = line + "<l1 style=\"color:red;\">";
                break;
            case 2:
                line = line + "<l1 style=\"color:black;\">";
                break;
            case 3:
                line = line + "<l1 style=\"color:lightgreen;\">";
                break;
            case 4:
                line = line + "<l1 style=\"color:darkgreen;\">";
                break;
        }
        String sarcastic;
        if (finishedReview.getSarcastic()) {
            sarcastic = "Yes";
        } else {
            sarcastic = "No";
        }

        line = line + finishedReview.getReview() + finishedReview.getEntities() + " Is sarcastic?: " + sarcastic + "<br><br>";

        return line;
    }


}

