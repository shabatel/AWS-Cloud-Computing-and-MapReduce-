import java.util.Arrays;

public class TitleReviews{

    private String title;
    private Review[] reviews;



    public Review[] getReviews() {
        return reviews;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "TitleReviews{" +
                "title='" + title + '\'' +
                ", reviews=" + Arrays.toString(reviews) +
                '}';
    }
}