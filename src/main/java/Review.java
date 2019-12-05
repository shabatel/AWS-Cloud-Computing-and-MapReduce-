public class Review {

    private String id;
    private String link;
    private String title;
    private String text;
    private int rating;
    private String author;

    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getRating() {
        return rating;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    private String date;

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", link='" + link + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", rating=" + rating +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}