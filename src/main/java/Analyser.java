import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;


public class Analyser {

    private StanfordCoreNLP sentimentPipeline;


    public Analyser() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, parse, sentiment");
        sentimentPipeline = new StanfordCoreNLP(props);
    }


    public int findSentiment(String review) {

        int mainSentiment = 0;
        if (review!= null && review.length() > 0) {
            int longest = 0;
            Annotation annotation = sentimentPipeline.process(review);
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.AnnotatedTree.class); //was .get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (longest < partText.length()) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }

            }
        }
        return mainSentiment;
    }


    public boolean isSarcastic(int rating, int sentimentAnalysis) {
        if (rating == 5) {
            return sentimentAnalysis == 0 || sentimentAnalysis == 1;
        }
        else if (rating == 4) {
            return sentimentAnalysis == 0;
        }
        else if (rating == 2) {
            return sentimentAnalysis == 3 || sentimentAnalysis == 4;
        }
        else if (rating == 1) {
            return sentimentAnalysis == 4;
        }
        return false;
    }
}