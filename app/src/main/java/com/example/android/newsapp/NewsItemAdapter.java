package com.example.android.newsapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * An {@link NewsItemAdapter} knows how to create a list item layout for each news item
 * in the data source (a list of {@link NewsItem} objects).
 *
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */
public class NewsItemAdapter extends ArrayAdapter<NewsItem> {

    private static final String LOG_TAG = NewsItemAdapter.class.getName();

    /**
     * Constructs a new {@link NewsItemAdapter}.
     *
     * @param context of the app
     * @param newsItems is the list of news items, which is the data source of the adapter
     */
    public NewsItemAdapter(Context context, List<NewsItem> newsItems) {
        super(context, 0, newsItems);
    }

    /**
     * Returns a list item view that displays information about the news item at the given position
     * in the list of news items.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
        }

        // Find the news item at the given position in the list of news items
        NewsItem currentNewsItem = getItem(position);

        /** GET SECTION */
        // Get the section string from the NewsItem object,
        String newsItemSection = currentNewsItem.getNewsSection();
        // Find the TextView for book title
        TextView newsItemSectionView = (TextView) listItemView.findViewById(R.id.text_news_section);
        // Display the section in the section TextView
        newsItemSectionView.setText(newsItemSection);

        /** GET PUBLISHED DATE  */
        // Find the TextView with view ID published_date
        TextView publishedDate = (TextView) listItemView.findViewById(R.id.text_news_date);

        // Convert the published date from ISO JSON format to java Date type
        String iso8601string = currentNewsItem.getNewsPublicationDate();
        String s = iso8601string.replace("Z", "+0000");
        Date dateTime = null;
        try {
            dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);

            // Convert the date to user format
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(dateTime);

            String dateOut;
            DateFormat dateFormatter;

            //Locale locale = Locale.getDefault();
            dateFormatter = DateFormat.getDateTimeInstance();
            dateOut = dateFormatter.format(dateTime);
            publishedDate.setText(dateOut);

        } catch (ParseException e) {
            // Suggestion:
            // Always better to stick this in a log statement instead of e.printStackTrace();
            Log.e(LOG_TAG, "Error parsing the date time of the article.");
        }

        /** GET TITLE  */
        // Get the title string from the NewsItem object,
        String newsItemTitle = currentNewsItem.getNewsTitle();
        // Find the TextView for book title
        TextView newsItemTitleView = (TextView) listItemView.findViewById(R.id.text_news_title);
        // Display the title in the title TextView
        newsItemTitleView.setText(newsItemTitle);

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }
}


