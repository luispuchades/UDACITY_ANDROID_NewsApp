package com.example.android.newsapp;

/**
 * Created by luisp on 11/07/2017.
 */


/**
 * A (@link NewsItem} object that defines which are the components of each News item
 */
public class NewsItem {

    /** Variable declaration */
    /** ******************** */

    /** News Section */
    private String mNewsSection;

    /** News Publication Date */
    private String mNewsPublicationDate;

    /** News Title */
    private String mNewsTitle;

    /** News URL */
    private String mNewsUrl;



    /** Constructor declaration */
    public NewsItem(String newsSection, String newsPublicationDate, String newsTitle,
                    String newsUrl) {

        mNewsTitle = newsTitle;
        mNewsPublicationDate = newsPublicationDate;
        mNewsSection = newsSection;
        mNewsUrl = newsUrl;
    }

    /** Getters methods declaration */
    /** *************************** */

    /** Get Section */
    public String getNewsSection() {
        return mNewsSection;
    }

    /** Get Publication Date */
    public String getNewsPublicationDate() {
        return mNewsPublicationDate;
    }

    /** Get title */
    public String getNewsTitle() {
        return mNewsTitle;
    }

    public String getNewsUrl() {
        return mNewsUrl;
    }
}
