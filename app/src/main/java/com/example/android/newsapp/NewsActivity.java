package com.example.android.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewsActivity extends AppCompatActivity
        implements LoaderCallbacks<List<NewsItem>> {

    private static final String LOG_TAG = NewsActivity.class.getName();

    // Fixed values for the number of items to retrieve from The Guardian API
    private static final int ITEMS_DEFAULT = 10;
    private static final int ITEMS_MIN = 1;
    private static final int ITEMS_MAX = 50;

    /**
     * URL for news item data from the The Guardian dataset
     */
    // http://content.guardianapis.com/search?q=debate&from-date=2017-06-16&order-by=newest&page-size=25&api-key=test
    private static final String NEWS_REQUEST_URL =
            "https://content.guardianapis.com/search?q=debate";

    // Keys and values for query parameters
    static final String QUERY_FROMDATE_KEY = "from-date";
    static final String QUERY_PAGESIZE_KEY = "page-size";

    static final String QUERY_SECTION_KEY = "section";
    static final String QUERY_SECTION_VALUE = "business";
    static final String QUERY_FORMAT_KEY = "format";
    static final String QUERY_FORMAT_VALUE = "json";
    static final String QUERY_ORDERBY_KEY = "order-by";
    static final String QUERY_ORDERBY_VALUE = "newest";
    static final String QUERY_APIKEY_KEY = "api-key";
    static final String QUERY_APIKEY_VALUE = "test";


    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    /**
     * Adapter for the list of news
     */
    private NewsItemAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of news items as input
        mAdapter = new NewsItemAdapter(this, new ArrayList<NewsItem>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected item.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current news item that was clicked on
                NewsItem currentNewsItem = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsItemUri = Uri.parse(currentNewsItem.getNewsUrl());

                // Create a new intent to view the news item URI
                Intent webSiteIntent = new Intent(Intent.ACTION_VIEW, newsItemUri);

                // Suggestion #1:
                // You should check if there is an app installed on the phone,
                // able to handle you event, before you launch it

                // Check if there is an available application to show the web page
                if (webSiteIntent.resolveActivity(getPackageManager()) != null) {
                    // Send the intent to launch a new activity
                    startActivity(webSiteIntent);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.noApp,
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<NewsItem>> onCreateLoader(int i, Bundle bundle) {

        // http://content.guardianapis.com/search?q=debate&from-date=2017-06-16&order-by=newest&page-size=25&api-key=test

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String startDate = fromDate(sharedPrefs);
        String items = items(sharedPrefs);
        Uri baseUri = Uri.parse(NEWS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter(QUERY_SECTION_KEY, QUERY_SECTION_VALUE);
        uriBuilder.appendQueryParameter(QUERY_FROMDATE_KEY, startDate);
        uriBuilder.appendQueryParameter(QUERY_PAGESIZE_KEY, items);

        uriBuilder.appendQueryParameter(QUERY_FORMAT_KEY, QUERY_FORMAT_VALUE);
        uriBuilder.appendQueryParameter(QUERY_ORDERBY_KEY, QUERY_ORDERBY_VALUE);
        uriBuilder.appendQueryParameter(QUERY_APIKEY_KEY, QUERY_APIKEY_VALUE);
        return new NewsItemLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> newsItems) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No news items found."
        mEmptyStateTextView.setText(R.string.no_news_items);

        // Clear the adapter of previous news item data
        mAdapter.clear();

        // If there is a valid list of {@link NewsItem}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (newsItems != null && !newsItems.isEmpty()) {
            mAdapter.addAll(newsItems);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    /**
     * Return the starting date as a String for the URL query, being
     * it the current date minus the amount of days in the preferences
     *
     * @param sharedPrefs the SharedPreferences object
     * @return the starting date as String in yyyy-MM-dd format
     */
    private String fromDate(SharedPreferences sharedPrefs) {

        // Retrieve the value in preferenes
        String daysString = sharedPrefs.getString(
                getString(R.string.settings_days_back_key),
                getString(R.string.settings_days_back_default));

        // Retrieve current date and time
        Calendar c = Calendar.getInstance();
        // System.out.println("Current time => " + c.getTime());

        // Substract the amount of days in preferences
        int amount = Integer.parseInt(daysString) * -1;
        c.add(Calendar.DAY_OF_MONTH, amount);

        // Convert the resulting date to the required format
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

    /**
     * Return the number of articles to be retrieved
     *
     * @param sharedPrefs the SharedPreferences object
     * @return the number of articles to be retrieved, as a String
     */
    private String items(SharedPreferences sharedPrefs) {

        // Retrieve the value in preferenes
        String itemsString = sharedPrefs.getString(
                getString(R.string.settings_items_key),
                getString(R.string.settings_items_default));

        // Suggestion:
        // If you really wanna use hard limits like these one's please
        // refactor them in some constants at the top of the class and
        // give them a meaningful name.
        int itemsInt;
        // Ensure that the value is not out of limits
        try {
            itemsInt = Integer.parseInt(itemsString);
        }catch(NumberFormatException e){
            itemsInt = ITEMS_DEFAULT;
        }
        if (itemsInt < ITEMS_MIN) {
            itemsInt = ITEMS_MIN;
        }
        if (itemsInt > ITEMS_MAX) {
            itemsInt = ITEMS_MAX;
        }

        // Convert to String
        itemsString = String.valueOf(itemsInt);

        return itemsString;
    }
}
