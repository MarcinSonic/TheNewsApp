package pl.marcingorski.thenewsapp;

public class Article {

    private String mUrl;

    // @param title of the article
    private String titleOfArticle;

    // @ param section of the article
    private String section;

    // @ param author of the article
    private String author;

    // @ param publish date of the article
    private String datePublished;

    public Article(String titleOfArticle, String section, String author, String datePublished, String url) {
        this.titleOfArticle = titleOfArticle;
        this.section = section;
        this.author = author;
        this.datePublished = datePublished;
        this.mUrl = url;
    }

    public String getTitleOfArticle() {
        return titleOfArticle;
    }

    public String getSection() {
        return section;
    }

    public String getAuthor() {
        return author;
        }

    public String getDatePublished() {
        return datePublished;
    }

    public String getmUrl() {
        return mUrl;
    }
}
