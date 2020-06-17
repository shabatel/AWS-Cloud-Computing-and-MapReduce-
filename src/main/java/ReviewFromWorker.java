


public class ReviewFromWorker {
    Review review;
    Integer sentimentAnalysis;
    String entities;
    Boolean sarcastic;

    public void setReview(Review review) {
        this.review = review;
    }

    public void setSentimentAnalysis(Integer sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }

    public void setSarcastic(Boolean sarcastic) {
        this.sarcastic = sarcastic;
    }

    @Override
    public String toString() {
        return "ReviewFromWorker{" +
                "review=" + review + '\'' +
                ", sentimentAnalysis=" + sentimentAnalysis + '\'' +
                ", entities='" + entities + '\'' +
                ", sarcastic=" + sarcastic + '\'' +
                '}';
    }

    public ReviewFromWorker(Review review, Integer sentimentAnalysis, String entities, Boolean sarcastic) {
        this.review = review;
        this.sentimentAnalysis = sentimentAnalysis;
        this.entities = entities;
        this.sarcastic = sarcastic;
    }

    public Integer getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public String getEntities() {
        return entities;
    }

    public Boolean getSarcastic() {
        return sarcastic;
    }

    public Review getReview() {  return review;
    }
}
